package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Pago confirmado!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Número: ${comprobante.numero}")
                Text("Fecha: ${comprobante.fechaHora}")
                Text("Empresa: ${comprobante.empresa}")
                Text("Concepto: ${comprobante.concepto}")
                Text("Cuota: ${comprobante.cuota}")
                Text("Folio: ${comprobante.folioCompromiso}")
                Text("Monto: ${formatoMoneda.format(comprobante.monto)}")
                Text("Recargo por mora: ${formatoMoneda.format(comprobante.recargoMora)}")

                Text(
                    text = "Total pagado: ${
                        formatoMoneda.format(comprobante.totalPagado)
                    }",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Canal: ${comprobante.canal}")
                Text("Estado: ${comprobante.estado}")
            }
        }

        Spacer(modifier = Modifier.padding(12.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a mis compromisos")
        }
    }
}
