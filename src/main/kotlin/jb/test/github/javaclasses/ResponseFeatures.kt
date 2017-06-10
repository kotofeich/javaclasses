package jb.test.github.javaclasses

/**
 * Created by ksenia on 6/10/17.
 */

class ResponseFeatures {

    var content = StringBuilder()
    var nextPage = String()
    var lastPage = String()

    constructor (content : String = "", link : List<String> = emptyList()) {
        this.content = StringBuilder(content)
        if (link.isEmpty()) {
            return
        }
        val data = link.get(0)
        val splitData = data.split(",")
        for (e in splitData) {
            if (e.contains("rel=\"next\"")) {
                nextPage = e.removeSuffix(">; rel=\"next\"").split("page=").last()
            }
            else if (e.contains("rel=\"last\"")) {
                lastPage = e.removeSuffix(">; rel=\"last\"").split("page=").last()
            }
        }
    }
}