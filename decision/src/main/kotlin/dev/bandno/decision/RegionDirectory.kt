package dev.bandno.decision

import java.io.DataInputStream

/**
 * Offline display names for a normalized number. China uses prefix data derived
 * from libphonenumber geocoding (`resources/geocoding/zh/86.txt`, Apache-2.0).
 * Other numbers use the country calling code only.
 *
 * This never affects screening. An unknown number returns null.
 */
class RegionDirectory(
    private val mobile: List<ChinaPrefixLayer>,
    private val landline: List<ChinaPrefixLayer>,
    private val countries: Map<Int, String>,
) {
    fun describe(number: NormalizedNumber?): String? {
        val digits = number?.digits ?: return null
        if (number.international) {
            val callingCode = callingCode(digits) ?: return null
            if (callingCode != CHINA) return countries[callingCode]
        }
        val national = chinaNational(digits) ?: return null
        return chinaName(national)
    }

    private fun callingCode(normalized: String): Int? {
        if (normalized.startsWith("0")) return null
        for (length in 3 downTo 1) {
            if (normalized.length <= length) continue
            val code = normalized.take(length).toIntOrNull() ?: continue
            if (code in countries) return code
        }
        return null
    }

    private fun chinaNational(normalized: String): String? {
        val national = when {
            normalized.startsWith("86") && normalized.length >= 13 -> normalized.drop(2)
            normalized.startsWith("86") -> return null
            else -> normalized
        }
        val withoutTrunk = if (national.startsWith("0")) national.drop(1) else national
        if (withoutTrunk.any { !it.isDigit() }) return null
        return withoutTrunk
    }

    private fun chinaName(national: String): String? {
        if (national.startsWith("1") && national.length == 11 && national[1] != '0') {
            return longest(mobile, national)
        }
        if (national.length !in 9..12 || national.startsWith("1") && national[1] in '3'..'9') return null
        return longest(landline, national)
    }

    private fun longest(layers: List<ChinaPrefixLayer>, national: String): String? {
        for (layer in layers) {
            if (national.length < layer.length) continue
            layer.find(national.take(layer.length))?.let { return it }
        }
        return null
    }

    companion object {
        private const val CHINA = 86

        fun load(
            mobileTable: ByteArray,
            landlineTable: ByteArray,
            countries: Map<Int, String>,
        ): RegionDirectory {
            val mobile = readLayers(mobileTable).sortedByDescending { it.length }
            val landline = readLayers(landlineTable).sortedByDescending { it.length }
            return RegionDirectory(mobile, landline, countries)
        }

        private fun readLayers(table: ByteArray): List<ChinaPrefixLayer> {
            val input = DataInputStream(table.inputStream())
            val nameCount = input.readUnsignedShort()
            val names = Array(nameCount) {
                val size = input.readUnsignedByte()
                val bytes = ByteArray(size)
                input.readFully(bytes)
                bytes.decodeToString()
            }
            val layerCount = input.readUnsignedByte()
            val layers = List(layerCount) {
                val length = input.readUnsignedByte()
                val rowCount = input.readInt()
                val prefixes = Array(rowCount) { "" }
                val labels = Array(rowCount) { "" }
                repeat(rowCount) { index ->
                    val bytes = ByteArray(length)
                    input.readFully(bytes)
                    prefixes[index] = bytes.decodeToString()
                    labels[index] = names[input.readUnsignedShort()]
                }
                ChinaPrefixLayer(length, prefixes, labels)
            }
            return layers
        }
    }
}

class ChinaPrefixLayer(
    val length: Int,
    private val prefixes: Array<String>,
    private val names: Array<String>,
) {
    fun find(key: String): String? {
        var low = 0
        var high = prefixes.size - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val cmp = prefixes[mid].compareTo(key)
            when {
                cmp == 0 -> return names[mid]
                cmp < 0 -> low = mid + 1
                else -> high = mid - 1
            }
        }
        return null
    }
}
