package jb.test.github.javaclasses

/**
 * Created by ksenia on 6/3/17.
 */

import  com.beust.klaxon.Parser
import  com.beust.klaxon.JsonArray
import  com.beust.klaxon.JsonObject
import  com.xenomachina.argparser.ArgParser
/*import  com.xenomachina.argparser.default*/
import  mu.KotlinLogging


class ParsedArgs(parser: ArgParser) {

    val config by parser.storing("config with token")
    val N by parser.storing("top N class names by frequency") { toInt() }
/*
    val reload by parser.storing("--reload", help = "reload from log").default(null)
*/

}

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val parsedArgs = ParsedArgs(ArgParser(args))
    val config : String = parsedArgs.config
    val token = parseToken(config)
    var page = ""
    var lastPage = ""
    val counterMap = mutableMapOf<String, Int> ()
    val restCommunicator = RestCommunicator(token)
    var processedReposCounter = 0
/*    var loggedRepoPath = ""
    var curRepoPage = "1"
    if (parsedArgs.reload != null) {
        val logValue = reloadFromLog(parsedArgs.reload.toString())
        processedReposCounter = logValue.reposCnt
        counterMap = logValue.counterMap
        page =  logValue.reposPage
        loggedRepoPath = logValue.repoPath
        curRepoPage =  logValue.curRepoPage
    }
    var skipPreviousRepos = false
    if (parsedArgs.reload != null) {
        skipPreviousRepos = true
    }*/
    do{
        var reposRequestString = "search/repositories?q=language:java+size" +
                ":>1000+pushed:>2014-06-01+stars:>10"
        if (page.isNotEmpty()) {
            reposRequestString += "&page=" + page
        }
        else page = "1"
        logger.info {"repos page: $page"}
        logger.info {"processed repos counter: $processedReposCounter"}
        val repoGet = restCommunicator.getHttpResult(reposRequestString)
        val parser: Parser = Parser()
        val javaRepos = (parser.parse(repoGet.content) as JsonObject)
                .getOrDefault("items", JsonArray<JsonObject>()) as JsonArray<JsonObject>
        for (repo in javaRepos) {
            logger.info { "repo name: " + repo.get("full_name") }
            /*if (skipPreviousRepos) {
                if (repo.get("full_name") == loggedRepoPath) {
                    skipPreviousRepos = false
                }
                else continue
            }
             processRepos(repo, restCommunicator, counterMap, curRepoPage)
            */

            processRepos(repo, restCommunicator, counterMap)
            processedReposCounter += 1
        }
        page = repoGet.nextPage
        if (lastPage.isEmpty()) {
            lastPage = repoGet.lastPage
        }

    } while (lastPage.isNotEmpty() &&  processedReposCounter != lastPage.toInt())

    println("total sent requests: " + restCommunicator.requestsCnt.toString())
    println("FINAL:")
    printOutTopKeys(counterMap
            .toList()
            .sortedBy { it.second }
            .asReversed(), parsedArgs.N)


}

private fun processRepos(repo: JsonObject,
                         restCommunicator: RestCommunicator,
                         counterMap: MutableMap<String, Int>,
                         /*curRepoPageLogged : String = "1",*/
                         sleepTime : Long = 4000) : Int {
    val parser: Parser = Parser()
    val repoPath = repo.get("full_name")
    /*var curRepoPage = curRepoPageLogged*/
    var curRepoPage = "1"
    var repoLastPage = ""
    var pagesCounter = 0
    var processedItemsCoutner = 0
    do {

        var curRepoRequestString = "search/code?q=class+in:file" +
                "+language:java+repo:" + repoPath
        if (curRepoPage.isNotEmpty()) {
            curRepoRequestString += "&page=" + curRepoPage
        }
        logger.info { "repo page: $curRepoPage" }
        //logger.info { "counter map: $counterMap" }
        val wordSearchGet = restCommunicator
                .getHttpResult(curRepoRequestString)

        val codeGet = parser.parse(wordSearchGet.content) as JsonObject
        val items = codeGet.getOrDefault("items", JsonArray<JsonObject>()) as JsonArray<JsonObject>
        for (item in items) {
            val name = item.get("name").toString()
            if (name.split(".")
                    .last()
                    .compareTo("java") == 0) {
                val curRate = counterMap
                        .getOrPut(name.removeSuffix(".java"), { 1 })
                counterMap.replace(name, curRate + 1)
                processedItemsCoutner += 1
            }
        }
        pagesCounter += 1
        if (repoLastPage.isEmpty()) {
            repoLastPage = wordSearchGet.lastPage
        }
        curRepoPage = wordSearchGet.nextPage


        Thread.sleep(sleepTime)
    } while (repoLastPage.isNotEmpty() && pagesCounter != repoLastPage.toInt())
    if (pagesCounter == 0) {
        Thread.sleep(sleepTime)
    }
    return processedItemsCoutner
}