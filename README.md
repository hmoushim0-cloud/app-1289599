# تطبيق نماء الزراعي — منظومة التقنية الزراعية والري الذكي (`namaa-soil-agritech-1289599`)
> منظومة agritech المتكاملة مع ربط LoRaWAN Soil Moisture Node + Solenoid Valve Relays

- **Project ID**: `#1289599`
- **Commercial Archetype**: `agritech`
- **Bound Hardware Peripheral**: `LoRaWAN Soil Moisture Node + Solenoid Valve Relays`
- **Package Name**: `sa.factory.namaasoilagritech1`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)

---

## [Sidekick Autonomous Update - Iteration 39]

### Architectural Innovations & Hardware Drivers

1. **`LorawanSoilMoistureEngine.kt`**:
   - Conforms to standard Cayenne LPP binary protocol over LoRaWAN EU868 / AS923 gateways.
   - Decodes multi-sensor telemetry channels:
     - Channel 1 (0x68): Volumetric Water Content (VWC % at 0.5% resolution).
     - Channel 2 (0x67): Soil & Canopy Temperature (-20 to 80 C at 0.1 C resolution).
     - Channel 3 (0x02): Soil Electrical Conductivity (EC Salinity in dS/m).
     - Channel 4 (0x00): Solenoid Valve Digital Actuation Status (OPEN / CLOSED).
   - Real-time agronomic hydration state machine:
     - `SATURATED`: Waterlogged soil (> 38% VWC) risking root hypoxia.
     - `FIELD_CAPACITY`: Optimal moisture balance (22% - 38% VWC).
     - `MILD_MOISTURE_DEFICIT`: Early depletion (14% - 22% VWC) requiring scheduled irrigation.
     - `WILTING_POINT`: Critical root moisture depletion (< 14% VWC) triggering emergency irrigation.

2. **`IrrigationPulseSchedulerEngine.kt`**:
   - Implements crop evapotranspiration model ($ET_c = ET_0 \times K_c$) for Saudi Date Palm orchards ($K_c = 0.90$) and greenhouse crops ($K_c = 0.75$).
   - Calculates root-zone volumetric deficit (Liters per tree basin).
   - Automatic Salinity Leaching Fraction ($LR$): increases irrigation quota by up to +30% when soil EC exceeds 2.2 dS/m to prevent salt accumulation in the root zone.
   - Pulsed irrigation timing algorithm: divides daily requirement into multi-stage cycles to eliminate runoff in desert sandy loam.
   - Encodes compact 5-byte downlink actuation frames for LoRaWAN Class C solenoid controllers.

3. **`HardwareSensorDriver.kt`**:
   - Asynchronous Kotlin Flow pipeline streaming synthesized Cayenne LPP telemetry frames with real-time agronomic triage and valve state monitoring.

4. **Automated Verification Suite (`NamaaSoilRegressionTest.kt`)**:
   - `testCheckoutCalculationAndTax`: Commercial transaction and VAT (15%) ledger verification.
   - `testCayenneLppEncodingAndDecoding`: Multi-channel binary frame packing and unpacking.
   - `testSoilHydrationStatusTransitions`: 4-stage agronomic moisture zone transitions.
   - `testCropWaterRequirementAndEvapotranspiration`: ETc and deficit volume calculations.
   - `testSalinityLeachingFractionAndPulseScheduling`: Leaching increment and downlink payload formatting.
   - `testHardwareSensorDriverIntegration`: Real-time telemetry flow emission.

---

### Build & Verification Commands

```bash
# Run unit regression test suite
./gradlew testDebugUnitTest

# Assemble production APK
./gradlew assembleDebug
```
