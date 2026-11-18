package ru.koolmax.cycoffline.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE session ADD COLUMN sub_sport INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE session ADD COLUMN avg_cadence INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE session ADD COLUMN avg_power INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE session ADD COLUMN max_cadence INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE session ADD COLUMN max_power INTEGER DEFAULT NULL")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE session ADD COLUMN total_calories INTEGER DEFAULT NULL")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE session ADD COLUMN maxHRUser INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE session ADD COLUMN weightUser INTEGER DEFAULT NULL")
    }
}

@Database(version = 5,
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
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .build()
    }
}
