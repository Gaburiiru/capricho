package com.example.caprichoapp.core.util

import com.example.caprichoapp.core.network.NetworkFailureReporter
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        NetworkFailureReporter.report() // el monitor verifica si fue por falta de internet
        Result.failure(e)
    }
