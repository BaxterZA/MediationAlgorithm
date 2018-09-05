@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

import kotlinx.coroutines.experimental.*


data class AdUnit(val successful: Boolean, val tmax: Int, val delay: Int, val id: String = "") : AdElement {
    private val loadedAd: MutableList<AdElement> = mutableListOf()
    private val failedAd: MutableList<AdElement> = mutableListOf()

    override suspend fun load(listener: LoadingListener) {
        try {
            println("Start $this")
            withTimeout(tmax.toLong()) {
                delay(delay)
                when (successful) {
                    true -> {
                        loadedAd.add(this@AdUnit)
                    }
                    false -> {
                        failedAd.add(this@AdUnit)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            failedAd.add(this@AdUnit)
            println("Finish ${this@AdUnit}, by tmax")
        } finally {
            println("Finish $this, loaded: $loadedAd, failed: $failedAd")
            listener.onComplete(loadedAd, failedAd)

        }
    }
}


class AdList(val surveyType: SurveyType, val adElements: List<AdElement>, val tmax: Int = 200, val id: String = "", val stopGroupIfLoaded: Boolean) : AdElement {
    private val loadedAd: MutableList<AdElement> = mutableListOf()
    private val failedAd: MutableList<AdElement> = mutableListOf()

    override suspend fun load(listener: LoadingListener) {
        println("Start $this")
        runBlocking {
            try {
                withTimeout(tmax.toLong()) {
                    when (surveyType) {
                        SurveyType.CONCURRENTLY -> loadConcurrently()
                        SurveyType.CONSISTENTLY -> loadConsistently()
                    }
                }
            } catch (ce: CancellationException) {
                println(ce.message)
            } finally {
                println("Finish ${this@AdList}, loaded: $loadedAd, failed: $failedAd")
                listener.onComplete(loadedAd, failedAd)
            }
        }
    }

    private suspend fun loadConsistently() {
        val deferredCalculations = async {
            adElements.map {
                it.load(object : LoadingListener {
                    override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                        loadedAd.addAll(loaded)
                        failedAd.addAll(failed)

                        if (stopGroupIfLoaded && loaded.isNotEmpty()) {
                            throw CancellationException("Should stop loading ${this@AdList} after element was loaded")
                        }
                    }
                })
            }
        }

        deferredCalculations.await()
    }

    private suspend fun loadConcurrently() {
        val deferredCalculations = adElements.map {
            async {
                it.load(object : LoadingListener {
                    override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                        loadedAd.addAll(loaded)
                        failedAd.addAll(failed)

                        if (stopGroupIfLoaded && loaded.isNotEmpty()) {
                            throw CancellationException("Should stop loading ${this@AdList} after element was loaded")
                        }
                    }
                })
            }
        }
        deferredCalculations.forEach { it.await() }
    }

    override fun toString(): String {
        return "AdListConsistent(surveyType=$surveyType, adElements=$adElements, tmax=$tmax, id='$id', stopGroupIfLoaded=$stopGroupIfLoaded)"
    }
}


enum class SurveyType {
    CONSISTENTLY, CONCURRENTLY
}

class AdElementBuilder {
    private var id: String = ""
    private var delay: Int = 100
    private var tmax: Int = 300
    private var successful: Boolean = false
    private var adElements: MutableList<AdElement> = mutableListOf()
    private var surveyType: SurveyType = SurveyType.CONSISTENTLY
    private var stopGroupIfLoaded: Boolean = false

    fun id(id: String) = apply { this.id = id }
    fun delay(delay: Int) = apply { this.delay = delay }
    fun tmax(tmax: Int) = apply { this.tmax = tmax }
    fun successful(successful: Boolean) = apply { this.successful = successful }
    fun addAdElement(adElement: AdElement) = apply { this.adElements.add(adElement) }
    fun surveyType(surveyType: SurveyType) = apply { this.surveyType = surveyType }
    fun stopGroupIfLoaded() = apply { this.stopGroupIfLoaded = true }

    fun build(): AdElement = if (adElements.isNotEmpty()) {
        AdList(surveyType, adElements, tmax, id, stopGroupIfLoaded)
    } else {
        AdUnit(successful, tmax, delay, id)
    }
}