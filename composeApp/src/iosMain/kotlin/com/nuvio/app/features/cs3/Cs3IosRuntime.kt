package com.nuvio.app.features.cs3

// iOS: CloudStream .cs3 files are DEX bytecode and cannot run here.
private object Cs3IosRuntime : Cs3Runtime {
    override val isSupported: Boolean = false
    override val unsupportedReason: String? =
        "CloudStream extensions are available on Android only."
}

actual fun createCs3Runtime(): Cs3Runtime = Cs3IosRuntime
