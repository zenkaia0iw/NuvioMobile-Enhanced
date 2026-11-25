package com.nuvio.app.features.cs3

// Android runtime. M1: contract only, execution arrives in M4 (DEX loader).
private object Cs3AndroidRuntime : Cs3Runtime {
    override val isSupported: Boolean = false
    override val unsupportedReason: String? =
        "CloudStream runtime is not available in this build yet."
}

actual fun createCs3Runtime(): Cs3Runtime = Cs3AndroidRuntime
