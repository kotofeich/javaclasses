package jb.test.github.javaclasses

/**
 * Created by ksenia on 6/3/17.
 */

import javax.xml.ws.http.HTTPException
import  com.github.kittinunf.fuel.httpGet
import  com.beust.klaxon.Parser
import  com.beust.klaxon.JsonArray
import  com.beust.klaxon.JsonObject
import  com.xenomachina.argparser.ArgParser



class ParsedArgs(parser: ArgParser) {

    val repoName by parser.storing("repo name")
    val ownerId by parser.storing("owner")
    val N by parser.storing("top N class names by frequency") { toInt() }

}

fun passJavaFile(jsonName : JsonObject) : String? {
    val name = jsonName.get("path").toString()

    val name_split = name.split(".")
    if (name_split.size > 1 && name_split[0].isNotEmpty()) {
        if (name_split.last().equals("java")) {
            return name.split("/").last().split(".").first()
        }
        return null
    }
    return null
}

@Throws(HTTPException::class)
fun getHttpResult(fullName : String, stringRequest : String, prefix : String = "https://api.github.com")
        : StringBuilder{
    val fullRestRequest = listOf(prefix,"repos",fullName,stringRequest).joinToString("/")
    val (_, response, result) = fullRestRequest.httpGet().responseString()
    if (response.httpStatusCode != 200) {
        throw HTTPException(response.httpStatusCode)
    }
    return StringBuilder(result.get())
}

fun printOutTopKeys(sortedPairs: List<Pair<String,Int>>, N : Int )  {
    var i = 0
    var lastVal = 0

    for ((key, v) in sortedPairs)   {
        if (i < N-1) {
            println(key + " " + v)
        }
        else if (i == N-1) {
            println(key + " " + v)
            lastVal = v
        }
        else if (v == lastVal) {
            println(key + " " + v)
        }
        else {
            return
        }
        i += 1
    }
}
fun main(args: Array<String>) {

    val parsedArgs : ParsedArgs = ParsedArgs(ArgParser(args))
    val parser: Parser = Parser()
    val fullName : String = parsedArgs.ownerId+'/'+parsedArgs.repoName

    val repoGet = getHttpResult(fullName, "git/refs/heads/master")
    val commitJson = parser.parse(repoGet) as JsonObject
    val objectJson = commitJson.get("object") as JsonObject
    val sha = objectJson.get("sha")
    val shaTrees = getHttpResult(fullName, "git/trees/"+sha+"?recursive=1")
    val shaJson = parser.parse(shaTrees) as JsonObject
    val treesJsonArray = shaJson.get("tree") as JsonArray<JsonObject>
    val javaFiles = treesJsonArray.mapNotNull {
        passJavaFile( it )
    }
    var grouped = javaFiles.distinct().groupingBy { it }.eachCount().toList()
    printOutTopKeys(grouped.sortedBy { it.second },parsedArgs.N)

}