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
import cl.duocuc.miregistro.model.Compromiso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UsuarioDbHelper(application)


    // Compromisos de prueba
    private val _compromisos = MutableStateFlow(
        listOf(
            Compromiso(
                id = 1L,
                empresa = "Altos del Maipo",
                concepto = "Crédito de consumo",
                cuota = "1 de 6",
                monto = 45000,
                vencimiento = "01-10-2026",
                estado = "Vencido",
                folio = "PF-0001"
            ),
            Compromiso(
                id = 2L,
                empresa = "Altos del Maipo",
                concepto = "Crédito de consumo",
                cuota = "2 de 6",
                monto = 45000,
                vencimiento = "01-11-2026",
                estado = "Pendiente",
                folio = "PF-0002"
            ),
            Compromiso(
                id = 3L,
                empresa = "Empresa Ejemplo",
                concepto = "Servicio mensual",
                cuota = "1 de 3",
                monto = 20000,
                vencimiento = "25-10-2026",
                estado = "Pendiente",
                folio = "PF-0003"
            )
        )
    )

    val compromisos: StateFlow<List<Compromiso>> =
        _compromisos.asStateFlow()

    // RN-04: bloquear cuotas pendientes de la misma empresa
    // cuando existe una cuota vencida de esa empresa.

    fun puedePagar(compromiso: Compromiso): Boolean {
        // Una cuota vencida puede pagarse para regularizar la deuda.
        if (compromiso.estado.equals("Vencido", ignoreCase = true)) {
            return true
        }

        // Solo se pueden pagar cuotas pendientes.
        if (!compromiso.estado.equals("Pendiente", ignoreCase = true)) {
            return false
        }

        // Revisa si existe una cuota vencida en la misma empresa.
        val tieneCuotaVencida = _compromisos.value.any { otraCuota ->
            otraCuota.empresa.equals(compromiso.empresa, ignoreCase = true) &&
                    otraCuota.estado.equals("Vencido", ignoreCase = true)
        }

        return !tieneCuotaVencida
    }

    fun pagarCompromiso(compromiso: Compromiso) {
        if (!puedePagar(compromiso)) {
            return
        }

        _compromisos.value = _compromisos.value.map { cuota ->
            if (cuota.id == compromiso.id) {
                cuota.copy(estado = "Pagado")
            } else {
                cuota
            }
        }
    }



    private val sesion = SesionPreferencias(application)

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _usuarios = MutableStateFlow(db.listarUsuarios())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    val roles: List<Rol> = db.listarRoles()

    private val _usuarioActual = MutableStateFlow(db.buscarPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    init {
        if (_usuarioActual.value == null) {
            // Buscamos si Camila ya está en la BD por su nombre
            val usuarioSimulado = db.buscarUsuarios("Camila Andrea").firstOrNull()

            if (usuarioSimulado != null) {
                _usuarioActual.value = usuarioSimulado
                sesion.guardar(usuarioSimulado.id)
            } else {
                // Creamos a Camila adaptado estrictamente a tus campos reales
                val camila = Usuario(
                    id = 0L,
                    nombre = "Camila Andrea Rojas Peña",
                    correo = "crojas@correo.ejemplo",
                    ciudad = "Maipú",
                    rolId = 1L, // Rol R-01 Usuario Final
                    rolNombre = "Usuario Final",
                    avatar = null
                )

                val nuevoId = db.insertarUsuario(camila)
                _usuarioActual.value = db.buscarPorId(nuevoId)
                sesion.guardar(nuevoId)
            }
            recargar()
        }
    }
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
