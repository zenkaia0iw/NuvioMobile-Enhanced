package com.nuvio.app.features.cs3

// SHA-256 as lowercase hex. Android implements it with the JDK. iOS does not
// run CS3 extensions, so the iOS actual is never reached.
internal expect fun sha256Hex(bytes: ByteArray): String
