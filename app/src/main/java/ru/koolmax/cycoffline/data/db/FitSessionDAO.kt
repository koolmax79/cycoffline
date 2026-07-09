package ru.koolmax.cycoffline.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.koolmax.cycoffline.data.FitStatisticItem

@Dao
interface FitSessionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(session: FitSessionItem)

    @Query("delete from session where file_name like :fileName")
    suspend fun delete(fileName: String)

    @Query("select * from session order by start_time desc")
    fun all(): Flow<List<FitSessionItem>>

    @Query("select * from session where start_time between :start and :end order by start_time")
    suspend fun allByInterval(start: Int, end: Int): List<FitSessionItem>

    @Query("select * from session where file_name like :fileName")
    suspend fun getSession(fileName: String): FitSessionItem

    @Query("select count(*)>0 from session where file_name like :fileName")
    suspend fun contains(fileName: String): Int

    @Query("select min(start_time) from session limit 1")
    suspend fun minStartTime(): Int?

    @Query("select max(start_time) from session limit 1")
    suspend fun maxStartTime(): Int?

    @Query("select min(start_time) as min, max(start_time) as max from session limit 1")
    suspend fun rangeStartTime(): MinMax?

    @Query("select min(start_time) as min, max(start_time) as max from session where start_time between :start and :end limit 1")
    suspend fun rangeStartTime(start: Int, end: Int): MinMax?

    @Query("select count(*) count from session where displayed = 0")
    suspend fun getCountNotDisplayed(): Int

    @Update
    suspend fun update(session: FitSessionItem)
    //@Query("select * from session")
    //suspend fun observeAll(): Flow<List<FitSession>>
}
