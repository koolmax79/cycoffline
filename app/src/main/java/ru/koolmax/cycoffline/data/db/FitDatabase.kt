package ru.koolmax.cycoffline.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1,
    entities = [FitSessionItem::class, DeviceInfo::class, SettingsItem::class],
    exportSchema = false)
abstract class FitDatabase: RoomDatabase() {
    abstract fun sessions(): FitSessionDAO
    abstract fun devices(): DevicesDAO
    abstract fun settings(): SettingsDAO

    companion object {
        //@Volatile
        private var instance: FitDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            create(context).also {
                instance = it
            }
        }

        private fun create(context: Context) =
            Room.databaseBuilder(context.applicationContext, FitDatabase::class.java, "fit.db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}
