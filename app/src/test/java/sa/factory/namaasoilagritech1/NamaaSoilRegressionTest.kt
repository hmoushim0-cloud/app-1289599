package sa.factory.namaasoilagritech1

import sa.factory.namaasoilagritech1.data.repository.NamaaSoilRepositoryImpl
import sa.factory.namaasoilagritech1.hardware.HardwareSensorDriver
import sa.factory.namaasoilagritech1.hardware.IrrigationPulseSchedulerEngine
import sa.factory.namaasoilagritech1.hardware.LorawanSoilMoistureEngine
import sa.factory.namaasoilagritech1.hardware.SoilHydrationStatus
import sa.factory.namaasoilagritech1.model.NamaaSoilItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NamaaSoilRegressionTest {

    @Test
    fun testCheckoutCalculationAndTax() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = NamaaSoilRepositoryImpl(sensorDriver)

        val item1 = NamaaSoilItem("t1", "خدمة تجريبية أ", "الوصف", 100.0, "معتمد", "عام")
        val item2 = NamaaSoilItem("t2", "خدمة تجريبية ب", "الوصف", 200.0, "معتمد", "عام")

        repository.addToCart(item1)
        repository.addToCart(item2)

        val order = repository.checkout("Mada EMV")

        assertEquals(300.0, order.subtotalSar, 0.01)
        assertEquals(45.0, order.vatSar, 0.01)
        assertEquals(345.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)
    }

    @Test
    fun testCayenneLppEncodingAndDecoding() {
        val engine = LorawanSoilMoistureEngine()

        val frame = engine.synthesizeCayenneFrame(
            vwc = 28.5,
            tempC = 31.2,
            ecDsM = 1.45,
            valveOpen = true
        )

        val reading = engine.decodeCayenneLppPayload(frame, devEui = "AABBCCDDEEFF0011", fCnt = 42)

        assertTrue("Frame must be valid", reading.isValidFrame)
        assertEquals("AABBCCDDEEFF0011", reading.devEui)
        assertEquals(42, reading.frameCounter)
        assertEquals(28.5, reading.vwcPercentage, 0.5)
        assertEquals(31.2, reading.soilTempCelsius, 0.2)
        assertEquals(1.45, reading.electricalConductivityDsM, 0.05)
        assertTrue("Valve state must match encoded frame", reading.isValveOpen)
    }

    @Test
    fun testSoilHydrationStatusTransitions() {
        // Saturated (> 38%)
        assertEquals(SoilHydrationStatus.SATURATED, LorawanSoilMoistureEngine.evaluateHydration(41.0))

        // Optimal Field Capacity (22% to 38%)
        assertEquals(SoilHydrationStatus.FIELD_CAPACITY, LorawanSoilMoistureEngine.evaluateHydration(27.0))

        // Mild Moisture Deficit (14% to 22%)
        assertEquals(SoilHydrationStatus.MILD_MOISTURE_DEFICIT, LorawanSoilMoistureEngine.evaluateHydration(18.5))

        // Critical Wilting Point (< 14%)
        assertEquals(SoilHydrationStatus.WILTING_POINT, LorawanSoilMoistureEngine.evaluateHydration(11.0))
    }

    @Test
    fun testCropWaterRequirementAndEvapotranspiration() {
        val scheduler = IrrigationPulseSchedulerEngine()

        // Test normal date palm with optimal moisture
        val planOptimal = scheduler.calculateIrrigationPlan(
            vwcCurrent = 29.0,
            soilEcDsM = 1.2,
            ambientTempC = 32.0,
            cropType = "Date Palm (Phoenix dactylifera)"
        )

        assertEquals(0.90, planOptimal.cropCoefficientKc, 0.01)
        assertTrue("ET0 must be positive", planOptimal.referenceEt0Mm > 0.0)
        assertTrue("ETc must be positive", planOptimal.cropWaterRequirementMm > 0.0)
        assertFalse("Optimal soil moisture should not trigger urgent irrigation", planOptimal.isAutomatedIrrigationDue)
    }

    @Test
    fun testSalinityLeachingFractionAndPulseScheduling() {
        val scheduler = IrrigationPulseSchedulerEngine()

        // Test depleted soil with high salinity (EC = 3.2 dS/m)
        val planStress = scheduler.calculateIrrigationPlan(
            vwcCurrent = 15.0,
            soilEcDsM = 3.2,
            ambientTempC = 38.0,
            cropType = "Date Palm (Phoenix dactylifera)"
        )

        assertTrue("Low moisture must trigger automated irrigation", planStress.isAutomatedIrrigationDue)
        assertTrue("High salinity must trigger positive leaching fraction", planStress.salinityLeachingAddedPct > 0)
        assertTrue("Pulse count should be divided to prevent runoff", planStress.recommendedPulseCount >= 2)
        assertEquals(5, planStress.downlinkCommandHex.length / 2) // 5 bytes hex = 10 chars
    }

    @Test
    fun testHardwareSensorDriverIntegration() = runBlocking {
        val driver = HardwareSensorDriver()
        assertNotNull(driver.loraEngine)
        assertNotNull(driver.schedulerEngine)

        val firstFrame = driver.observePeripheralStream().first()
        assertEquals("CONNECTING", firstFrame.connectionState)
        assertTrue(firstFrame.peripheralName.contains("LoRaWAN Soil Moisture"))
    }
}
