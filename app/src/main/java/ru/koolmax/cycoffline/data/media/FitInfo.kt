package ru.koolmax.cycoffline.data.media

import android.util.Log
import com.garmin.fit.ActivityMesg
import com.garmin.fit.ActivityMesgListener
import com.garmin.fit.FileIdMesg
import com.garmin.fit.FileIdMesgListener
import com.garmin.fit.MesgBroadcaster
import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import com.garmin.fit.SessionMesg
import com.garmin.fit.SessionMesgListener
import kotlin.math.max
import kotlin.math.min

class FitInfo: RecordMesgListener, FileIdMesgListener, SessionMesgListener, ActivityMesgListener {
    var fileId: FileIdMesg? = null
        private set;
    var session: SessionMesg? = null
        private set;
    private var activity: ActivityMesg? = null
        private set;
    var timestampStart = Long.MAX_VALUE
        private set
    private var timestampEnd = Long.MIN_VALUE

    val timeCount: Int
        get() = (timestampEnd - timestampStart + 1).toInt()

    override fun onMesg(p0: RecordMesg?) {
        if(p0 == null) return;
        timestampStart = min(timestampStart, p0.timestamp.timestamp)
        timestampEnd = max(timestampEnd, p0.timestamp.timestamp)
        //Log.i("cycoffline1", p0.timestamp.timestamp.toString())
    }

    override fun onMesg(p0: FileIdMesg?) {
        fileId = p0
    }

    override fun onMesg(p0: SessionMesg?) {
        session = p0
    }

    override fun onMesg(p0: ActivityMesg?) {
        activity = p0
    }
}

fun MesgBroadcaster.addListeners(listener: FitInfo) {
    this.addListener(listener as RecordMesgListener)
    this.addListener(listener as FileIdMesgListener)
    this.addListener(listener as SessionMesgListener)
    this.addListener(listener as ActivityMesgListener)
}