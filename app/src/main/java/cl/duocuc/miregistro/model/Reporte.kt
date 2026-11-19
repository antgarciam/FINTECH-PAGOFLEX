package cl.duocuc.miregistro.model

data class Reporte(
    val id: Long = 0,
    val estado: String = "Abierto",
    val clienteNombre: String = "",
    val clienteCorreo: String = "",
    val motivo: String = "",
    val detalle: String = "",
    val fechaHora: String = "",
    val respuestaAgente: String = ""
)