package cl.duocuc.miregistro.model

data class Reporte(
    val id : Long = System.currentTimeMillis(),
    val clienteNombre: String,
    val clienteCorreo : String,
    val motivo: String,
    val detalle: String,
    val estado: String = "En revisión",
    val fechaHora: String,
    val respuestaAgente : String? = null
)
