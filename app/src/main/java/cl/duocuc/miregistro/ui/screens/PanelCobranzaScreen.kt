package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.MetricasCobranza

@Composable
fun PanelCobranzaScreen(metricas: MetricasCobranza, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Panel Financiero", style = MaterialTheme.typography.headlineMedium)
        Text("Empresa: ${metricas.empresa}")
        Text("Recaudado: $${metricas.totalRecaudado}")
        Text("Tasa: ${metricas.tasaCobranza}")
        Text("Pagados: ${metricas.pagados} | Pendientes: ${metricas.pendientes} | Vencidos: ${metricas.vencidos}")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Crear compromiso con cuotas") }
    }
}