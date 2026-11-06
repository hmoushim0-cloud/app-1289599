package sa.factory.namaasoilagritech1.hardware

import kotlin.math.*

/**
 * Intelligent Pulse Irrigation Scheduler & Solenoid Actuator Engine
 * Computes date palm and greenhouse crop evapotranspiration (ETc), root zone soil
 * moisture deficits, salinity leaching fractions, and pulsed solenoid valve timings.
 */

data class IrrigationSchedulePlan(
    val cropType: String,
    val cropCoefficientKc: Double,
    val referenceEt0Mm: Double,
    val cropWaterRequirementMm: Double,
    val deficitLitersPerTree: Double,
    val recommendedPulseCount: Int,
    val durationPerPulseMinutes: Int,
    val soakIntervalMinutes: Int,
    val salinityLeachingAddedPct: Int,
    val isAutomatedIrrigationDue: Boolean,
    val downlinkCommandHex: String,
    val rationale: String
)

class IrrigationPulseSchedulerEngine {

    /**
     * Calculates precision pulse irrigation schedule based on soil telemetry.
     */
    fun calculateIrrigationPlan(
        vwcCurrent: Double,
        soilEcDsM: Double,
        ambientTempC: Double,
        cropType: String = "Date Palm (Phoenix dactylifera)",
        rootDepthMeters: Double = 1.2
    ): IrrigationSchedulePlan {
        val kc = when {
            cropType.contains("Date Palm", ignoreCase = true) -> 0.90
            cropType.contains("Greenhouse", ignoreCase = true) -> 0.75
            else -> 0.85
        }

        // Hargreaves-Samani temperature-based ET0 proxy for desert climate
        val et0 = max(3.5, min(9.5, 0.16 * ambientTempC))
        val etc = (et0 * kc * 10.0).roundToInt() / 10.0

        val targetVwc = 28.0 // Optimal Field Capacity
        val moistureDeficitPct = max(0.0, targetVwc - vwcCurrent)

        // Date palm basin area ~10 m2
        val basinAreaM2 = 10.0
        val baseWaterNeededLiters = moistureDeficitPct * 10.0 * basinAreaM2 * rootDepthMeters * 0.1

        // Leaching requirement if soil EC > 2.2 dS/m
        val leachingPct = if (soilEcDsM > 2.2) {
            min(30, ((soilEcDsM - 2.2) * 15.0).roundToInt())
        } else 0

        val totalDeficitLiters = baseWaterNeededLiters * (1.0 + leachingPct / 100.0)
        val isDue = vwcCurrent < 22.0 || totalDeficitLiters > 40.0

        // Pulse scheduling: 3 to 4 pulses to avoid surface runoff in sandy loam
        val emitterRateLph = 60.0 // Standard drip/bubbler emitter 60 L/h
        val totalRunMinutes = if (isDue) ((totalDeficitLiters / emitterRateLph) * 60.0).roundToInt() else 0
        val pulseCount = if (totalRunMinutes >= 30) 3 else if (totalRunMinutes > 0) 2 else 0
        val durationPerPulse = if (pulseCount > 0) max(10, totalRunMinutes / pulseCount) else 0

        // Generate downlink frame for Solenoid Valve Actuator (Port 2)
        // Format: [ValveID(1B), OpCode(1B), DurationMin(2B), Checksum(1B)]
        val valveId = 0x01
        val opCode = if (isDue) 0x01 else 0x00
        val durHigh = (durationPerPulse ushr 8) and 0xFF
        val durLow = durationPerPulse and 0xFF
        val chk = (valveId + opCode + durHigh + durLow) and 0xFF

        val downlinkBytes = byteArrayOf(
            valveId.toByte(),
            opCode.toByte(),
            durHigh.toByte(),
            durLow.toByte(),
            chk.toByte()
        )
        val downlinkHex = downlinkBytes.joinToString("") { String.format("%02X", it) }

        val rationale = if (isDue) {
            "Soil moisture below field capacity (${vwcCurrent}% VWC). Dispatched ${pulseCount} pulse cycles with ${durationPerPulse} min intervals (Leaching: +${leachingPct}%)."
        } else {
            "Soil moisture within optimal field capacity range (${vwcCurrent}% VWC). Solenoids idle."
        }

        return IrrigationSchedulePlan(
            cropType = cropType,
            cropCoefficientKc = kc,
            referenceEt0Mm = (et0 * 10.0).roundToInt() / 10.0,
            cropWaterRequirementMm = etc,
            deficitLitersPerTree = (totalDeficitLiters * 10.0).roundToInt() / 10.0,
            recommendedPulseCount = pulseCount,
            durationPerPulseMinutes = durationPerPulse,
            soakIntervalMinutes = 20,
            salinityLeachingAddedPct = leachingPct,
            isAutomatedIrrigationDue = isDue,
            downlinkCommandHex = downlinkHex,
            rationale = rationale
        )
    }
}
