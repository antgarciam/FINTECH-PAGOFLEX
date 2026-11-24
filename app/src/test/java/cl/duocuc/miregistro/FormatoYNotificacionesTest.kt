package com.example.pagoflex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatoYNotificacionesTest {

    data class NotificacionItem(val id: Long, val usuarioId: Long, val mensaje: String, val leida: Boolean)

    @Test
    fun formatearMonedaClp_formateaMilesConPuntoYSignoPeso() {
        assertEquals("$45.000", formatearClp(45000))
        assertEquals("$1.250.000", formatearClp(1250000))
    }

    @Test
    fun filtrarNotificacionesPorUsuario_devuelveSoloLasDelUsuarioActivo() {
        val lista = listOf(
            NotificacionItem(1L, 10L, "Pago recibido", true),
            NotificacionItem(2L, 10L, "Cuota próxima a vencer", false),
            NotificacionItem(3L, 20L, "Bienvenido a PagoFlex", false)
        )
        val delUsuario10 = lista.filter { it.usuarioId == 10L }
        assertEquals(2, delUsuario10.size)
        assertTrue(delUsuario10.all { it.usuarioId == 10L })
    }

    @Test
    fun contarNotificacionesNoLeidas_retornaCantidadExacta() {
        val lista = listOf(
            NotificacionItem(1L, 10L, "Pago recibido", true),
            NotificacionItem(2L, 10L, "Cuota próxima a vencer", false),
            NotificacionItem(3L, 10L, "Reporte actualizado", false)
        )
        val noLeidas = lista.count { !it.leida }
        assertEquals(2, noLeidas)
    }

    private fun formatearClp(monto: Int): String {
        return "$" + String.format("%,d", monto).replace(',', '.')
    }
}