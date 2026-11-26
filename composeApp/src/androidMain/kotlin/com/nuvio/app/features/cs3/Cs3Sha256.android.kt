package com.nuvio.app.features.cs3

import java.security.MessageDigest

internal actual fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString(separator = "") { "%02x".format(it) }
}
