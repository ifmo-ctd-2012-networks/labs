package ru.ifmo.ctddev.igushkin.networks.lab1

import java.io.*
import java.net.*
import java.util.*
import kotlin.concurrent.*

/**
 * ru.ifmo.ctddev.igushkin.networks.lab1.Instance of network discovery.
 */

public data class InstanceEntry(val macAddress: String,
                                val hostName: String)

public class Instance(public val iface: NetworkInterface,
                      public val port: Int,
                      public val onNeighboursUpdated: (List<Instance.NeighbourEntry>) -> Unit = { }) {

    private val socket = broadcastSocketOnInterface(iface, port)
    private val macAddr = iface.hardwareAddress
    private val hostNameBytes = iface.inetAddresses.asSequence().first().hostName.toByteArray(charset = CHARSET)
    private val hostNameLength = byteArrayOf(hostNameBytes.size().toByte())

    private val broadcastAddr = InetSocketAddress(BROADCAST_ADDRESS, DEFAULT_PORT)

    public data class NeighbourEntry(
            val instanceEntry: InstanceEntry,
            @Volatile var ticksMissed: Int = 0,
            @Volatile var missedThisTick: Boolean = false,
            @Volatile var timestamp: Int = 0)

    private val neighbours = TreeMap<String, NeighbourEntry>()

    public fun run() {
        onTicks { sendAnnounce() }
        onTicks { updateNeighbours() }
        listen()
    }

    private fun listen() {
        try {
            while (true) {
                val bytes = ByteArray(PACKET_SIZE)
                val packet = DatagramPacket(bytes, 0, PACKET_SIZE)
                socket.receive(packet)

                val input = bytes.inputStream(0, packet.length)
                fun getBytes(n: Int) = ByteArray(n).apply { input.read(this) }

                if (input.available() < 11) continue;

                val macAddr = getBytes(6).toHexString("-")
                var hostNameLength = getBytes(1)[0].toInt()
                if (hostNameLength < 0)
                    hostNameLength += 128;

                val hostName = getBytes(hostNameLength).toString(CHARSET)
                val time = getBytes(8).getInt()

                neighbours.getOrPut(macAddr) {
                    NeighbourEntry(InstanceEntry(macAddr, hostName), 0, false, 0)
                }.apply {
                    missedThisTick = false
                    ticksMissed = 0
                    timestamp = time
                }
            }
        } catch (e: SocketException) {
            println(e.getMessage())
        }
    }

    private fun sendAnnounce() {
        val output = ByteArrayOutputStream(PACKET_SIZE)

        output write macAddr
        output write hostNameLength
        output write hostNameBytes
        output write timestampBytes()

        val bytes = output.toByteArray()
        val packet = DatagramPacket(bytes, 0, output.size(), broadcastAddr)

        socket send packet
    }

    private fun updateNeighbours() {
        val macsToRemove = ArrayList<String>()
        for ((m, n) in neighbours) {
            if (n.missedThisTick)
                ++n.ticksMissed
            if (n.ticksMissed >= MISSED_TICKS_THRESHOLD) {
                macsToRemove add m
            }
            n.missedThisTick = true
        }
        macsToRemove.forEach { neighbours remove it }
        onNeighboursUpdated(neighbours.values().toList())
    }

    private fun onTicks(applyAction: () -> Unit) {
        timer(period = DEFAULT_PERIOD.toLong(), action = { applyAction() })
    }
}

fun defaultInterface(): NetworkInterface {
    val result = NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .firstOrNull { !it.isLoopback && it.isUp }
    if (result == null)
        throw IllegalStateException("No available network interfaces.")
    return result
}

fun broadcastSocketOnInterface(iface: NetworkInterface, port: Int = DEFAULT_PORT): DatagramSocket {
    val addr = InetSocketAddress(iface.inetAddresses.asSequence().first(), port)
    val result = DatagramSocket(addr) apply { broadcast = true }
    return result
}