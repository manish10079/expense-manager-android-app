package com.mknlabs.expensetracker.ai

import java.security.MessageDigest
import java.util.Locale

/**
 * Stable key for one on-device OCR receipt body. Same screenshot (cosmetic
 * whitespace/case aside) hashes identically so a second save can be skipped.
 */
object OcrReceiptFingerprint {

    fun of(ocrText: String): String = sha256Hex(normalize(ocrText))

    private fun normalize(value: String): String =
        value.trim().replace(WHITESPACE, " ").lowercase(Locale.ROOT)

    private fun sha256Hex(material: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(material.toByteArray(Charsets.UTF_8))
        return buildString(bytes.size * 2) {
            for (byte in bytes) {
                val value = byte.toInt() and 0xFF
                if (value < 0x10) append('0')
                append(Integer.toHexString(value))
            }
        }
    }

    private val WHITESPACE = Regex("""\s+""")
}
