package dev.bandno.decision

/**
 * Strips separators and common China country-code prefixes so the same handset
 * compares equal whether it arrives as `+86 138-0013-8000` or `13800138000`.
 */
object NumberNormalizer {
    fun normalize(raw: String?): String? {
        val parsed = parse(raw) ?: return null
        return domesticDigits(parsed)
    }

    fun parse(raw: String?): NormalizedNumber? {
        if (raw.isNullOrBlank()) return null
        val digits = buildString(raw.length) {
            for (ch in raw) {
                if (ch.isDigit()) append(ch)
            }
        }
        if (digits.isEmpty()) return null
        val international = raw.trimStart().startsWith("+") ||
            (digits.startsWith("00") && digits.length > 4)
        val kept = if (digits.startsWith("00") && digits.length > 4) digits.drop(2) else digits
        return NormalizedNumber(kept, international)
    }

    /** Digits used to compare repeat calls and blocked prefixes. */
    fun domesticDigits(number: NormalizedNumber): String {
        val digits = number.digits
        val withoutCountry = when {
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
        return national
    }
}
