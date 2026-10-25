
package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.ComprobantePago
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ComprobantePagoScreen(
    comprobante: ComprobantePago,
    onVolver: () -> Unit
) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(
        Locale("es", "CL")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Pago confirmado!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.padding(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Comprobante de pago",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Número: ${comprobante.numero}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Fecha: ${comprobante.fechaHora}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Empresa: ${comprobante.empresa}",
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
                    text = "Folio: ${comprobante.folioCompromiso}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Monto: ${formatoMoneda.format(comprobante.monto)}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Recargo por mora: ${
                        formatoMoneda.format(comprobante.recargoMora)
                    }",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Total pagado: ${
                        formatoMoneda.format(comprobante.totalPagado)
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Canal: ${comprobante.canal}",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Estado: ${comprobante.estado}",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Volver a mis compromisos")
        }
    }
}
