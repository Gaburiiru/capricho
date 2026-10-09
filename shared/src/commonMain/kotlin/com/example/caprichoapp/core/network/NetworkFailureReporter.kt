package com.example.caprichoapp.core.network

/**
 * Puente entre los repositorios y el [ConnectivityMonitor]: `runCatchingCancellable` avisa acá
 * cada vez que una llamada falla, y el monitor decide (verificando) si es por falta de internet.
 * No depende de ninguna API de Ktor ni de supabase-kt.
 */
object NetworkFailureReporter {
    var listener: (() -> Unit)? = null

    fun report() {
        listener?.invoke()
    }
}
