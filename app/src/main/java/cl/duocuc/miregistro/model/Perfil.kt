package cl.duocuc.miregistro.model

data class Perfil(
    val nombre: String,
    val profesion: String,
    val ciudad: String,
    val descripcion: String,
    val seguidores: Int,
    val siguiendo: Int,
    val publicaciones: Int,
    val intereses: List<String>
)

data class PerfilUiState(
    val perfil: Perfil,
    val loSigo: Boolean = false
)
