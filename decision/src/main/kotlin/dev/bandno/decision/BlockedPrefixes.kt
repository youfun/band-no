package dev.bandno.decision

object BlockedPrefixes {
    const val MIN_LENGTH = 3
    const val MAX_LENGTH = 7
    const val MAX_COUNT = 30

    fun sanitize(raw: String): String? {
        val digits = buildString(raw.length) {
            for (ch in raw) {
                if (ch.isDigit()) append(ch)
            }
        }
        return digits.takeIf { it.length in MIN_LENGTH..MAX_LENGTH }
    }

    fun isValid(prefix: String): Boolean =
        prefix.length in MIN_LENGTH..MAX_LENGTH && prefix.all { it.isDigit() }

    fun matches(normalizedNumber: String?, prefixes: List<String>): Boolean {
        val number = normalizedNumber ?: return false
        if (number.isEmpty()) return false
        return prefixes.any { prefix -> isValid(prefix) && number.startsWith(prefix) }
    }

    fun add(existing: List<String>, raw: String): List<String> {
        val prefix = sanitize(raw) ?: return existing
        if (prefix in existing) return existing
        if (existing.size >= MAX_COUNT) return existing
        return existing + prefix
    }

    fun remove(existing: List<String>, prefix: String): List<String> = existing.filterNot { it == prefix }

    fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',')
            .mapNotNull { sanitize(it) }
            .distinct()
            .take(MAX_COUNT)
    }

    fun encode(prefixes: List<String>): String =
        prefixes.mapNotNull { sanitize(it) }.distinct().take(MAX_COUNT).joinToString(",")
}
