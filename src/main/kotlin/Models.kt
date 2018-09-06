@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

import kotlinx.coroutines.experimental.*


data class AdUnit(val successful: Boolean, val tmax: Int, val delay: Int, val id: String = "", val fireCallbackIfLoaded: Boolean) : AdElement {

    override suspend fun load(listener: LoadingListener) {
        try {
            println("Start $this")
            withTimeout(tmax.toLong()) {
                delay(delay)
                println("Finish ${this@AdUnit}, successful: $successful")
                listener.onComplete(this@AdUnit, successful)
            }
        } catch (e: TimeoutCancellationException) {
            println("Failed ${this@AdUnit}, by tmax")
            listener.onComplete(this@AdUnit, false)
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
                        SurveyType.CONCURRENTLY -> loadConcurrently(listener)
                        SurveyType.CONSISTENTLY -> loadConsistently(listener)
                    }
                }
            } catch (ce: CancellationException) {
                println(ce.message)
            } finally {
                println("Finish ${this@AdList}, loaded: $loadedAd, failed: $failedAd")
            }
        }
    }

    private suspend fun loadConsistently(listener: LoadingListener) {
        val deferredCalculations = async {
            adElements.map {
                it.load(object : LoadingListener {
                    override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                        if (successfully) {
                            loadedAd.add(adUnit)
                        } else {
                            failedAd.add(adUnit)
                        }
                        listener.onComplete(adUnit, successfully)


                        if (stopGroupIfLoaded && successfully) {
                            throw CancellationException("Should stop loading ${this@AdList} after element was loaded")
                        }
                    }
                })
            }
        }

        deferredCalculations.await()
    }

    private suspend fun loadConcurrently(listener: LoadingListener) {
        val deferredCalculations = adElements.map {
            async {
                it.load(object : LoadingListener {
                    override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                        if (successfully) {
                            loadedAd.add(adUnit)
                        } else {
                            failedAd.add(adUnit)
                        }
                        listener.onComplete(adUnit, successfully)

                        if (stopGroupIfLoaded && successfully) {
                            throw CancellationException("Should stop loading ${this@AdList} after element was loaded")
                        }
                    }
                })
            }
        }

        deferredCalculations.forEach { it.await() }
    }

    override fun toString(): String {
        return "AdList(surveyType=$surveyType, adElements=$adElements, tmax=$tmax, id='$id', stopGroupIfLoaded=$stopGroupIfLoaded)"
    }
}

class AdRequest(private val adElement: AdElement) {
    private val loadedAd: MutableList<AdUnit> = mutableListOf()
    private val failedAd: MutableList<AdUnit> = mutableListOf()
    private var onLoadedCalled = false

    fun load(loadingListener: AdRequestLoadingListener) {
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully) {
                        loadedAd.add(adUnit)

                        if (!onLoadedCalled && adUnit.fireCallbackIfLoaded) {
                            onLoadedCalled = true
                            loadingListener.onLoaded(adUnit)
                        }
                    } else {
                        failedAd.add(adUnit)
                    }

                }
            })

            println("Ad request completed, loaded: $loadedAd, failed: $failedAd\"")
            loadingListener.onCompleted(loadedAd, failedAd)
        }
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

    private var surveyType: SurveyType = SurveyType.CONSISTENTLY    // GROUP
    private var stopGroupIfLoaded: Boolean = false                  // UNIT
    private var fireCallbackIfLoaded: Boolean = false               // UNIT

    fun id(id: String) = apply { this.id = id }
    fun delay(delay: Int) = apply { this.delay = delay }
    fun tmax(tmax: Int) = apply { this.tmax = tmax }
    fun successful(successful: Boolean) = apply { this.successful = successful }
    fun addAdElement(adElement: AdElement) = apply { this.adElements.add(adElement) }
    fun surveyType(surveyType: SurveyType) = apply { this.surveyType = surveyType }
    fun stopGroupIfLoaded() = apply { this.stopGroupIfLoaded = true }
    fun fireCallbackIfLoaded() = apply { this.fireCallbackIfLoaded = true }

    fun build(): AdElement = if (adElements.isNotEmpty()) {
        AdList(surveyType, adElements, tmax, id, stopGroupIfLoaded)
    } else {
        AdUnit(successful, tmax, delay, id, fireCallbackIfLoaded)
    }
}