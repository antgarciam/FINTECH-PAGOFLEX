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
import cl.duocuc.miregistro.model.ComprobantePago
import cl.duocuc.miregistro.model.Reporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.emptyList


class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UsuarioDbHelper(application)

    private val _comprobanteActual =
        MutableStateFlow<ComprobantePago?>(null)

    //Comprobante de pago del compromiso

    val comprobanteActual: StateFlow<ComprobantePago?> =
        _comprobanteActual.asStateFlow()

    // historial de pagos
    private val _historialPagos =
        MutableStateFlow<List<ComprobantePago>>(emptyList())

    val historialPagos: StateFlow<List<ComprobantePago>> =
        _historialPagos.asStateFlow()

    // Compromisos de prueba

    private val _compromisos =
        MutableStateFlow<List<Compromiso>>(emptyList())

    val compromisos: StateFlow<List<Compromiso>> =
        _compromisos.asStateFlow()

    private val _comprobantesCargados =
        MutableStateFlow(false)


    // RN-04: bloquear cuotas pendientes de la misma empresa
    // cuando existe una cuota vencida de esa empresa.


    private fun cargarDatosFinancieros(usuarioId: Long) {
        db.crearCompromisosIniciales(usuarioId)

        _compromisos.value = db.listarCompromisos(usuarioId)
        _historialPagos.value = db.listarPagos(usuarioId)
        _comprobantesCargados.value = true
    }


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



    fun pagarCompromiso(compromiso: Compromiso): ComprobantePago? {
        val usuario = _usuarioActual.value ?: return null

        if (!puedePagar(compromiso)) return null

        val compromisoActual = _compromisos.value
            .find { it.id == compromiso.id }
            ?: return null

        val fechaHora = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale("es", "CL")
        ).format(Date())

        val comprobante = ComprobantePago(
            numero = "PF-PAGO-${System.currentTimeMillis()}",
            fechaHora = fechaHora,
            empresa = compromisoActual.empresa,
            concepto = compromisoActual.concepto,
            cuota = compromisoActual.cuota,
            folioCompromiso = compromisoActual.folio,
            monto = compromisoActual.monto,
            recargoMora = 0,
            totalPagado = compromisoActual.monto,
            canal = "Aplicación PagoFlex",
            estado = "Confirmado"
        )

        val pagoId = db.insertarPago(usuario.id, comprobante)

        if (pagoId == -1L) return null

        val actualizado = db.actualizarEstadoCompromiso(
            usuarioId = usuario.id,
            compromisoId = compromisoActual.id,
            nuevoEstado = "Pagado"
        )

        if (!actualizado) return null

        _compromisos.value = db.listarCompromisos(usuario.id)
        _historialPagos.value = db.listarPagos(usuario.id)
        _comprobanteActual.value = comprobante

        return comprobante
    }


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
        cargarDatosFinancieros(id)
    }

    fun validarRegistro(usuario: Usuario): String? {
        if (!validarRutChileno(usuario.rut)) {
            return "El RUT ingresado no es válido."
        }

        if (db.existeRut(usuario.rut)) {
            return "Ya existe una cuenta registrada con ese RUT."
        }

        if (db.existeCorreo(usuario.correo)) {
            return "Ya existe una cuenta registrada con ese correo."
        }

        return null
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


    fun validarRutChileno(rutCompleto: String): Boolean {
        val rutLimpio = rutCompleto
            .replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()

        if (rutLimpio.length < 2) return false

        val cuerpo = rutLimpio.dropLast(1)
        val dvRecibido = rutLimpio.last()

        if (!cuerpo.all { it.isDigit() }) return false
        if (!dvRecibido.isDigit() && dvRecibido != 'K') return false

        var suma = 0
        var multiplicador = 2

        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) {
                2
            } else {
                multiplicador + 1
            }
        }

        val resultado = 11 - (suma % 11)

        val dvEsperado = when (resultado) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.digitToChar()
        }

        return dvEsperado == dvRecibido
    }


    fun iniciarSesion(rut: String) {
        val rutLimpio = rut
            .replace(".", "")
            .replace("-", "")
            .trim()

        val usuarioEncontrado = db.listarUsuarios().find { usuario ->
            usuario.rut
                .replace(".", "")
                .replace("-", "")
                .trim()
                .equals(rutLimpio, ignoreCase = true)
        }

        if (usuarioEncontrado != null) {
            _usuarioActual.value = usuarioEncontrado
            sesion.guardar(usuarioEncontrado.id)
            recargar()
            cargarDatosFinancieros(usuarioEncontrado.id)
        }
    }

    fun buscarPorRut(rutIngresado: String): Usuario? {
        val rutLimpio = rutIngresado
            .replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()

        return db.listarUsuarios().find { usuario ->
            usuario.rut
                .replace(".", "")
                .replace("-", "")
                .trim()
                .uppercase() == rutLimpio
        }
    }

    fun actualizarContrasena(rutIngresado: String, nuevaContrasena: String): Boolean {
        // 1. Limpiamos el RUT ingresado (quitar puntos y guión)
        val rutLimpioInput = rutIngresado.replace(".", "").replace("-", "").trim()

        // 2. Buscamos si existe el usuario en la lista cargada
        val usuario = _usuarios.value.find { user ->
            user.rut.replace(".", "").replace("-", "").trim().equals(rutLimpioInput, ignoreCase = true)
        } ?: return false

        // 3. Actualizamos en la base de datos SQLite
        val filasAfectadas = db.actualizarContrasena(usuario.rut, nuevaContrasena)

        if (filasAfectadas > 0) {
            recargar() // Refresca la lista de usuarios en memoria
            return true
        }

        return false
    }

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    // Cargar reportes iniciales al iniciar el ViewModel (si tienes un init)
    init {
        cargarReportes()
    }

    fun cargarReportes() {
        _reportes.value = db.listarReportes()
    }

    fun guardarReporteEnBD(motivo: String, detalle: String) {
        val usuario = _usuarioActual.value
        val nuevoReporte = Reporte(
            clienteNombre = usuario?.nombre ?: "Usuario anónimo",
            clienteCorreo = usuario?.correo ?: "sin_correo@pago.cl",
            motivo = motivo,
            detalle = detalle,
            fechaHora = "26-10-2026 13:00",
            estado = "En revisión"
        )

        // 1. Guardar en SQLite
        db.insertarReporte(nuevoReporte)

        // 2. Refrescar la lista observable para que la pantalla del Agente se actualice
        cargarReportes()
    }
}
