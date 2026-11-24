package com.example.pagoflex

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutenticacionValidacionTest {

    @Test
    fun validarRutChileno_conDigitoVerificadorValido_retornaTrue() {
        assertTrue(esRutValido("27044812-7"))
        assertTrue(esRutValido("11111111-1"))
        assertTrue(esRutValido("15678432-K"))
    }

    @Test
    fun validarRutChileno_conDigitoIncorrectoOFormatoInvalido_retornaFalse() {
        assertFalse(esRutValido("27044812-0"))
        assertFalse(esRutValido("12345678-9"))
        assertFalse(esRutValido("270448127"))
    }

    @Test
    fun validarContrasenaSegura_evaluaLongitudMayusculaNumeroYSimbolo() {
        assertTrue(esContrasenaSegura("PagoFlex2026!"))
        assertFalse(esContrasenaSegura("pagoflex2026"))
        assertFalse(esContrasenaSegura("Ab1!"))
    }

    private fun esRutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").uppercase()
        val partes = limpio.split("-")
        if (partes.size != 2) return false
        val cuerpo = partes[0].toIntOrNull() ?: return false
        val dvIngresado = partes[1]

        var suma = 0
        var multiplo = 2
        var temp = cuerpo
        while (temp > 0) {
            suma += (temp % 10) * multiplo
            temp /= 10
            multiplo = if (multiplo == 7) 2 else multiplo + 1
        }
        val resto = 11 - (suma % 11)
        val dvCalculado = when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
        return dvCalculado == dvIngresado
    }

    private fun esContrasenaSegura(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&#]).{8,}\$")
        return regex.matches(pass)
    }
}