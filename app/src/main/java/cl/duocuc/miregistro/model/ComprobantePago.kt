
package cl.duocuc.miregistro.model

data class ComprobantePago(
    val numero: String,
    val fechaHora: String,
    val empresa: String,
    val concepto: String,
    val cuota: String,
    val folioCompromiso: String,
    val monto: Int,
    val recargoMora: Int,
    val totalPagado: Int,
    val canal: String,
    val estado: String
)
