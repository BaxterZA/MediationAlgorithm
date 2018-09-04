@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

import org.junit.Assert.*

class Tests {

    @Test
    fun adUnit_onComplete_withLoaded() {

        val startAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        runBlocking {
            startAdElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(startAdElement), loaded)
                    assertTrue(failed.isEmpty())
                }
            })
        }
    }

    @Test
    fun adUnit_onComplete_withFailed() {

        val startAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        runBlocking {
            startAdElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertTrue(loaded.isEmpty())
                    assertEquals(listOf(startAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adUnit_onComplete_withFailedByTMax() {

        val startAdElement: AdElement = AdElementBuilder().successful(true).delay(500).tmax(200).build()
        runBlocking {
            startAdElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertTrue(loaded.isEmpty())
                    assertEquals(listOf(startAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adList_onComplete_withLoaded() {

        val innerAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(innerAdElement).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(innerAdElement), loaded)
                    assertTrue(failed.isEmpty())
                }
            })
        }
    }

    @Test
    fun adList_onComplete_withFailed() {

        val innerAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(innerAdElement).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertTrue(loaded.isEmpty())
                    assertEquals(listOf(innerAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adList_onComplete_withTwoLoaded() {

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(firstAdElement, secondAdElement), loaded)
                    assertTrue(failed.isEmpty())
                }
            })
        }
    }

    @Test
    fun adList_onComplete_withTwoFailed() {

        val firstAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(firstAdElement, secondAdElement), failed)
                    assertTrue(loaded.isEmpty())
                }
            })
        }
    }

    @Test
    fun adList_onComplete_oneLoadedOneFailed() {

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(firstAdElement), loaded)
                    assertEquals(listOf(secondAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adList_onComplete_oneLoadedOneFailedByTMax() {

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(500).tmax(300).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(firstAdElement), loaded)
                    assertEquals(listOf(secondAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adList_withInnerList_onComplete_consistently() {

        val innerFirstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val innerSecondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val innerAdElement: AdElement = AdElementBuilder().addAdElement(innerFirstAdElement).addAdElement(innerSecondAdElement).surveyType(SurveyType.CONSISTENTLY).build()

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).addAdElement(innerAdElement).surveyType(SurveyType.CONSISTENTLY).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(firstAdElement, innerFirstAdElement), loaded)
                    assertEquals(listOf(secondAdElement, innerSecondAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adList_withInnerList_onComplete_concurrently() {

        val innerFirstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val innerSecondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val innerAdElement: AdElement = AdElementBuilder().addAdElement(innerFirstAdElement).addAdElement(innerSecondAdElement).surveyType(SurveyType.CONCURRENTLY).build()

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).addAdElement(innerAdElement).surveyType(SurveyType.CONCURRENTLY).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(loaded: List<AdElement>, failed: List<AdElement>) {
                    assertEquals(listOf(firstAdElement, innerFirstAdElement), loaded)
                    assertEquals(listOf(secondAdElement, innerSecondAdElement), failed)
                }
            })
        }
    }

    @Test
    fun adElementBuilder_AdUnit() {
        val successfulAdElement = AdElementBuilder().id("id").delay(300).successful(true).build()

        assert(successfulAdElement is AdUnit)
        successfulAdElement as AdUnit
        assertEquals("id", successfulAdElement.id)
        assertEquals(300, successfulAdElement.delay)
        assertEquals(true, successfulAdElement.successful)


        val unSuccessfulAdElement = AdElementBuilder().id("id_1").delay(500).successful(false).build()

        assert(unSuccessfulAdElement is AdUnit)
        unSuccessfulAdElement as AdUnit
        assertEquals("id_1", unSuccessfulAdElement.id)
        assertEquals(500, unSuccessfulAdElement.delay)
        assertEquals(false, unSuccessfulAdElement.successful)
    }

    @Test
    fun adElementBuilder_AdListConsistent() {
        val innerAdElement = AdElementBuilder().id("id_inner").delay(300).successful(true).build()
        val adElement = AdElementBuilder().id("id").surveyType(SurveyType.CONSISTENTLY).delay(500).addAdElement(innerAdElement).build()

        assert(adElement is AdListConsistent)
        adElement as AdListConsistent
        assertEquals("id", adElement.id)
        assertEquals(500, adElement.tmax)
        assertEquals(1, adElement.adElements.size)
        assertEquals(innerAdElement, adElement.adElements[0])
    }

    @Test
    fun adElementBuilder_AdListConcurrent() {
        val innerAdElement = AdElementBuilder().id("id_inner").delay(300).successful(true).build()
        val adElement = AdElementBuilder().id("id").surveyType(SurveyType.CONCURRENTLY).delay(500).addAdElement(innerAdElement).build()

        assert(adElement is AdListConcurrent)
        adElement as AdListConcurrent
        assertEquals("id", adElement.id)
        assertEquals(500, adElement.tmax)
        assertEquals(1, adElement.adElements.size)
        assertEquals(innerAdElement, adElement.adElements[0])
    }
}