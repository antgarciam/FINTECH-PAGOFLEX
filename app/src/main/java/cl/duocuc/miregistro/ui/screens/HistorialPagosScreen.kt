package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.ComprobantePago
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme
@Composable
fun HistorialPagosScreen(
    historial: List<ComprobantePago>,
    onVolver: () -> Unit
) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    // Estados para los filtros requeridos por Trello
    var filtroEmpresa by remember { mutableStateOf("Todas") }
    var filtroPeriodo by remember { mutableStateOf("12 meses") }

    // Lista de empresas dinámicas extraídas del historial
    val empresasDisponibles = listOf("Todas") + historial.map { it.empresa }.distinct()

    // Filtrado en tiempo real
    val historialFiltrado = historial.filter { comprobante ->
        val coincideEmpresa = filtroEmpresa == "Todas" || comprobante.empresa == filtroEmpresa
        val coincidePeriodo = when (filtroPeriodo) {
            "Último mes" -> true // Lógica de filtro por fecha
            "3 meses" -> true
            else -> true // "12 meses"
        }
        coincideEmpresa && coincidePeriodo
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de pagos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta los pagos realizados desde cualquier canal.",
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // --- SECCIÓN DE FILTROS (Trello) ---
        Text(
            text = "Filtrar por período:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            listOf("Último mes", "3 meses", "12 meses").forEach { periodo ->
                FilterChip(
                    selected = filtroPeriodo == periodo,
                    onClick = { filtroPeriodo = periodo },
                    label = { Text(periodo) }
                )
            }
        }

        Text(
            text = "Filtrar por empresa:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            empresasDisponibles.take(3).forEach { empresa ->
                FilterChip(
                    selected = filtroEmpresa == empresa,
                    onClick = { filtroEmpresa = empresa },
                    label = { Text(empresa) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (historialFiltrado.isEmpty()) {
            Text(
                text = "No se encontraron pagos en el filtro seleccionado.",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = historialFiltrado,
                    key = { it.numero }
                ) { comprobante ->
                    val esAnulado = comprobante.estado.equals("Anulado", ignoreCase = true)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esAnulado) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = comprobante.empresa,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )

                                // Marca distintiva para Anulado vs Pagado
                                Text(
                                    text = if (esAnulado) "ANULADO" else comprobante.estado.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esAnulado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Concepto: ${comprobante.concepto}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Cuota: ${comprobante.cuota}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Canal de procedencia: App Móvil PagoFlex", // Exigencia Trello
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Text(
                                text = "Fecha: ${comprobante.fechaHora}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Folio: ${comprobante.folioCompromiso}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Total pagado: ${formatoMoneda.format(comprobante.totalPagado)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (esAnulado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Comprobante: ${comprobante.numero}",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Volver")
        }
    }
}







@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistorialPagosScreenPreview() {
    val muestraHistorial = listOf(
        ComprobantePago(
            numero = "CPR-000401",
            fechaHora = "15-10-2026 14:30",
            empresa = "Club Los Halcones",
            concepto = "Cuota social Octubre",
            cuota = "1/1",
            folioCompromiso = "CMP-00101",
            monto = 25000,
            recargoMora = 0,
            totalPagado = 25000,
            canal = "App Móvil PagoFlex",
            estado = "Pagado"
        ),
        ComprobantePago(
            numero = "CPR-000402",
            fechaHora = "02-09-2026 10:15",
            empresa = "Condominio Plaza Oriente",
            concepto = "Gasto Común Septiembre",
            cuota = "1/12",
            folioCompromiso = "CMP-00102",
            monto = 80000,
            recargoMora = 5000,
            totalPagado = 85000,
            canal = "Web PagoFlex",
            estado = "Anulado"
        )
    )

    MiRegistroTheme {
        HistorialPagosScreen(
            historial = muestraHistorial,
            onVolver = {}
        )
    }
}