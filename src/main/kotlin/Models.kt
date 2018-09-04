@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

import kotlinx.coroutines.experimental.*


data class AdUnit(val successful: Boolean, val tmax: Int, val delay: Int, val id: String = "") : AdElement {
    private val loadedAd: MutableList<AdElement> = mutableListOf()
    private val failedAd: MutableList<AdElement> = mutableListOf()

    override suspend fun load(listener: LoadingListener) {
        try {
            withTimeout(tmax.toLong()) {
                println("Start $this")
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


class AdListConsistent(val adElements: List<AdElement>, val tmax: Int = 200, val id: String = "") : AdElement {
    private val loadedAd: MutableList<AdElement> = mutableListOf()
    private val failedAd: MutableList<AdElement> = mutableListOf()

    override suspend fun load(listener: LoadingListener) {
        println("Start $this")
        runBlocking {
            val deferredCalculations = async {
                adElements.map {
                    it.load(object : LoadingListener {
                        override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                            loadedAd.addAll(loaded)
                            failedAd.addAll(failed)
                        }
                    })
                }
            }

            deferredCalculations.await()

            println("Finish ${this@AdListConsistent}, loaded: $loadedAd, failed: $failedAd")
            listener.onComplete(loadedAd, failedAd)
        }
    }
}

class AdListConcurrent(val adElements: List<AdElement>, val tmax: Int = 200, val id: String = "") : AdElement {
    private val loadedAd: MutableList<AdElement> = mutableListOf()
    private val failedAd: MutableList<AdElement> = mutableListOf()

    override suspend fun load(listener: LoadingListener) {
        println("Start $this")
        runBlocking {
            val deferredCalculations = adElements.map {
                async {
                    it.load(object : LoadingListener {
                        override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                            loadedAd.addAll(loaded)
                            failedAd.addAll(failed)
                        }
                    })
                }
            }

            deferredCalculations.forEach { it.await() }

            println("Finish ${this@AdListConcurrent}, loaded: $loadedAd, failed: $failedAd")
            listener.onComplete(loadedAd, failedAd)
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
    private var surveyType: SurveyType = SurveyType.CONSISTENTLY

    fun id(id: String) = apply { this.id = id }
    fun delay(delay: Int) = apply { this.delay = delay }
    fun tmax(tmax: Int) = apply { this.tmax = tmax }
    fun successful(successful: Boolean) = apply { this.successful = successful }
    fun addAdElement(adElement: AdElement) = apply { this.adElements.add(adElement) }
    fun surveyType(surveyType: SurveyType) = apply { this.surveyType = surveyType }

    fun build(): AdElement = if (adElements.isNotEmpty()) {
        when (surveyType) {
            SurveyType.CONSISTENTLY -> AdListConsistent(adElements, delay, id)
            SurveyType.CONCURRENTLY -> AdListConcurrent(adElements, delay, id)
        }
    } else {
        AdUnit(successful, tmax, delay, id)
    }
}