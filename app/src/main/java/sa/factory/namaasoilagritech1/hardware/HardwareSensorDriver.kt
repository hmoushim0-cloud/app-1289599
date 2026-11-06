package sa.factory.namaasoilagritech1.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "LoRaWAN Soil Moisture Node + Solenoid Valve Relays"
    val loraEngine = LorawanSoilMoistureEngine()
    val schedulerEngine = IrrigationPulseSchedulerEngine()

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "LoRaWAN Gateway Connecting (AS923/EU868 Ch 1-8)..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "LoRaWAN Soil Node & Solenoids: ONLINE | READY | Calibrated"))

        var vwc = 24.5
        var soilTemp = 28.5
        val soilEc = 1.35
        var frameIndex = 0

        while (true) {
            delay(1500)
            frameIndex++
            val jitter = Random.nextInt(1, 8)
            vwc = (vwc + Random.nextDouble(-0.3, 0.4)).coerceIn(12.0, 36.0)
            soilTemp = (soilTemp + Random.nextDouble(-0.1, 0.2)).coerceIn(20.0, 42.0)

            val rawFrame = loraEngine.synthesizeCayenneFrame(
                vwc = vwc,
                tempC = soilTemp,
                ecDsM = soilEc,
                valveOpen = false
            )
            val reading = loraEngine.decodeCayenneLppPayload(rawFrame, fCnt = frameIndex)
            val plan = schedulerEngine.calculateIrrigationPlan(
                vwcCurrent = reading.vwcPercentage,
                soilEcDsM = reading.electricalConductivityDsM,
                ambientTempC = reading.soilTempCelsius
            )

            val valveState = if (plan.isAutomatedIrrigationDue) "ACTIVE" else "IDLE"
            val payload = "LORA AS923 | VWC: ${reading.vwcPercentage}% | Temp: ${reading.soilTempCelsius}C | EC: ${reading.electricalConductivityDsM} dS/m | Status: ${reading.hydrationStatus.levelSar} | Valve: $valveState | ETc: ${plan.cropWaterRequirementMm}mm | Pulses: ${plan.recommendedPulseCount}x${plan.durationPerPulseMinutes}m"

            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", payload))
        }
    }
}
