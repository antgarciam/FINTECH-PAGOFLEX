package cl.duocuc.miregistro.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_ESTUDIANTE = 3L
    }
}
