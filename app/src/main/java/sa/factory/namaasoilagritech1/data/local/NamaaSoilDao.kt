package sa.factory.namaasoilagritech1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NamaaSoilDao {
    @Query("SELECT * FROM agritech_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<NamaaSoilEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<NamaaSoilEntity>)

    @Query("DELETE FROM agritech_items_cache")
    suspend fun clearAll()
}
