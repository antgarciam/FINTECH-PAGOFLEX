package com.example.pagoflex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompromisosYCuotasTest {

    data class CuotaItem(val id: Long, val monto: Int, val estado: String)

    @Test
    fun calcularValorCuota_divideMontoTotalEnPartesIguales() {
        assertEquals(30000, calcularCuotaMensual(90000, 3))
        assertEquals(25000, calcularCuotaMensual(100000, 4))
    }

    @Test
    fun filtrarCuotasPendientes_retornaUnicamenteCuotasNoPagadas() {
        val cuotas = listOf(
            CuotaItem(1L, 30000, "Pagado"),
            CuotaItem(2L, 30000, "Pendiente"),
            CuotaItem(3L, 30000, "Pendiente")
        )
        val pendientes = cuotas.filter { it.estado == "Pendiente" }
        assertEquals(2, pendientes.size)
        assertTrue(pendientes.all { it.estado == "Pendiente" })
    }

    @Test
    fun calcularTotalDeudaPendiente_sumaSoloMontosPendientes() {
        val cuotas = listOf(
            CuotaItem(1L, 45000, "Pagado"),
            CuotaItem(2L, 45000, "Pendiente"),
            CuotaItem(3L, 15000, "Pendiente")
        )
        val totalPendiente = cuotas.filter { it.estado == "Pendiente" }.sumOf { it.monto }
        assertEquals(60000, totalPendiente)
    }

    private fun calcularCuotaMensual(montoTotal: Int, numeroCuotas: Int): Int {
        require(numeroCuotas > 0) { "El número de cuotas debe ser mayor a 0" }
        return montoTotal / numeroCuotas
    }
}