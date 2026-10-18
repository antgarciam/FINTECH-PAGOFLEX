package cl.duocuc.miregistro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.miregistro.data.ConvertidorImagen
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UsuarioDbHelper(application)

    private val sesion = SesionPreferencias(application)

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _usuarios = MutableStateFlow(db.listarUsuarios())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    val roles: List<Rol> = db.listarRoles()

    private val _usuarioActual = MutableStateFlow(db.buscarPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private fun recargar() {
        val texto = _filtro.value.trim()
        _usuarios.value = if (texto.isEmpty()) db.listarUsuarios() else db.buscarUsuarios(texto)
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        recargar()
    }

    fun obtener(id: Long): Usuario? = db.buscarPorId(id)

    fun registrar(usuario: Usuario) {
        val id = db.insertarUsuario(usuario)
        _usuarioActual.value = db.buscarPorId(id)
        sesion.guardar(id)
        recargar()
    }

    fun actualizar(editado: Usuario) {
        db.actualizarUsuario(editado)
        if (_usuarioActual.value?.id == editado.id) _usuarioActual.value = db.buscarPorId(editado.id)
        recargar()
    }

    fun eliminar(usuario: Usuario) {
        db.eliminarUsuario(usuario.id)
        if (_usuarioActual.value?.id == usuario.id) cerrarSesion()
        recargar()
    }

    // NUEVO (Cámara): la cámara entrega directamente un Bitmap
    fun cambiarAvatarDesdeCamara(foto: Bitmap) {
        guardarAvatar(ConvertidorImagen.aBytes(foto))
    }

    // NUEVO (Galería): la galería entrega un Uri. Para leerlo hace falta un Context:
    // getApplication() lo entrega porque este es un AndroidViewModel.
    fun cambiarAvatarDesdeGaleria(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri) ?: return
        guardarAvatar(ConvertidorImagen.aBytes(imagen))
    }

    // NUEVO (Avatar): 1) UPDATE en la BD  2) se relee al usuario para refrescar el Perfil.
    // Solo funciona con sesión iniciada (?: return = "si no hay usuario, no hagas nada").
    private fun guardarAvatar(bytes: ByteArray) {
        val usuario = _usuarioActual.value ?: return
        db.actualizarAvatar(usuario.id, bytes)
        _usuarioActual.value = db.buscarPorId(usuario.id)
        recargar()
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
