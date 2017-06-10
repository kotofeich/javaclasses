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