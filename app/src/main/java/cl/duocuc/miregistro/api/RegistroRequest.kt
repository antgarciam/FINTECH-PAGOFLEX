package cl.duocuc.miregistro.model

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val rut: String,
    val contrasena: String,
    val ciudad: String,
    val preguntaSeguridad: String,
    val respuestaSeguridad: String
)