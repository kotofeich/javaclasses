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
    val verbose by parser.flagging("-v", "--verbose", help = "extensive output")
}


fun main(args: Array<String>) {

    val parsedArgs: ParsedArgs = ParsedArgs(ArgParser(args))
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
                println("total repos counted: " + processedCounter.toString())
            }

        }
        page = repoGet.nextPage
        lastPage = repoGet.lastPage
        val uniqFrequencyVals = setOf(counterMap
                .toList()
                .sortedBy { it.second }
                .asReversed()
                .map({it.second}))
        if (parsedArgs.verbose) {
            print("Current frequency values:")
            for (x in uniqFrequencyVals) {
                println(x.toString())
            }
        }
    } while (!page.equals(lastPage))

    println("FINAL:")
    printOutTopKeys(counterMap
            .toList()
            .sortedBy { it.second }
            .asReversed(),
            parsedArgs.N)


}

private fun processRepos(repo: JsonObject,
                         restCommunicator: RestCommunicator,
                         parsedArgs: ParsedArgs,
                         counterMap: MutableMap<String, Int>,
                         sleepTime : Long = 5000) {
    val parser: Parser = Parser()
    val repoPath = repo.get("full_name")
    println("processing: " + repoPath)
    var repoCurPage = ""
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
        repoCurPage = wordSearchGet.nextPage
        repoLastPage = wordSearchGet.lastPage
        if (parsedArgs.verbose) {
            println("next page " + repoCurPage + " lastPage " + repoLastPage)
        }
        Thread.sleep(sleepTime)
    } while (!repoCurPage.equals(repoLastPage))
    if (parsedArgs.verbose) {
        println("processed pages:" + pagesCounter.toString())
    }
    if (pagesCounter == 0) {
        Thread.sleep(sleepTime)
    }

}