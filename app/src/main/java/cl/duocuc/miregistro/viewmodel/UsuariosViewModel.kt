package cl.duocuc.miregistro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.miregistro.api.RegistroRequest
import cl.duocuc.miregistro.api.RetrofitClient
import cl.duocuc.miregistro.data.ReporteRepository
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioRepository
import cl.duocuc.miregistro.model.Reporte
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val sesion = SesionPreferencias(application)
    private val userRepo = UsuarioRepository(RetrofitClient.authApi, sesion)
    private val repRepo = ReporteRepository(RetrofitClient.reportesApi)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _errorGeneral = MutableStateFlow<String?>(null)
    val errorGeneral = _errorGeneral.asStateFlow()
    private val _errorRut = MutableStateFlow<String?>(null)
    val errorRut = _errorRut.asStateFlow()
    private val _errorNombre = MutableStateFlow<String?>(null)
    val errorNombre = _errorNombre.asStateFlow()
    private val _errorCorreo = MutableStateFlow<String?>(null)
    val errorCorreo = _errorCorreo.asStateFlow()
    private val _errorPassword = MutableStateFlow<String?>(null)
    val errorPassword = _errorPassword.asStateFlow()
    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()
    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso = _registroExitoso.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual = _usuarioActual.asStateFlow()
    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes = _reportes.asStateFlow()

    init {
        // Usuario de prueba para ver el panel de Agente
        _usuarioActual.value = Usuario(nombre = "Agente Test", correo = "a@a.cl", ciudad = "Santiago", rolId = 2L)

        // ⚠️ SI SOLO QUIERES PROBAR EL LOGIN, COMENTA ESTA LÍNEA:
        // cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _isLoading.value = true
            repRepo.listarReportes().onSuccess { _reportes.value = it }.onFailure { _errorGeneral.value = it.message }
            _isLoading.value = false
        }
    }

    fun ejecutarLogin(rut: String, contrasena: String) {
        _errorGeneral.value = null
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.login(rut, contrasena).onSuccess { _loginExitoso.value = true }.onFailure { _errorGeneral.value = it.message }
            _isLoading.value = false
        }
    }

    fun ejecutarRegistro(nombre: String, rut: String, correo: String, ciudad: String, pass: String, confirmPass: String) {
        _errorRut.value = null; _errorNombre.value = null; _errorCorreo.value = null; _errorPassword.value = null; _errorGeneral.value = null
        var esValido = true

        if (nombre.isBlank() || nombre.any { it.isDigit() }) { _errorNombre.value = "Nombre inválido"; esValido = false }
        if (!rut.matches(Regex("^[0-9]{1,2}\\.?[0-9]{3}\\.?[0-9]{3}-?[0-9kK]{1}$"))) { _errorRut.value = "RUT inválido (ej: 12345678-9)"; esValido = false }
        if (!correo.contains("@")) { _errorCorreo.value = "Correo inválido"; esValido = false }

        val errorPass = validarContrasena(pass)
        if (errorPass != null) { _errorPassword.value = errorPass; esValido = false }
        else if (pass != confirmPass) { _errorPassword.value = "Las claves no coinciden"; esValido = false }

        if (!esValido) return

        viewModelScope.launch {
            _isLoading.value = true
            val req = RegistroRequest(rut, nombre, correo, pass, ciudad, "pregunta", "respuesta")
            userRepo.registrar(req).onSuccess { _registroExitoso.value = true }.onFailure { _errorGeneral.value = it.message }
            _isLoading.value = false
        }
    }

    private fun validarContrasena(pass: String): String? {
        if (pass.length < 8) return "Mínimo 8 caracteres"
        if (!pass.any { it.isUpperCase() }) return "Falta mayúscula"
        if (!pass.any { it.isLowerCase() }) return "Falta minúscula"
        if (!pass.any { it.isDigit() }) return "Falta número"
        if (!pass.any { !it.isLetterOrDigit() }) return "Falta símbolo (@, #, $)"
        return null
    }
}