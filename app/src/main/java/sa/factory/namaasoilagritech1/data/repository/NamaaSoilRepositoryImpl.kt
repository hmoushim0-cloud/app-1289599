package sa.factory.namaasoilagritech1.data.repository

import sa.factory.namaasoilagritech1.domain.repository.NamaaSoilRepository
import sa.factory.namaasoilagritech1.hardware.HardwareSensorDriver
import sa.factory.namaasoilagritech1.model.NamaaSoilItem
import sa.factory.namaasoilagritech1.model.NamaaSoilOrder
import sa.factory.namaasoilagritech1.model.NamaaSoilTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NamaaSoilRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : NamaaSoilRepository {

    private val _catalog = MutableStateFlow<List<NamaaSoilItem>>(listOf(
        NamaaSoilItem("1", "الباقة التنفيذية المعتمدة", "تكامل مباشر مع LoRaWAN Soil Moisture Node + Solenoid Valve Relays", 180.0, "معتمد", "الخدمات الرئيسية"),
        NamaaSoilItem("2", "خدمة المعالجة الفورية", "أداء أصيل بسرعة 60 إطار في الثانية", 95.0, "فوري", "المراقبة الحية"),
        NamaaSoilItem("3", "باقة الصيانة والدعم السحابي", "تشفير كامل للبيانات المحلية ومزامنة آمنة", 250.0, "شامل", "التقارير الضريبية")
    ))
    override val catalogFlow: Flow<List<NamaaSoilItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<NamaaSoilItem>>(emptyList())
    override val activeCart: StateFlow<List<NamaaSoilItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<NamaaSoilTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            NamaaSoilTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: NamaaSoilItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: NamaaSoilItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): NamaaSoilOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = NamaaSoilOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
