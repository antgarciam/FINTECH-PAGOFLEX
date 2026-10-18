package cl.duocuc.miregistro.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val ciudad: String,
    val id: Long = 0,
    val rolId: Long = Rol.ID_ESTUDIANTE,
    val rolNombre: String = "",
    // NUEVO (Avatar): la foto como BYTES (lo que se guarda en la columna BLOB).
    // null = el usuario aún no elige foto -> se muestra el avatar por defecto.
    // Va al final y con valor por defecto: el resto del proyecto no cambia.
    val avatar: ByteArray? = null
)
