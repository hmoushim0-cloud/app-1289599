package sa.factory.namaasoilagritech1.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.namaasoilagritech1.model.NamaaSoilItem

@Entity(tableName = "agritech_items_cache")
data class NamaaSoilEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): NamaaSoilItem = NamaaSoilItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
