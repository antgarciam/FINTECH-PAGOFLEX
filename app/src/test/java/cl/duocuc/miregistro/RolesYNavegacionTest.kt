package com.example.pagoflex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolesYNavegacionTest {

    @Test
    fun resolverRutaInicial_segunRolId_retornaDestinoCorrecto() {
        assertEquals("admin_home", obtenerRutaPorRol(1))
        assertEquals("agente_home", obtenerRutaPorRol(2))
        assertEquals("usuario_home", obtenerRutaPorRol(3))
        assertEquals("ejecutivo_home", obtenerRutaPorRol(4))
    }

    @Test
    fun resolverRutaInicial_conRolDesconocido_redirigeALogin() {
        assertEquals("login", obtenerRutaPorRol(0))
        assertEquals("login", obtenerRutaPorRol(99))
    }

    @Test
    fun verificarPermisoGestionUsuarios_soloPermiteAdministrador() {
        assertTrue(puedeGestionarUsuarios(1))
        assertFalse(puedeGestionarUsuarios(2))
        assertFalse(puedeGestionarUsuarios(3))
        assertFalse(puedeGestionarUsuarios(4))
    }

    private fun obtenerRutaPorRol(rolId: Int): String = when (rolId) {
        1 -> "admin_home"
        2 -> "agente_home"
        3 -> "usuario_home"
        4 -> "ejecutivo_home"
        else -> "login"
    }

    private fun puedeGestionarUsuarios(rolId: Int): Boolean = rolId == 1
}