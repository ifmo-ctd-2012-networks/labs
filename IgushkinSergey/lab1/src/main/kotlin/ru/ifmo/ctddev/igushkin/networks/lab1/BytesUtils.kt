package ru.ifmo.ctddev.igushkin.networks.lab1

import java.nio.*

private val HEX_CHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

fun Byte.toHexString() =
        this.toInt().let {
            StringBuilder {
                append(HEX_CHARS[it and 0xF0 shr 4])
                append(HEX_CHARS[it and 0x0F])
            }.toString()
        }

fun ByteArray.toHexString(delimiter: String) = this.map { it.toHexString() }.joinToString(delimiter)

fun ByteArray.getInt() = ByteBuffer.wrap(this).getInt()

fun ByteArray.getLong() = ByteBuffer.wrap(this).getLong()

fun timestampBytes() = (System.currentTimeMillis() / 1000).toInt().let {
    ByteBuffer.allocate(8).putInt(it).array()
}