package dev.bandno.decision

/**
 * Strips separators and common China country-code prefixes so the same handset
 * compares equal whether it arrives as `+86 138-0013-8000` or `13800138000`.
 */
object NumberNormalizer {
    fun normalize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val digits = buildString(raw.length) {
            for (ch in raw) {
                if (ch.isDigit()) append(ch)
            }
        }
        if (digits.isEmpty()) return null

        val withoutCountry = when {
            digits.startsWith("0086") && digits.length > 4 -> digits.drop(4)
            digits.startsWith("86") && digits.length >= 13 -> digits.drop(2)
            else -> digits
        }
        val national = if (withoutCountry.length == 12 && withoutCountry.startsWith("0") &&
            withoutCountry[1] == '1'
        ) {
            withoutCountry.drop(1)
        } else {
            withoutCountry
        }
        return national.ifEmpty { null }
    }
}
