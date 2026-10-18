package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.FormularioUiState
import cl.duocuc.miregistro.model.NivelExperiencia
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registroExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }

    fun onCiudadSeleccionada(ciudad: String) {
        _uiState.update { it.copy(ciudad = ciudad) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onNivelSeleccionado(nivel: NivelExperiencia) {
        _uiState.update { it.copy(nivel = nivel) }
    }

    fun onNoticiasCambia(valor: Boolean) {
        _uiState.update { it.copy(recibirNoticias = valor) }
    }

    fun onTerminosCambia(valor: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    fun onRegistrar(): Usuario? {
        val estado = _uiState.value

        val errorNombre = if (estado.nombre.isBlank()) "El nombre es obligatorio" else null
        val errorCorreo = if (!estado.correo.contains("@") || !estado.correo.contains("."))
            "Ingresa un correo válido" else null
        val errorContrasena = if (estado.contrasena.length < 6)
            "Mínimo 6 caracteres" else null
        val errorTerminos = if (!estado.aceptaTerminos)
            "Debes aceptar los términos" else null

        val hayErrores = listOf(errorNombre, errorCorreo, errorContrasena, errorTerminos)
            .any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                errorTerminos = errorTerminos,
                registroExitoso = !hayErrores
            )
        }

        return if (hayErrores) null else Usuario(estado.nombre, estado.correo, estado.ciudad)
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}
