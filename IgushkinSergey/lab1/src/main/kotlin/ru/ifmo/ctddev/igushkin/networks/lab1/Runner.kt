package ru.ifmo.ctddev.igushkin.networks.lab1

import java.text.*
import java.util.*

object Runner {
    @JvmStatic
    fun main(args: Array<String>) {
        val port = if (args.size() >= 1) args[0].toInt() else DEFAULT_PORT
        Instance(defaultInterface(), port) {
            var maxLen = 10
            for (n in it) {
                val s = "${n.instanceEntry.macAddress} -- ${n.instanceEntry.hostName}" +
                        stringIf({ n.ticksMissed > 0 }, { " (${n.ticksMissed}!)" }) + " ${millisToDateTime(n.timestamp)}"
                println(s)
                maxLen = maxLen coerceAtLeast s.length()
            }
            println("${it.size()} total")
            println("-".repeat(maxLen + 1))
        }.run()
    }
}

inline fun stringIf(condition: () -> Boolean, thenString: () -> String, elseString: String = "")
        = if (condition()) thenString() else elseString;

val dateTimeFormat = SimpleDateFormat("d/M/YY, hh:mm:ss")

fun millisToDateTime(unixTime: Int): String = dateTimeFormat.format(Date(unixTime.toLong() * 1000));