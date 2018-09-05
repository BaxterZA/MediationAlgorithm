@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

interface LoadingListener {
    fun onComplete(adUnit: AdUnit, successfully: Boolean)
}

interface AdRequestLoadingListener {
    fun onLoaded(adUnit: AdUnit)
    fun onCompleted(loaded: List<AdUnit>, failed: List<AdUnit>)
}

interface AdElement {
    suspend fun load(listener: LoadingListener)
}