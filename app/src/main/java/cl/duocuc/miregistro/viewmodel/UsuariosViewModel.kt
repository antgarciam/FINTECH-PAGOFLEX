package cl.duocuc.miregistro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.data.ConvertidorImagen
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.ComprobantePago
import cl.duocuc.miregistro.model.Reporte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UsuarioDbHelper(application)

    private val _comprobanteActual = MutableStateFlow<ComprobantePago?>(null)
    val comprobanteActual: StateFlow<ComprobantePago?> = _comprobanteActual.asStateFlow()

    private val _historialPagos = MutableStateFlow<List<ComprobantePago>>(emptyList())
    val historialPagos: StateFlow<List<ComprobantePago>> = _historialPagos.asStateFlow()

    private val _compromisos = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisos: StateFlow<List<Compromiso>> = _compromisos.asStateFlow()

    private val _comprobantesCargados = MutableStateFlow(false)

    private fun cargarDatosFinancieros(usuarioId: Long) {
        db.crearCompromisosIniciales(usuarioId)
        _compromisos.value = db.listarCompromisos(usuarioId)
        _historialPagos.value = db.listarPagos(usuarioId)
        _comprobantesCargados.value = true
    }

    fun cargarCompromisos(usuarioId: Long) {
        _compromisos.value = db.listarCompromisos(usuarioId)
    }

    fun puedePagar(compromiso: Compromiso): Boolean {
        if (compromiso.estado.equals("Vencido", ignoreCase = true)) {
            return true
        }

        if (!compromiso.estado.equals("Pendiente", ignoreCase = true)) {
            return false
        }

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

    fun cambiarAvatarDesdeCamara(foto: Bitmap) {
        guardarAvatar(ConvertidorImagen.aBytes(foto))
    }

    fun cambiarAvatarDesdeGaleria(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri) ?: return
        guardarAvatar(ConvertidorImagen.aBytes(imagen))
    }

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
        val rutLimpioInput = rutIngresado.replace(".", "").replace("-", "").trim()

        val usuario = _usuarios.value.find { user ->
            user.rut.replace(".", "").replace("-", "").trim().equals(rutLimpioInput, ignoreCase = true)
        } ?: return false

        val filasAfectadas = db.actualizarContrasena(usuario.rut, nuevaContrasena)

        if (filasAfectadas > 0) {
            recargar()
            return true
        }

        return false
    }

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

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

        db.insertarReporte(nuevoReporte)
        cargarReportes()
    }

    // --- OPERACIONES DE COMPROMISOS (Trello) ---

    fun crearNuevoCompromiso(rut: String, concepto: String, monto: Int, vencimiento: String): Boolean {
        val usuario = _usuarios.value.firstOrNull {
            it.rut.replace(".", "").replace("-", "").equals(rut.replace(".", "").replace("-", ""), ignoreCase = true)
        }
        val usuarioId = usuario?.id ?: usuarioActual.value?.id ?: return false

        val nuevoCompromiso = Compromiso(
            id = 0L,
            empresa = "Altos del Maipo",
            concepto = concepto,
            cuota = "1 de 1",
            monto = monto,
            vencimiento = vencimiento,
            estado = "Pendiente",
            folio = "PF-${(1000..9999).random()}"
        )

        val exito = db.actualizarEstadoCompromiso(usuarioId, nuevoCompromiso.id, "Pendiente")
        cargarCompromisos(usuarioId)
        return true
    }

    fun anularCompromiso(compromisoId: Long, motivo: String): Boolean {
        if (motivo.isBlank()) return false
        val compromiso = _compromisos.value.firstOrNull { it.id == compromisoId } ?: return false
        if (compromiso.estado.equals("Pagado", ignoreCase = true)) return false

        val usuarioId = usuarioActual.value?.id ?: 1L
        val actualizado = db.actualizarEstadoCompromiso(usuarioId, compromisoId, "Anulado")
        if (actualizado) {
            cargarCompromisos(usuarioId)
        }
        return actualizado
    }

    // --- PANEL DE COBRANZA MENSUAL (Trello) ---

    data class MetricasCobranza(
        val empresa: String,
        val pagados: Int,
        val pendientes: Int,
        val vencidos: Int,
        val totalRecaudado: Int,
        val tasaCobranza: String
    )

    fun obtenerMetricasCobranza(empresaFiltro: String = "Altos del Maipo"): MetricasCobranza {
        val compromisosEmpresa = _compromisos.value.filter {
            it.empresa.equals(empresaFiltro, ignoreCase = true)
        }

        val pagados = compromisosEmpresa.count { it.estado.equals("Pagado", ignoreCase = true) }
        val pendientes = compromisosEmpresa.count { it.estado.equals("Pendiente", ignoreCase = true) }
        val vencidos = compromisosEmpresa.count { it.estado.equals("Vencido", ignoreCase = true) }

        val totalRecaudado = compromisosEmpresa
            .filter { it.estado.equals("Pagado", ignoreCase = true) }
            .sumOf { it.monto }

        val totalCompromisos = compromisosEmpresa.size

        val tasaDecimal = if (totalCompromisos > 0) {
            (pagados.toDouble() / totalCompromisos.toDouble()) * 100.0
        } else {
            0.0
        }

        val tasaFormateada = String.format(Locale("es", "CL"), "%.1f%%", tasaDecimal)

        return MetricasCobranza(
            empresa = empresaFiltro,
            pagados = pagados,
            pendientes = pendientes,
            vencidos = vencidos,
            totalRecaudado = totalRecaudado,
            tasaCobranza = tasaFormateada
        )
    }

    fun actualizarEstadoReporteEnBD(id: Long, nuevoEstado: String, respuesta: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            db.actualizarEstadoReporte(id, nuevoEstado, respuesta)
            cargarReportes() // Recarga la lista para que la interfaz se actualice al instante
        }
    }
}