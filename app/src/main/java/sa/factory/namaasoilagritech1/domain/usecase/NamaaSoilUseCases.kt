package sa.factory.namaasoilagritech1.domain.usecase

import sa.factory.namaasoilagritech1.domain.repository.NamaaSoilRepository
import sa.factory.namaasoilagritech1.model.NamaaSoilItem
import sa.factory.namaasoilagritech1.model.NamaaSoilOrder
import sa.factory.namaasoilagritech1.model.NamaaSoilTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: NamaaSoilRepository) {
    operator fun invoke(): Flow<List<NamaaSoilItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: NamaaSoilRepository) {
    val cart = repository.activeCart
    fun add(item: NamaaSoilItem) = repository.addToCart(item)
    fun remove(item: NamaaSoilItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: NamaaSoilRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): NamaaSoilOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: NamaaSoilRepository) {
    operator fun invoke(): Flow<NamaaSoilTelemetry> = repository.telemetryFlow
}
