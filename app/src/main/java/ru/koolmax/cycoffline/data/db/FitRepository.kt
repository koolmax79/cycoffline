package ru.koolmax.cycoffline.data.db

import javax.inject.Inject
import javax.inject.Singleton

//import javax.inject.Inject

@Singleton
class FitRepository @Inject constructor(private val db: FitDatabase) {
    suspend fun add(fit: FitSessionItem) = db.sessions().add(fit)
    suspend fun delete(fileName: String) = db.sessions().delete(fileName)
    fun all() = db.sessions().all()
    suspend fun allByInterval(start: Int, end: Int) = db.sessions().allByInterval(start, end)
    suspend fun getSession(fileName: String) = db.sessions().getSession(fileName)

    suspend fun contains(fileName: String) = db.sessions().contains(fileName)>0

    suspend fun minStartTime() = db.sessions().minStartTime()?.let { return@let FitConverter().toLocalDateTime(it) }
    suspend fun maxStartTime() = db.sessions().maxStartTime()?.let { return@let FitConverter().toLocalDateTime(it) }
    suspend fun rangeStartTime() = db.sessions().rangeStartTime()?.let { return@let Pair(FitConverter().toLocalDateTime(it.min), FitConverter().toLocalDateTime(it.max)) }
    suspend fun rangeStartTime(start: Int, end: Int) = db.sessions().rangeStartTime(start, end)?.let { return@let Pair(FitConverter().toLocalDateTime(it.min), FitConverter().toLocalDateTime(it.max)) }

    suspend fun getCountNotDisplayed() = db.sessions().getCountNotDisplayed()

    suspend fun update(session: FitSessionItem) = db.sessions().update(session)

    //fun allByIntervalSync(start: Int, end: Int) = db.sessions().allByIntervalSync(start, end)

    //fun add(uri: Uri, context: Context) {

    //}
}
