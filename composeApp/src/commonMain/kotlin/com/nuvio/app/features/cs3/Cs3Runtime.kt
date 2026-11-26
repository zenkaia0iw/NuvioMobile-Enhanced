package com.nuvio.app.features.cs3

// Platform boundary for CloudStream .cs3 execution.
// commonMain only declares the contract. DEX loading lives in androidMain.
// iOS returns an unsupported state. No Android types leak into common code.

interface Cs3Runtime {
    val isSupported: Boolean
    val unsupportedReason: String?
}

expect fun createCs3Runtime(): Cs3Runtime
