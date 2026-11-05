package sa.factory.namaasoilagritech1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NamaaSoilEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agritechDao(): NamaaSoilDao
}
