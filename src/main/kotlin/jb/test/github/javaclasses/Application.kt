package jb.test.github.javaclasses

/**
 * Created by ksenia on 6/3/17.
 */

import  com.beust.klaxon.Parser
import  com.beust.klaxon.JsonArray
import  com.beust.klaxon.JsonObject
import  com.xenomachina.argparser.ArgParser



class ParsedArgs(parser: ArgParser) {

    val config by parser.storing("config with token")
    val N by parser.storing("top N class names by frequency") { toInt() }
    val verbose by parser.flagging("-v", "--verbose", help = "verbose output")
}


fun main(args: Array<String>) {
    val parsedArgs = ParsedArgs(ArgParser(args))
    val config : String = parsedArgs.config
    val token = parseToken(config)
    var page = ""
    var lastPage = ""
    val counterMap = mutableMapOf<String, Int> ()
    val restCommunicator = RestCommunicator(token)
    var processedCounter = 0
    do{
        var reposRequestString = "search/repositories?q=language:java"
        if (page.isNotEmpty()) {
            reposRequestString += "&page=" + page
        }

        val repoGet = restCommunicator.getHttpResult(reposRequestString)
        val parser: Parser = Parser()
        val javaRepos = (parser.parse(repoGet.content) as JsonObject)
                .getOrDefault("items", JsonArray<JsonObject>()) as JsonArray<JsonObject>
        for (repo in javaRepos) {
            processRepos(repo, restCommunicator, parsedArgs, counterMap)
            processedCounter += 1
            if (parsedArgs.verbose) {
                println("number of repos processed: $processedCounter")
            }
        }
        page = repoGet.nextPage
        if (lastPage.isEmpty()) {
            lastPage = repoGet.lastPage
        }

    } while (lastPage.isNotEmpty() &&  processedCounter != lastPage.toInt())

    println("FINAL:")
    printOutTopKeys(counterMap
            .toList()
            .sortedBy { it.second }
            .asReversed(), parsedArgs.N)


}

private fun processRepos(repo: JsonObject,
                         restCommunicator: RestCommunicator,
                         parsedArgs: ParsedArgs,
                         counterMap: MutableMap<String, Int>,
                         sleepTime : Long = 4000) {
    val parser: Parser = Parser()
    val repoPath = repo.get("full_name")
    println("processing: $repoPath")
    var repoCurPage = "1"
    var repoLastPage = ""
    var pagesCounter = 0
    do {

        var curRepoRequestString = "search/code?q=class+in:file" +
                "+language:java+repo:" + repoPath
        if (repoCurPage.isNotEmpty()) {
            curRepoRequestString += "&page=" + repoCurPage
        }
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
            }
        }
        pagesCounter += 1
        if (repoLastPage.isEmpty()) {
            repoLastPage = wordSearchGet.lastPage
        }
        if (parsedArgs.verbose) {
            println("current page $repoCurPage lastPage $repoLastPage")
        }
        repoCurPage = wordSearchGet.nextPage


        Thread.sleep(sleepTime)
    } while (repoLastPage.isNotEmpty() && pagesCounter != repoLastPage.toInt())
    if (parsedArgs.verbose) {
        println("processed pages: $pagesCounter")
    }
    if (pagesCounter == 0) {
        Thread.sleep(sleepTime)
    }

}