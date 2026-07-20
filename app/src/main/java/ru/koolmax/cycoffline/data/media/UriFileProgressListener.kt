package ru.koolmax.cycoffline.data.media

open class UriFileProgressListener {
    open fun onBegin() {}
    open fun onStep(step: Int) {}
    open fun onFinish() {}
}