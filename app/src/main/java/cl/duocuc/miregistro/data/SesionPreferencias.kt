package cl.duocuc.miregistro.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardarToken(token: String) { prefs.edit { putString("jwt_token", token) } }
    fun guardarRol(rol: String) { prefs.edit { putString("rol_usuario", rol) } }

    fun leerToken(): String? = prefs.getString("jwt_token", null)
    fun leerRol(): String? = prefs.getString("rol_usuario", null)

    fun borrar() { prefs.edit { clear() } }
}