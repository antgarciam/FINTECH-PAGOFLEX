package cl.duocuc.miregistro.model

data class Compromiso(
    val id: Long,
    val empresa: String,
    val concepto: String,
    val cuota: String,
    val monto: Int,
    val vencimiento: String,
    val estado: String,
    val folio: String
)
