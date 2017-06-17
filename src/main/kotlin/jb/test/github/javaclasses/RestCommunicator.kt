package jb.test.github.javaclasses

import com.github.kittinunf.fuel.httpGet
import mu.KotlinLogging
import javax.xml.ws.http.HTTPException

/**
 * Created by ksenia on 6/7/17.
 */

class RestCommunicator {
    
    var prefix = "https://api.github.com"
    var token = ""
            constructor(token : String) {
        this.token = "token " + token
    }
    var requestsCnt = 0
    val maxTry = 3
    val exceptionSleepTime = 10000

    private val logger = KotlinLogging.logger {}


    @Throws(HTTPException::class)
    fun getHttpResult(stringRequest : String)
            : ResponseFeatures {
        val fullRestRequest = listOf(prefix, stringRequest).joinToString("/")
        var repeat = true
        var tryCount = 0
        while (repeat) {
            try {
                val (request, response, result) = fullRestRequest
                        .httpGet()
                        .header(mapOf("Authorization" to token))
                        .responseString()

                if (response.httpStatusCode != 200) {
                    logger.info { request.toString() }
                    logger.info { "Status code:" + response.httpStatusCode }
                    logger.info { "Sent requests $requestsCnt" }
                    throw HTTPException(response.httpStatusCode)

                }
                requestsCnt += 1
                val link = response.httpResponseHeaders.get("Link")
                return ResponseFeatures(result.get(), link.orEmpty())
            } catch (e: HTTPException) {
                tryCount += 1
                if (tryCount < maxTry) {
                    repeat = false
                }
                Thread.sleep(exceptionSleepTime.toLong())
            }

        }
       return ResponseFeatures()

    }
}
