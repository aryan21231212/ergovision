package com.ergovision.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ergovision.app.data.dao.HazardEventDao
import com.ergovision.app.data.entity.HazardEvent

@Database(entities = [HazardEvent::class], version = 1, exportSchema = false)
abstract class HazardDatabase : RoomDatabase() {
    abstract fun hazardEventDao(): HazardEventDao

    companion object {
        @Volatile
        private var INSTANCE: HazardDatabase? = null

        fun getInstance(context: Context): HazardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HazardDatabase::class.java,
                    "ergovision_hazards.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
