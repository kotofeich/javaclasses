/**
 * Created by ksenia on 6/17/17.
 */

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import jb.github.javaclasses.RestCommunicator
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.xml.ws.http.HTTPException

class RestCommunicatorTest {

    @Test
    fun testRateLimitWorks() {
        val restCommunicator = RestCommunicator("")
        val responseFeatures = restCommunicator.getHttpResult("rate_limit",1)
        val parser: Parser = Parser()
        val featuresObj = parser.parse(responseFeatures.content) as JsonObject
        assertTrue(featuresObj.keys.contains("resources"))
    }

    @Test(expected = HTTPException::class)
    fun testFailsWhenNotFound() {
        val restCommunicator = RestCommunicator("")
        restCommunicator.getHttpResult("earch",1)
    }
}