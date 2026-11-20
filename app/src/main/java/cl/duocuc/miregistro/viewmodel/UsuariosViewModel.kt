package cl.duocuc.miregistro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.api.CiudadDto
import cl.duocuc.miregistro.api.CrearCompromisoRequest
import cl.duocuc.miregistro.api.CrearReporteRequest
import cl.duocuc.miregistro.api.CuotaDto
import cl.duocuc.miregistro.api.NotificacionDto
import cl.duocuc.miregistro.api.RecuperarRequest
import cl.duocuc.miregistro.api.RegistroRequest
import cl.duocuc.miregistro.api.RetrofitClient
import cl.duocuc.miregistro.api.UsuarioDto
import cl.duocuc.miregistro.data.ConvertidorImagen
import cl.duocuc.miregistro.data.PagoRepository
import cl.duocuc.miregistro.data.ReporteRepository
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioRepository
import cl.duocuc.miregistro.model.ComprobantePago
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.MetricasCobranza
import cl.duocuc.miregistro.model.Reporte
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val sesion = SesionPreferencias(application)
    private val userRepo = UsuarioRepository(
        RetrofitClient.authApi,
        RetrofitClient.usuariosApi,
        sesion
    )
    private val pagoRepo = PagoRepository(RetrofitClient.pagosApi)
    private val repRepo = ReporteRepository(RetrofitClient.reportesApi)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorGeneral = MutableStateFlow<String?>(null)
    val errorGeneral = _errorGeneral.asStateFlow()

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito = _mensajeExito.asStateFlow()

    private val _errorRut = MutableStateFlow<String?>(null)
    val errorRut = _errorRut.asStateFlow()

    private val _errorNombre = MutableStateFlow<String?>(null)
    val errorNombre = _errorNombre.asStateFlow()

    private val _errorCorreo = MutableStateFlow<String?>(null)
    val errorCorreo = _errorCorreo.asStateFlow()

    private val _errorCiudad = MutableStateFlow<String?>(null)
    val errorCiudad = _errorCiudad.asStateFlow()

    private val _errorPregunta = MutableStateFlow<String?>(null)
    val errorPregunta = _errorPregunta.asStateFlow()

    private val _errorRespuesta = MutableStateFlow<String?>(null)
    val errorRespuesta = _errorRespuesta.asStateFlow()

    private val _errorPassword = MutableStateFlow<String?>(null)
    val errorPassword = _errorPassword.asStateFlow()

    private val _errorConfirmPassword = MutableStateFlow<String?>(null)
    val errorConfirmPassword = _errorConfirmPassword.asStateFlow()

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso = _registroExitoso.asStateFlow()

    private val _recuperacionExitosa = MutableStateFlow(false)
    val recuperacionExitosa = _recuperacionExitosa.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual = _usuarioActual.asStateFlow()

    private val _ciudades = MutableStateFlow<List<CiudadDto>>(emptyList())
    val ciudades = _ciudades.asStateFlow()

    private val _usuariosSistema = MutableStateFlow<List<UsuarioDto>>(emptyList())
    val usuariosSistema = _usuariosSistema.asStateFlow()

    private val _cuotasUsuario = MutableStateFlow<List<CuotaDto>>(emptyList())
    val cuotasUsuario = _cuotasUsuario.asStateFlow()

    private val _ultimoComprobante = MutableStateFlow<ComprobantePago?>(null)
    val ultimoComprobante = _ultimoComprobante.asStateFlow()

    private val _historialPagos = MutableStateFlow<List<ComprobantePago>>(emptyList())
    val historialPagos = _historialPagos.asStateFlow()

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes = _reportes.asStateFlow()

    private val _notificaciones = MutableStateFlow<List<NotificacionDto>>(emptyList())
    val notificaciones = _notificaciones.asStateFlow()

    val preguntasSeguridad = listOf(
        "¿Cuál es el nombre de tu primera mascota?",
        "¿En qué ciudad naciste?",
        "¿Cuál es el nombre de tu colegio básico?",
        "¿Cuál es tu comida favorita?"
    )

    init {
        cargarCiudades()
        restaurarSesionSiExiste()
    }

    private fun restaurarSesionSiExiste() {
        val id = sesion.leerUsuarioId()
        val rol = sesion.leerRolId()
        if (id > 0L && rol > 0) {
            _usuarioActual.value = Usuario(
                id = id,
                nombre = sesion.leerNombre(),
                correo = sesion.leerCorreo(),
                ciudad = sesion.leerCiudad(),
                rolId = rol.toLong(),
                rolNombre = nombreDeRol(rol),
                rut = sesion.leerRut()
            )
            cargarDatosSegunRol(id, rol)
        }
    }

    fun nombreDeRol(rolId: Int): String = when (rolId) {
        1 -> "Administrador"
        2 -> "Agente de Soporte"
        4 -> "Ejecutivo de Cobranza"
        else -> "Usuario Final"
    }

    fun cargarCiudades() {
        viewModelScope.launch {
            userRepo.listarCiudades().onSuccess { lista ->
                _ciudades.value = lista
            }
        }
    }

    fun limpiarMensajes() {
        _errorGeneral.value = null
        _mensajeExito.value = null
        _errorRut.value = null
        _errorNombre.value = null
        _errorCorreo.value = null
        _errorCiudad.value = null
        _errorPregunta.value = null
        _errorRespuesta.value = null
        _errorPassword.value = null
        _errorConfirmPassword.value = null
    }

    fun validarRutChileno(rutInput: String): Boolean {
        val limpio = rutInput.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 8 || limpio.length > 9) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false
        var suma = 0
        var multiplo = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * multiplo
            multiplo = if (multiplo == 7) 2 else multiplo + 1
        }
        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return dv == dvEsperado
    }

    fun normalizarRut(rutInput: String): String {
        val limpio = rutInput.replace(".", "").replace("-", "").trim().uppercase()
        return if (limpio.length >= 2) {
            "${limpio.dropLast(1)}-${limpio.last()}"
        } else rutInput.trim()
    }

    fun validarContrasena(pass: String): String? {
        if (pass.length < 8) {
            return "La contraseña es muy corta (mínimo 8 caracteres). Agrega más letras, números y un símbolo."
        }
        if (!pass.any { it.isUpperCase() }) {
            return "Falta una letra mayúscula. Incluye al menos una letra entre A y Z."
        }
        if (!pass.any { it.isLowerCase() }) {
            return "Falta una letra minúscula. Incluye al menos una letra entre a y z."
        }
        if (!pass.any { it.isDigit() }) {
            return "Falta un número. Agrega al menos un dígito del 0 al 9."
        }
        if (!pass.any { !it.isLetterOrDigit() }) {
            return "Falta un carácter especial. Incluye al menos un símbolo como @, #, $, ! o %."
        }
        return null
    }

    fun ejecutarLogin(rut: String, contrasena: String) {
        limpiarMensajes()
        var valido = true

        if (rut.isBlank()) {
            _errorRut.value = "El RUT es obligatorio. Ingrésalo con guión y dígito verificador (ej: 12345678-9)."
            valido = false
        } else if (!validarRutChileno(rut)) {
            _errorRut.value = "El RUT ingresado no es válido. Revisa los números y el dígito verificador."
            valido = false
        }

        if (contrasena.isBlank()) {
            _errorPassword.value = "Debes ingresar tu contraseña para continuar."
            valido = false
        }

        if (!valido) return

        val rutNormalizado = normalizarRut(rut)
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.login(rutNormalizado, contrasena)
                .onSuccess { resp ->
                    _usuarioActual.value = Usuario(
                        id = resp.id,
                        nombre = resp.nombre,
                        correo = resp.correo,
                        ciudad = resp.ciudad,
                        rolId = resp.rolId.toLong(),
                        rolNombre = nombreDeRol(resp.rolId),
                        rut = rutNormalizado
                    )
                    cargarDatosSegunRol(resp.id, resp.rolId)
                    _loginExitoso.value = true
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    private fun cargarDatosSegunRol(usuarioId: Long, rolId: Int) {
        when (rolId) {
            1 -> {
                cargarUsuariosSistema()
                cargarReportes()
            }
            2 -> cargarReportes()
            4 -> cargarUsuariosSistema()
            else -> {
                cargarCuotasUsuario(usuarioId)
                cargarNotificaciones(usuarioId)
            }
        }
    }

    fun ejecutarRegistro(
        nombre: String,
        rut: String,
        correo: String,
        ciudad: String,
        preguntaSeguridad: String,
        respuestaSeguridad: String,
        pass: String,
        confirmPass: String
    ) {
        limpiarMensajes()
        var esValido = true

        if (nombre.trim().length < 3 || nombre.any { it.isDigit() }) {
            _errorNombre.value = "Nombre inválido. Ingresa tu nombre y apellido sin números (mínimo 3 letras)."
            esValido = false
        }
        if (!validarRutChileno(rut)) {
            _errorRut.value = "RUT inválido. Escríbelo con guión y dígito verificador correcto (ej: 12345678-9)."
            esValido = false
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            _errorCorreo.value = "Correo inválido. Incluye '@' y un dominio válido (ej: usuario@correo.cl)."
            esValido = false
        }
        if (ciudad.isBlank()) {
            _errorCiudad.value = "Debes seleccionar una ciudad de la lista desplegable."
            esValido = false
        }
        if (preguntaSeguridad.isBlank()) {
            _errorPregunta.value = "Selecciona una pregunta de seguridad para proteger tu cuenta."
            esValido = false
        }
        if (respuestaSeguridad.trim().length < 2) {
            _errorRespuesta.value = "Ingresa una respuesta de al menos 2 caracteres para recuperar tu clave si la olvidas."
            esValido = false
        }

        val errorPass = validarContrasena(pass)
        if (errorPass != null) {
            _errorPassword.value = errorPass
            esValido = false
        }
        if (confirmPass != pass) {
            _errorConfirmPassword.value = "Las contraseñas no coinciden. Vuelve a escribir la misma clave en ambos campos."
            esValido = false
        }

        if (!esValido) return

        val rutNormalizado = normalizarRut(rut)
        viewModelScope.launch {
            _isLoading.value = true
            val req = RegistroRequest(
                nombre = nombre.trim(),
                correo = correo.trim(),
                rut = rutNormalizado,
                contrasena = pass,
                ciudad = ciudad,
                preguntaSeguridad = preguntaSeguridad,
                respuestaSeguridad = respuestaSeguridad.trim()
            )
            userRepo.registrar(req)
                .onSuccess {
                    _mensajeExito.value = "Cuenta creada correctamente. Ahora inicia sesión con tu RUT."
                    _registroExitoso.value = true
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun ejecutarRecuperarClave(
        rut: String,
        respuestaSeguridad: String,
        nuevaClave: String,
        confirmarClave: String
    ) {
        limpiarMensajes()
        var valido = true

        if (!validarRutChileno(rut)) {
            _errorRut.value = "RUT inválido. Escríbelo con guión y dígito verificador correcto (ej: 12345678-9)."
            valido = false
        }
        if (respuestaSeguridad.isBlank()) {
            _errorRespuesta.value = "Debes ingresar la respuesta de seguridad que registraste al crear tu cuenta."
            valido = false
        }
        val errPass = validarContrasena(nuevaClave)
        if (errPass != null) {
            _errorPassword.value = errPass
            valido = false
        }
        if (confirmarClave != nuevaClave) {
            _errorConfirmPassword.value = "Las contraseñas no coinciden. Escribe exactamente la misma clave."
            valido = false
        }

        if (!valido) return

        val rutNormalizado = normalizarRut(rut)
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.recuperarClave(
                RecuperarRequest(
                    rut = rutNormalizado,
                    respuestaSeguridad = respuestaSeguridad.trim(),
                    nuevaContrasena = nuevaClave
                )
            ).onSuccess {
                _mensajeExito.value = "Contraseña restablecida con éxito. Ya puedes iniciar sesión."
                _recuperacionExitosa.value = true
            }.onFailure {
                _errorGeneral.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun resetFlagsAuth() {
        _registroExitoso.value = false
        _recuperacionExitosa.value = false
        limpiarMensajes()
    }

    fun actualizarPerfilPropio(nombre: String, correo: String, ciudad: String) {
        limpiarMensajes()
        val actual = _usuarioActual.value ?: return
        var valido = true

        if (nombre.trim().length < 3 || nombre.any { it.isDigit() }) {
            _errorNombre.value = "Nombre inválido. Escribe al menos 3 letras sin números."
            valido = false
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            _errorCorreo.value = "Correo inválido. Incluye '@' y un dominio válido."
            valido = false
        }
        if (ciudad.isBlank()) {
            _errorCiudad.value = "Selecciona una ciudad válida de la lista."
            valido = false
        }
        if (!valido) return

        viewModelScope.launch {
            _isLoading.value = true
            userRepo.actualizarUsuario(actual.id, nombre.trim(), correo.trim(), ciudad, actual.rolId.toInt())
                .onSuccess { dto ->
                    _usuarioActual.value = actual.copy(
                        nombre = dto.nombre,
                        correo = dto.correo,
                        ciudad = dto.ciudad
                    )
                    _mensajeExito.value = "Datos de perfil actualizados correctamente."
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun cambiarClavePropia(actual: String, nueva: String, confirmar: String) {
        limpiarMensajes()
        var valido = true

        if (actual.isBlank()) {
            _errorGeneral.value = "Debes escribir tu contraseña actual para autorizar el cambio."
            valido = false
        }
        val errNueva = validarContrasena(nueva)
        if (errNueva != null) {
            _errorPassword.value = errNueva
            valido = false
        }
        if (confirmar != nueva) {
            _errorConfirmPassword.value = "La confirmación no coincide con la nueva contraseña."
            valido = false
        }
        if (!valido) return

        viewModelScope.launch {
            _isLoading.value = true
            userRepo.cambiarClave(actual, nueva)
                .onSuccess {
                    _mensajeExito.value = "Contraseña actualizada de forma segura."
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun actualizarAvatarLocal(bitmap: Bitmap) {
        val bytes = ConvertidorImagen.aBytes(bitmap)
        _usuarioActual.value = _usuarioActual.value?.copy(avatar = bytes)
    }

    fun actualizarAvatarDesdeUri(uri: Uri) {
        val bitmap = ConvertidorImagen.desdeUri(getApplication(), uri) ?: return
        actualizarAvatarLocal(bitmap)
    }

    fun cargarCuotasUsuario(usuarioId: Long = _usuarioActual.value?.id ?: sesion.leerUsuarioId()) {
        if (usuarioId <= 0L) return
        viewModelScope.launch {
            _isLoading.value = true
            pagoRepo.listarCuotasUsuario(usuarioId)
                .onSuccess { lista ->
                    _cuotasUsuario.value = lista
                    _historialPagos.value = lista
                        .filter { it.estado.equals("Pagado", ignoreCase = true) }
                        .map { cuota ->
                            ComprobantePago(
                                numero = "CPR-${cuota.idCuota.toString().padStart(6, '0')}",
                                fechaHora = cuota.vencimiento,
                                empresa = cuota.empresa,
                                concepto = cuota.concepto,
                                cuota = "${cuota.cuotaActual}/${cuota.totalCuotas}",
                                folioCompromiso = "CMP-${cuota.compromisoId.toString().padStart(5, '0')}",
                                monto = cuota.monto,
                                recargoMora = 0,
                                totalPagado = cuota.monto,
                                canal = "App Móvil PagoFlex",
                                estado = "Pagado"
                            )
                        }
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun pagarCuotaSeleccionada(cuota: CuotaDto, onComprobanteListo: () -> Unit) {
        limpiarMensajes()
        viewModelScope.launch {
            _isLoading.value = true
            pagoRepo.pagarCuota(cuota.idCuota)
                .onSuccess {
                    val ahora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                    val comprobante = ComprobantePago(
                        numero = "CPR-${cuota.idCuota.toString().padStart(6, '0')}",
                        fechaHora = ahora,
                        empresa = cuota.empresa,
                        concepto = cuota.concepto,
                        cuota = "${cuota.cuotaActual}/${cuota.totalCuotas}",
                        folioCompromiso = "CMP-${cuota.compromisoId.toString().padStart(5, '0')}",
                        monto = cuota.monto,
                        recargoMora = 0,
                        totalPagado = cuota.monto,
                        canal = "App Móvil PagoFlex",
                        estado = "Pagado"
                    )
                    _ultimoComprobante.value = comprobante
                    cargarCuotasUsuario()
                    onComprobanteListo()
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun generarVistaPreviaCuotas(montoTotal: Int, numeroCuotas: Int, fechaInicio: String): List<Compromiso> {
        if (montoTotal <= 0 || numeroCuotas <= 0 || fechaInicio.isBlank()) return emptyList()
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val fechaBase = try {
            formato.parse(fechaInicio) ?: return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }
        val montoCuota = montoTotal / numeroCuotas
        val cal = Calendar.getInstance()
        return (1..numeroCuotas).map { i ->
            cal.time = fechaBase
            cal.add(Calendar.MONTH, i)
            Compromiso(
                id = i.toLong(),
                empresa = "Vista previa",
                concepto = "Cuota $i de $numeroCuotas",
                cuota = i,
                monto = montoCuota.toDouble(),
                vencimiento = formato.format(cal.time),
                folio = "PREV-$i",
                estado = "Pendiente"
            )
        }
    }

    fun crearCompromisoConCuotas(
        rutCliente: String,
        empresa: String,
        concepto: String,
        montoTotal: Int,
        numeroCuotas: Int,
        fechaInicio: String,
        onCreado: () -> Unit
    ) {
        limpiarMensajes()
        if (!validarRutChileno(rutCliente)) {
            _errorRut.value = "RUT inválido. Ingresa un RUT chileno válido con guión y dígito verificador."
            return
        }
        val rutNorm = normalizarRut(rutCliente)
        val cliente = _usuariosSistema.value.find {
            normalizarRut(it.rut).equals(rutNorm, ignoreCase = true)
        }
        if (cliente == null) {
            _errorRut.value = "No existe un usuario activo con ese RUT. Verifica el RUT del cliente."
            return
        }
        if (empresa.isBlank() || concepto.isBlank() || montoTotal <= 0 || numeroCuotas !in 1..36 || fechaInicio.isBlank()) {
            _errorGeneral.value = "Completa empresa, concepto, monto mayor a 0, cuotas (1-36) y fecha con el calendario."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            pagoRepo.crearCompromiso(
                CrearCompromisoRequest(
                    usuarioId = cliente.id,
                    empresa = empresa.trim(),
                    concepto = concepto.trim(),
                    montoTotal = montoTotal,
                    numeroCuotas = numeroCuotas,
                    fechaInicio = fechaInicio
                )
            ).onSuccess {
                _mensajeExito.value = "Compromiso creado con $numeroCuotas cuotas automáticas para ${cliente.nombre}."
                onCreado()
            }.onFailure {
                _errorGeneral.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun calcularMetricasCobranza(): MetricasCobranza {
        val todas = _cuotasUsuario.value
        val pagados = todas.count { it.estado.equals("Pagado", ignoreCase = true) }
        val vencidos = todas.count { it.estado.equals("Vencido", ignoreCase = true) }
        val pendientes = todas.count { it.estado.equals("Pendiente", ignoreCase = true) }
        val recaudado = todas.filter { it.estado.equals("Pagado", ignoreCase = true) }.sumOf { it.monto }
        val tasa = if (todas.isEmpty()) 100 else (pagados * 100) / todas.size
        return MetricasCobranza(
            empresa = "PagoFlex",
            tasaCobranza = "$tasa%",
            totalRecaudado = recaudado.toString(),
            pagados = pagados,
            pendientes = pendientes,
            vencidos = vencidos
        )
    }

    fun cargarUsuariosSistema() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.listarUsuarios()
                .onSuccess { _usuariosSistema.value = it }
                .onFailure { _errorGeneral.value = it.message }
            _isLoading.value = false
        }
    }

    fun actualizarUsuarioAdmin(usuario: UsuarioDto, nombre: String, correo: String, ciudad: String, rolId: Int) {
        limpiarMensajes()
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.actualizarUsuario(usuario.id, nombre.trim(), correo.trim(), ciudad, rolId)
                .onSuccess {
                    _mensajeExito.value = "Usuario actualizado correctamente."
                    cargarUsuariosSistema()
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun desactivarUsuarioAdmin(usuarioId: Long, onDesactivado: () -> Unit) {
        limpiarMensajes()
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.desactivarUsuario(usuarioId)
                .onSuccess {
                    _mensajeExito.value = "Usuario desactivado sin eliminar su historial financiero."
                    cargarUsuariosSistema()
                    onDesactivado()
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _isLoading.value = true
            repRepo.listarReportes()
                .onSuccess { _reportes.value = it }
                .onFailure { _errorGeneral.value = it.message }
            _isLoading.value = false
        }
    }

    fun crearReporteUsuario(motivo: String, detalle: String) {
        val actual = _usuarioActual.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repRepo.crearReporte(
                CrearReporteRequest(
                    clienteNombre = actual.nombre,
                    clienteCorreo = actual.correo,
                    motivo = motivo,
                    detalle = detalle
                )
            ).onSuccess {
                _mensajeExito.value = "Reporte enviado correctamente."
                cargarReportes()
            }.onFailure {
                _errorGeneral.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun resolverReporteAgente(reporteId: Long, respuesta: String, nuevoEstado: String, onResuelto: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repRepo.actualizarReporte(reporteId, nuevoEstado, respuesta)
                .onSuccess {
                    cargarReportes()
                    onResuelto()
                }
                .onFailure {
                    _errorGeneral.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun cargarNotificaciones(usuarioId: Long = _usuarioActual.value?.id ?: 0L) {
        if (usuarioId <= 0L) return
        viewModelScope.launch {
            try {
                val res = RetrofitClient.notificacionesApi.listarNotificaciones(usuarioId)
                if (res.isSuccessful && res.body() != null) {
                    _notificaciones.value = res.body()!!
                }
            } catch (_: Exception) {
            }
        }
    }

    fun obtenerRolActual(): Int = sesion.leerRolId()

    fun sesionBorrar() {
        sesion.borrar()
        _usuarioActual.value = null
        _cuotasUsuario.value = emptyList()
        _historialPagos.value = emptyList()
        _loginExitoso.value = false
        _registroExitoso.value = false
        _recuperacionExitosa.value = false
        limpiarMensajes()
    }
}