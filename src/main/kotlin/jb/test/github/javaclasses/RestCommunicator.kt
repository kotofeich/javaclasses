package jb.test.github.javaclasses

import com.github.kittinunf.fuel.httpGet
import javax.xml.ws.http.HTTPException

/**
 * Created by ksenia on 6/7/17.
 */

class RestCommunicator {
    
    var prefix = "https://api.github.com"
    var token = "token 7b46403b8cd5994e44449d799eb517b52f4365c2"

    constructor(token : String) {
        this.token = "token " + token
    }

    @Throws(HTTPException::class)
    fun getHttpResult(stringRequest : String)
            : ResponseFeatures {
        val fullRestRequest = listOf(prefix,stringRequest).joinToString("/")

        val (request, response, result) = fullRestRequest
                .httpGet()
                .header(mapOf("Authorization" to token))
                .responseString()
        if (response.httpStatusCode != 200) {
            println(request.toString())
            println("Status code:" + response.httpStatusCode )
            throw HTTPException(response.httpStatusCode)
        }
        val link = response.httpResponseHeaders.get("Link")
        return ResponseFeatures(result.get(), link.orEmpty())
    }
}
