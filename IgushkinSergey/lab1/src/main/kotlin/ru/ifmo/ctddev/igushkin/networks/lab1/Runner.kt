package ru.ifmo.ctddev.igushkin.networks.lab1

object Runner {
    @JvmStatic
    fun main(args: Array<String>) {
        Instance(defaultInterface()) {
            for (n in it) {
                println("${n.macAddress} -- ${n.hostName}")
            }
            println("${it.size()} total")
            println("----------------------------------------------------------------")
        }.run()
    }
}