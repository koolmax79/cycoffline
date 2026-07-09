package ru.koolmax.cycoffline.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(value: SettingsItem)

    @Query("select * from settings")
    fun all(): List<SettingsItem>

    @Query("select value from settings where key like :key limit 1")
    fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setValue(itm: SettingsItem)
}