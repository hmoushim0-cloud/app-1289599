package sa.factory.namaasoilagritech1.data.remote

import sa.factory.namaasoilagritech1.model.NamaaSoilItem
import sa.factory.namaasoilagritech1.model.NamaaSoilOrder

interface NamaaSoilApiService {
    suspend fun fetchCatalog(): List<NamaaSoilItem>
    suspend fun submitOrder(order: NamaaSoilOrder): Boolean
}
