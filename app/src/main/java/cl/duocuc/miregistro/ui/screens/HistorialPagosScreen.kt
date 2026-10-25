
package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.ComprobantePago
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistorialPagosScreen(
    historial: List<ComprobantePago>,
    onVolver: () -> Unit
) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(
        Locale("es", "CL")
    )

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
            text = "Consulta los pagos simulados que has realizado.",
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (historial.isEmpty()) {
            Text(
                text = "Todavía no tienes pagos registrados.",
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = historial,
                    key = { it.numero }
                ) { comprobante ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = comprobante.empresa,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Concepto: ${comprobante.concepto}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Cuota: ${comprobante.cuota}",
                                color = MaterialTheme.colorScheme.onSurface
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
                                text = "Total pagado: ${
                                    formatoMoneda.format(comprobante.totalPagado)
                                }",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Comprobante: ${comprobante.numero}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Estado: ${comprobante.estado}",
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
