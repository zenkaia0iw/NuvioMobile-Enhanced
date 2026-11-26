package com.nuvio.app.features.cs3

// CloudStream extensions are Android only. This is never called on iOS.
internal actual fun sha256Hex(bytes: ByteArray): String =
    throw UnsupportedOperationException("CloudStream extensions are Android only.")
