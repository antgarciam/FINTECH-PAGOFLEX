package cl.duocuc.miregistro.model

data class MetricasCobranza(
    val empresa: String = "PagoFlex",
    val tasaCobranza: String = "85%",
    val totalRecaudado: String = "0",
    val pagados: Int = 0,
    val pendientes: Int = 0,
    val vencidos: Int = 0
)