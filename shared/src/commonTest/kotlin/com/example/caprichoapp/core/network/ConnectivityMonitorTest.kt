package com.example.caprichoapp.core.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityMonitorTest {

    @Test
    fun `arranca en linea`() = runTest {
        val monitor = ConnectivityMonitor(backgroundScope, probe = { true })
        runCurrent()
        assertTrue(monitor.isOnline.value)
    }

    @Test
    fun `un fallo con la prueba exitosa es falsa alarma y sigue en linea`() = runTest {
        var probes = 0
        val monitor = ConnectivityMonitor(backgroundScope, probe = { probes++; true })

        monitor.onRequestFailed()
        runCurrent()

        assertTrue(monitor.isOnline.value)
        assertEquals(1, probes)
    }

    @Test
    fun `un fallo con la prueba caida pasa a sin conexion`() = runTest {
        val monitor = ConnectivityMonitor(backgroundScope, probe = { false }, retryDelayMs = 1_000)

        monitor.onRequestFailed()
        runCurrent()

        assertFalse(monitor.isOnline.value)
    }

    @Test
    fun `si la prueba lanza una excepcion cuenta como sin conexion`() = runTest {
        val monitor = ConnectivityMonitor(
            backgroundScope,
            probe = { throw IllegalStateException("UnknownHost") },
            retryDelayMs = 1_000,
        )

        monitor.onRequestFailed()
        runCurrent()

        assertFalse(monitor.isOnline.value)
    }

    @Test
    fun `reintenta hasta que vuelve la conexion y avisa una sola vez`() = runTest {
        var connected = false
        val monitor = ConnectivityMonitor(backgroundScope, probe = { connected }, retryDelayMs = 1_000)
        var reconnections = 0
        backgroundScope.launch { monitor.reconnected.collect { reconnections++ } }
        runCurrent()

        monitor.onRequestFailed()
        runCurrent()
        assertFalse(monitor.isOnline.value)

        advanceTimeBy(3_500) // varios reintentos fallidos: sigue sin conexión
        runCurrent()
        assertFalse(monitor.isOnline.value)
        assertEquals(0, reconnections)

        connected = true
        advanceTimeBy(1_000)
        runCurrent()

        assertTrue(monitor.isOnline.value)
        assertEquals(1, reconnections)
    }

    @Test
    fun `varios fallos seguidos se resuelven con una sola verificacion`() = runTest {
        var probes = 0
        val monitor = ConnectivityMonitor(backgroundScope, probe = { probes++; true })

        repeat(5) { monitor.onRequestFailed() }
        runCurrent()

        assertEquals(1, probes)
    }

    @Test
    fun `despues de recuperarse un nuevo corte vuelve a detectarse`() = runTest {
        var connected = false
        val monitor = ConnectivityMonitor(backgroundScope, probe = { connected }, retryDelayMs = 1_000)

        monitor.onRequestFailed()
        runCurrent()
        connected = true
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(monitor.isOnline.value)

        connected = false
        monitor.onRequestFailed()
        runCurrent()

        assertFalse(monitor.isOnline.value)
    }

    @Test
    fun `checkNow detecta que no hay internet sin que falle ningun pedido`() = runTest {
        val monitor = ConnectivityMonitor(backgroundScope, probe = { false }, retryDelayMs = 1_000)

        monitor.checkNow()
        runCurrent()

        assertFalse(monitor.isOnline.value)
    }

    @Test
    fun `las verificaciones periodicas detectan el corte y la vuelta`() = runTest {
        var connected = true
        var probes = 0
        val monitor = ConnectivityMonitor(
            backgroundScope,
            probe = { probes++; connected },
            retryDelayMs = 1_000,
        )
        backgroundScope.launch { monitor.periodicChecks(intervalMs = 4_000).collect() }
        runCurrent()
        assertTrue(monitor.isOnline.value)
        assertEquals(1, probes) // verificó apenas arrancó

        connected = false
        advanceTimeBy(4_000)
        runCurrent()
        assertFalse(monitor.isOnline.value) // se enteró sin ningún pedido fallido

        connected = true
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(monitor.isOnline.value)
    }
}
