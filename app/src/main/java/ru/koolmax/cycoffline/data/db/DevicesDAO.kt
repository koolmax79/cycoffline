package ru.koolmax.cycoffline.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.koolmax.cycoffline.data.db.DeviceInfo

@Dao
interface DevicesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(device: DeviceInfo)

    @Query("delete from devices where address like :address")
    suspend fun delete(address: String)

    @Query("select * from devices")
    fun all(): Flow<List<DeviceInfo>>
}