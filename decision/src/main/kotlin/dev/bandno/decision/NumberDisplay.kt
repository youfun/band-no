package dev.bandno.decision

object NumberDisplay {
    fun mask(number: String?): String {
        if (number.isNullOrBlank()) return ""
        if (number.length <= 4) return "****"
        if (number.length <= 7) {
            return number.take(1) + "****" + number.takeLast(2)
        }
        return number.take(3) + "****" + number.takeLast(4)
    }
}
