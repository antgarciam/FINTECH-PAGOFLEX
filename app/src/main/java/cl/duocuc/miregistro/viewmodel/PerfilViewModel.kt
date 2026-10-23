package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.Perfil
import cl.duocuc.miregistro.model.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PerfilViewModel : ViewModel() {
    private val perfilEjemplo = Perfil(
        nombre = "Camila Andrea Rojas Peña",
        profesion = "Rol: R-01 (Usuario final)",
        ciudad = "Maipú",
        descripcion = "crojas@correo.ex",
        seguidores = 0,
        siguiendo = 0,
        publicaciones = 0,
        intereses = listOf("PagoFlex", "Fintech","Mis Cuentas")
    )

    private val _uiState = MutableStateFlow(PerfilUiState(perfil = perfilEjemplo))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()
}
