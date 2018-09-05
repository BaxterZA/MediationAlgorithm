@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Tests {

    @Test
    fun adUnit_onComplete_withLoaded() {

        val startAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        runBlocking {
            startAdElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(startAdElement, adUnit)
                    assertTrue(successfully)
                }
            })
        }
    }

    @Test
    fun adUnit_onComplete_withFailedByTMax() {

        val startAdElement: AdElement = AdElementBuilder().successful(true).delay(500).tmax(200).build()
        runBlocking {
            startAdElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(startAdElement, adUnit)
                    assertFalse(successfully)
                }
            })
        }
    }

    @Test
    fun adUnit_onComplete_withFailed() {

        val startAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        runBlocking {
            startAdElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(startAdElement, adUnit)
                    assertFalse(successfully)
                }
            })
        }
    }

    @Test
    fun adListConsistent_onComplete_withLoaded() {

        val innerAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(innerAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(innerAdElement, adUnit)
                    assertTrue(successfully)
                }
            })
        }
    }

    @Test
    fun adListConcurrent_onComplete_withLoaded() {

        val innerAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(innerAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(adUnit, adUnit)
                    assertTrue(successfully)
                }
            })
        }
    }

    @Test
    fun adListConsistent_onComplete_withFailed() {

        val innerAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(innerAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(adUnit, adUnit)
                    assertFalse(successfully)
                }
            })
        }
    }

    @Test
    fun adListConcurrent_onComplete_withFailed() {

        val innerAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(innerAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(adUnit, adUnit)
                    assertFalse(successfully)
                }
            })
        }
    }

    @Test
    fun adListConsistent_onComplete_withTwoLoaded() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && (firstAdElement == adUnit && secondAdElement == adUnit)) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_onComplete_withTwoLoaded() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && (firstAdElement == adUnit && secondAdElement == adUnit)) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConsistent_onComplete_withTwoFailed() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (!successfully && (firstAdElement == adUnit && secondAdElement == adUnit)) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_onComplete_withTwoFailed() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (!successfully && (firstAdElement == adUnit && secondAdElement == adUnit)) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConsistent_onComplete_oneLoadedOneFailed() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && firstAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else if (!successfully && secondAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_onComplete_oneLoadedOneFailed() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && firstAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else if (!successfully && secondAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConsistent_onComplete_oneLoadedOneFailedByTMax() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(500).tmax(300).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && firstAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else if (!successfully && secondAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_onComplete_oneLoadedOneFailedByTMax() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(500).tmax(300).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && firstAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else if (!successfully && secondAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConsistent_onComplete_oneLoadedAndStop() {

        val countDownLatch = CountDownLatch(1)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).id("first").build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).id("second").build()
        val adElement: AdElement = AdElementBuilder()
                .addAdElement(firstAdElement)
                .addAdElement(secondAdElement)
                .surveyType(SurveyType.CONSISTENTLY)
                .tmax(2000)
                .stopGroupIfLoaded()
                .build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if (successfully && firstAdElement == adUnit) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_onComplete_oneLoadedAndStop() {

        val countDownLatch = CountDownLatch(1)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(100).tmax(300).id("first").build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).id("second").build()
        val adElement: AdElement = AdElementBuilder()
                .addAdElement(firstAdElement)
                .addAdElement(secondAdElement)
                .surveyType(SurveyType.CONCURRENTLY)
                .tmax(2000)
                .stopGroupIfLoaded()
                .build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertTrue(successfully)
                    countDownLatch.countDown()
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConsistent_onComplete_stopListByTMax() {

        val countDownLatch = CountDownLatch(1)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).id("first").build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(300).id("second").build()
        val adElement: AdElement = AdElementBuilder()
                .addAdElement(firstAdElement)
                .addAdElement(secondAdElement)
                .surveyType(SurveyType.CONSISTENTLY)
                .tmax(300)
                .build()

        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(firstAdElement, adUnit)
                    assertTrue(successfully)
                    countDownLatch.countDown()
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_onComplete_stopListByTMax() {

        val countDownLatch = CountDownLatch(1)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(100).tmax(500).id("first").build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(300).tmax(500).id("second").build()
        val adElement: AdElement = AdElementBuilder()
                .addAdElement(firstAdElement)
                .addAdElement(secondAdElement)
                .surveyType(SurveyType.CONCURRENTLY)
                .tmax(200)
                .build()

        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    assertEquals(firstAdElement, adUnit)
                    assertTrue(successfully)
                    countDownLatch.countDown()
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConsistent_withInnerList_onComplete() {

        val countDownLatch = CountDownLatch(4)
        val innerFirstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val innerSecondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val innerAdElement: AdElement = AdElementBuilder().addAdElement(innerFirstAdElement).addAdElement(innerSecondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).addAdElement(innerAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if ((successfully && (adUnit == firstAdElement || adUnit == innerFirstAdElement)) ||
                            (!successfully && (adUnit == secondAdElement || adUnit == innerSecondAdElement))) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun adListConcurrent_withInnerList_onComplete() {

        val countDownLatch = CountDownLatch(4)
        val innerFirstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val innerSecondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val innerAdElement: AdElement = AdElementBuilder().addAdElement(innerFirstAdElement).addAdElement(innerSecondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()

        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).addAdElement(innerAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        runBlocking {
            adElement.load(object : LoadingListener {
                override fun onComplete(adUnit: AdUnit, successfully: Boolean) {
                    if ((successfully && (adUnit == firstAdElement || adUnit == innerFirstAdElement)) ||
                            (!successfully && (adUnit == secondAdElement || adUnit == innerSecondAdElement))) {
                        countDownLatch.countDown()
                    } else {
                        fail()
                    }
                }
            })
        }

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
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
        val firstAdElement = AdElementBuilder().id("id_first").delay(300).successful(true).build()
        val secondAdElement = AdElementBuilder().id("id_second").delay(300).successful(true).build()
        val adElement = AdElementBuilder()
                .id("id")
                .surveyType(SurveyType.CONSISTENTLY)
                .tmax(2000)
                .addAdElement(firstAdElement)
                .addAdElement(secondAdElement)
                .stopGroupIfLoaded()
                .build()

        assert(adElement is AdList)
        adElement as AdList
        assertEquals(SurveyType.CONSISTENTLY, adElement.surveyType)
        assertEquals("id", adElement.id)
        assertEquals(2000, adElement.tmax)
        assertEquals(2, adElement.adElements.size)
        assertEquals(firstAdElement, adElement.adElements[0])
        assertEquals(secondAdElement, adElement.adElements[1])
        assertTrue(adElement.stopGroupIfLoaded)
    }

    @Test
    fun adElementBuilder_AdListConcurrent() {
        val firstAdElement = AdElementBuilder().id("id_first").delay(300).successful(true).build()
        val secondAdElement = AdElementBuilder().id("id_second").delay(300).successful(true).build()
        val adElement = AdElementBuilder()
                .id("id")
                .surveyType(SurveyType.CONCURRENTLY)
                .tmax(2000)
                .addAdElement(firstAdElement)
                .addAdElement(secondAdElement)
                .stopGroupIfLoaded()
                .build()

        assert(adElement is AdList)
        adElement as AdList
        assertEquals(SurveyType.CONCURRENTLY, adElement.surveyType)
        assertEquals("id", adElement.id)
        assertEquals(2000, adElement.tmax)
        assertEquals(2, adElement.adElements.size)
        assertEquals(firstAdElement, adElement.adElements[0])
        assertEquals(secondAdElement, adElement.adElements[1])
        assertTrue(adElement.stopGroupIfLoaded)
    }

    @Test
    fun adRequestConsistent_twoLoaded_callBackOnFirst() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(500).fireCallbackIfLoaded().build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(200).tmax(500).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        val adRequest = AdRequest(adElement)
        adRequest.load(object : AdRequestLoadingListener {
            override fun onLoaded(adUnit: AdUnit) {
                if (firstAdElement == adUnit) {
                    countDownLatch.countDown()
                } else {
                    fail()
                }
            }

            override fun onCompleted(loaded: List<AdUnit>, failed: List<AdUnit>) {
                assertEquals(2, loaded.size)
                assertTrue(loaded.contains(firstAdElement))
                assertTrue(loaded.contains(secondAdElement))
                assertTrue(failed.isEmpty())
                countDownLatch.countDown()
            }
        })

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))

    }

    @Test
    fun adRequestConcurrent_twoLoaded_callBackOnFirst() {

        val countDownLatch = CountDownLatch(2)
        val firstAdElement: AdElement = AdElementBuilder().successful(true).delay(100).tmax(500).fireCallbackIfLoaded().build()
        val secondAdElement: AdElement = AdElementBuilder().successful(true).delay(300).tmax(500).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        val adRequest = AdRequest(adElement)
        adRequest.load(object : AdRequestLoadingListener {
            override fun onLoaded(adUnit: AdUnit) {
                if (firstAdElement == adUnit) {
                    countDownLatch.countDown()
                } else {
                    fail()
                }
            }

            override fun onCompleted(loaded: List<AdUnit>, failed: List<AdUnit>) {
                assertEquals(2, loaded.size)
                assertTrue(loaded.contains(firstAdElement))
                assertTrue(loaded.contains(secondAdElement))
                assertTrue(failed.isEmpty())
                countDownLatch.countDown()
            }
        })

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))

    }

    @Test
    fun adRequestConsistent_twoFailed() {

        val countDownLatch = CountDownLatch(1)
        val firstAdElement: AdElement = AdElementBuilder().successful(false).delay(200).tmax(500).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(200).tmax(500).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        val adRequest = AdRequest(adElement)
        adRequest.load(object : AdRequestLoadingListener {
            override fun onLoaded(adUnit: AdUnit) {
                fail()
            }

            override fun onCompleted(loaded: List<AdUnit>, failed: List<AdUnit>) {
                assertEquals(2, failed.size)
                assertTrue(failed.contains(firstAdElement))
                assertTrue(failed.contains(secondAdElement))
                assertTrue(loaded.isEmpty())
                countDownLatch.countDown()
            }
        })

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))

    }

    @Test
    fun adRequestConcurrent_twoFailed() {

        val countDownLatch = CountDownLatch(1)
        val firstAdElement: AdElement = AdElementBuilder().successful(false).delay(100).tmax(500).build()
        val secondAdElement: AdElement = AdElementBuilder().successful(false).delay(300).tmax(500).build()
        val adElement: AdElement = AdElementBuilder().addAdElement(firstAdElement).addAdElement(secondAdElement).surveyType(SurveyType.CONCURRENTLY).tmax(2000).build()
        val adRequest = AdRequest(adElement)
        adRequest.load(object : AdRequestLoadingListener {
            override fun onLoaded(adUnit: AdUnit) {
                fail()
            }

            override fun onCompleted(loaded: List<AdUnit>, failed: List<AdUnit>) {
                assertEquals(2, failed.size)
                assertTrue(failed.contains(firstAdElement))
                assertTrue(failed.contains(secondAdElement))
                assertTrue(loaded.isEmpty())
                countDownLatch.countDown()
            }
        })

        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))

    }

    @Test
    fun adRequest_preCacheListWithSecondLoaded_oneConcurrentWithCallback_listConsistentEveryoneWithCallback() {

        val countDownLatch = CountDownLatch(2)

        val firstPreCacheAdElement: AdElement = AdElementBuilder().successful(false).id("firstPreCacheAdElement").delay(200).tmax(500).build()
        val secondPreCacheAdElement: AdElement = AdElementBuilder().successful(true).id("secondPreCacheAdElement").delay(200).tmax(500).build()
        val preCache: AdElement = AdElementBuilder().addAdElement(firstPreCacheAdElement).addAdElement(secondPreCacheAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()

        val concurrentAdElement: AdElement = AdElementBuilder().successful(true).id("concurrentAdElement").delay(1000).tmax(5000).fireCallbackIfLoaded().build()

        val firstListAdElement: AdElement = AdElementBuilder().successful(false).id("firstListAdElement").delay(200).tmax(500).fireCallbackIfLoaded().build()
        val secondListAdElement: AdElement = AdElementBuilder().successful(true).id("secondListAdElement").delay(200).tmax(500).fireCallbackIfLoaded().build()
        val list: AdElement = AdElementBuilder().addAdElement(firstListAdElement).addAdElement(secondListAdElement).surveyType(SurveyType.CONSISTENTLY).tmax(2000).build()
        val mainAds: AdElement = AdElementBuilder().addAdElement(concurrentAdElement).addAdElement(list).surveyType(SurveyType.CONCURRENTLY).tmax(5000).build()

        val ads: AdElement = AdElementBuilder().addAdElement(preCache).addAdElement(mainAds).surveyType(SurveyType.CONSISTENTLY).tmax(5000).build()

        val adRequest = AdRequest(ads)
        adRequest.load(object : AdRequestLoadingListener {
            override fun onLoaded(adUnit: AdUnit) {
                if (secondListAdElement == adUnit) {
                    countDownLatch.countDown()
                } else {
                    fail()
                }
            }

            override fun onCompleted(loaded: List<AdUnit>, failed: List<AdUnit>) {
                assertEquals(3, loaded.size)
                assertTrue(loaded.contains(secondPreCacheAdElement))
                assertTrue(loaded.contains(secondListAdElement))
                assertTrue(loaded.contains(concurrentAdElement))

                assertEquals(2, failed.size)
                assertTrue(failed.contains(firstPreCacheAdElement))
                assertTrue(failed.contains(firstListAdElement))
                countDownLatch.countDown()
            }
        })

        assertTrue(countDownLatch.await(2000, TimeUnit.MILLISECONDS))

    }


}