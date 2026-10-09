package com.example.caprichoapp.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Sabe si la app tiene internet, sin APIs de cada plataforma (todo en `commonMain`).
 *
 * Funciona por **verificación** (no por adivinar el tipo de error), y se dispara de dos formas:
 *  - **Proactiva:** [periodicChecks] verifica cada pocos segundos mientras la app está visible,
 *    y [checkNow] verifica al instante (al entrar a una pantalla, al volver del fondo). Así la
 *    app se entera de que no hay internet sin esperar a que falle una llamada a Supabase.
 *  - **Reactiva:** cuando una llamada de red falla, se avisa con [onRequestFailed].
 *
 * Cada verificación:
 *  1. Hace una prueba liviana ([probe]).
 *     Si la prueba responde, fue un error cualquiera y sigue "en línea" (la pantalla muestra su error).
 *     Si no responde, pasa a "sin conexión".
 *  3. Mientras no hay conexión reintenta cada [retryDelayMs] hasta que vuelva.
 *     Al volver, publica [isOnline] = true y emite en [reconnected] para que la pantalla recargue.
 *
 * Los avisos se encolan en un canal conflated: se pueden llamar desde cualquier hilo y varios
 * fallos seguidos (por ejemplo, 5 pedidos en paralelo) generan una sola verificación.
 *
 * @param probe devuelve true si hay respuesta del servidor (cualquier código HTTP sirve) y
 *  false o lanza una excepción si no se pudo conectar.
 */
class ConnectivityMonitor(
    private val scope: CoroutineScope,
    private val probe: suspend () -> Boolean,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
) {
    private val _isOnline = MutableStateFlow(true)

    /** `false` solo cuando se confirmó que no hay conexión. Arranca en `true`. */
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _reconnected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite una vez cada vez que se recupera la conexión después de haberla perdido. */
    val reconnected: SharedFlow<Unit> = _reconnected.asSharedFlow()

    private val failures = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch {
            for (ignored in failures) verify()
        }
    }

    /** Lo llama el repositorio cuando falla un pedido. Seguro desde cualquier hilo. */
    fun onRequestFailed() {
        failures.trySend(Unit)
    }

    /** Pide una verificación inmediata. Seguro desde cualquier hilo. */
    fun checkNow() {
        failures.trySend(Unit)
    }

    /**
     * Verifica ahora y después cada [intervalMs]. Pensado para colectarse con el ciclo de vida
     * de la pantalla: se detiene en segundo plano y verifica al instante al volver.
     */
    fun periodicChecks(intervalMs: Long = DEFAULT_CHECK_INTERVAL_MS): Flow<Unit> = flow {
        while (true) {
            checkNow()
            emit(Unit)
            delay(intervalMs)
        }
    }

    private suspend fun verify() {
        if (safeProbe()) return // falsa alarma: había red, el error fue otro

        _isOnline.value = false
        while (true) {
            delay(retryDelayMs)
            if (safeProbe()) break
        }
        failures.tryReceive() // descarta avisos viejos para no verificar de nuevo al pedo
        _isOnline.value = true
        _reconnected.tryEmit(Unit)
    }

    private suspend fun safeProbe(): Boolean =
        try {
            probe()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }

    companion object {
        const val DEFAULT_RETRY_DELAY_MS = 3_000L
        const val DEFAULT_CHECK_INTERVAL_MS = 4_000L
    }
}
