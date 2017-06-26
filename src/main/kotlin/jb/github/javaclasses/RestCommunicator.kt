package jb.github.javaclasses

import com.github.kittinunf.fuel.httpGet
import mu.KotlinLogging
import javax.xml.ws.http.HTTPException

/**
 * Created by ksenia on 6/7/17.
 */

class RestCommunicator(var token: String) {
    
    var prefix = "https://api.github.com"
    var requestsCnt = 0
    val exceptionSleepTime = 100000

    private val logger = KotlinLogging.logger {}


    @Throws(HTTPException::class)
    fun getHttpResult(stringRequest : String, maxTry: Int = 3)
            : ResponseFeatures {
        val fullRestRequest = listOf(prefix, stringRequest).joinToString("/")
        var tryCount = 0
        while (true) {
            try {
                val h = if (token.isNotEmpty()) {
                    mapOf("Authorization" to "token " + token)
                } else null
                val (request, response, result) = fullRestRequest
                        .httpGet()
                        .header(h)
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
                if (tryCount == maxTry) {
                    throw HTTPException(e.statusCode)
                }
                Thread.sleep(exceptionSleepTime.toLong())
            }

        }
    }
}
