package cl.duocuc.miregistro.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {
    private val prefs = context.getSharedPreferences("sesion_pagoflex", Context.MODE_PRIVATE)

    fun guardarSesion(
        token: String,
        id: Long,
        nombre: String,
        correo: String,
        rut: String,
        ciudad: String,
        rolId: Int
    ) {
        prefs.edit {
            putString("jwt_token", token)
            putLong("usuario_id", id)
            putString("usuario_nombre", nombre)
            putString("usuario_correo", correo)
            putString("usuario_rut", rut)
            putString("usuario_ciudad", ciudad)
            putInt("rol_usuario", rolId)
        }
    }

    fun actualizarDatosLocales(nombre: String, correo: String, ciudad: String) {
        prefs.edit {
            putString("usuario_nombre", nombre)
            putString("usuario_correo", correo)
            putString("usuario_ciudad", ciudad)
        }
    }

    fun leerToken(): String? = prefs.getString("jwt_token", null)
    fun leerUsuarioId(): Long = prefs.getLong("usuario_id", 0L)
    fun leerNombre(): String = prefs.getString("usuario_nombre", "") ?: ""
    fun leerCorreo(): String = prefs.getString("usuario_correo", "") ?: ""
    fun leerRut(): String = prefs.getString("usuario_rut", "") ?: ""
    fun leerCiudad(): String = prefs.getString("usuario_ciudad", "Santiago") ?: "Santiago"
    fun leerRolId(): Int = prefs.getInt("rol_usuario", 0)

    fun borrar() {
        prefs.edit { clear() }
    }
}