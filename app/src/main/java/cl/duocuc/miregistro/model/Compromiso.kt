package cl.duocuc.miregistro.model

data class Compromiso(
    val id: Long = 0,
    val empresa: String = "",
    val concepto: String = "",
    val cuota: Int = 1,
    val monto: Double = 0.0,
    val vencimiento: String = "",
    val folio: String = "",
    val estado: String = "Pendiente",
    val usuarioId: Long = 0
)