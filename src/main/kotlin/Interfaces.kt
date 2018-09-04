@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

interface LoadingListener {
    fun onComplete(loaded: List<AdElement>, failed: List<AdElement>)
}


interface AdElement {
    suspend fun load(listener: LoadingListener)
}