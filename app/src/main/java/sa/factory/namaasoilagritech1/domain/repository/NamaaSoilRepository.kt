package sa.factory.namaasoilagritech1.domain.repository

import sa.factory.namaasoilagritech1.model.NamaaSoilItem
import sa.factory.namaasoilagritech1.model.NamaaSoilOrder
import sa.factory.namaasoilagritech1.model.NamaaSoilTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NamaaSoilRepository {
    val catalogFlow: Flow<List<NamaaSoilItem>>
    val activeCart: StateFlow<List<NamaaSoilItem>>
    val telemetryFlow: Flow<NamaaSoilTelemetry>
    
    fun addToCart(item: NamaaSoilItem)
    fun removeFromCart(item: NamaaSoilItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): NamaaSoilOrder
    suspend fun refreshCatalog()
}
