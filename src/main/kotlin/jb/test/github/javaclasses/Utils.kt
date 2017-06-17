package jb.test.github.javaclasses

import java.io.BufferedReader
import java.io.FileReader

/**
 * Created by ksenia on 6/10/17.
 */

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

fun parseToken(configPath : String) : String {
    val br = BufferedReader(FileReader(configPath));
    val line = br.readLine()
    if (line.isEmpty()) {
        println("Empty token!")
    }
    return line
}

/*
class LogValue(reposCnt: Int,
               counterMap: MutableMap<String, Int>,
               reposPage: String,
               repoPath: String,
               curRepoPage: String) {
    var reposCnt = reposCnt
    val counterMap = counterMap
    var reposPage = reposPage
    var repoPath = repoPath
    var curRepoPage = curRepoPage
}


fun reloadFromLog(logPath : String) : LogValue {
    val br = BufferedReader(FileReader(logPath));
    var reposCnt = 0
    val counterMap = mutableMapOf<String, Int>()
    var reposPage = ""
    var repoPath = ""
    var curRepoPage = ""
    var line = br.readLine()
    while (line != null) {
        if (line.contains("repos page")){
            reposPage = line.split("repos page: ")[1]
        }
        else if (line.contains("counter map")){
            var s = line.split("counter map: ")[1]
            if (s != "{}") {
                s = s.dropLast(1).drop(0)
                for (e in s.split(", ")) {
                    val a = e.split("=")
                    counterMap.put(a[0], a[1].toInt())
                }
            }
        }
        else if (line.contains("repo name")){
            repoPath = line.split("repo name: ")[1]
        }
        else if (line.contains("repo page")){
            curRepoPage = line.split("repo page: ")[1]
        }
        else if (line.contains("processed repos counter")){
           reposCnt = line.split("processed repos counter: ")[1].toInt()
        }
        line = br.readLine()
    } 
    return LogValue(reposCnt, counterMap, reposPage, repoPath, curRepoPage)
}*/
