package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.model.Compromiso
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CompromisosScreen(
    compromisos: List<Compromiso>,
    puedePagar: (Compromiso) -> Boolean,
    onVolver: () -> Unit,
    onPagar: (Compromiso) -> Unit
) {
    var compromisoSeleccionado by remember {
        mutableStateOf<Compromiso?>(null)
    }

    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    var filtroEstado by remember {
        mutableStateOf("Todos")
    }

    val cantidadPagados = compromisos.count {
        it.estado.equals("Pagado", ignoreCase = true)
    }

    val cantidadVencidos = compromisos.count {
        it.estado.equals("Vencido", ignoreCase = true)
    }

    val cantidadPendientes = compromisos.count {
        it.estado.equals("Pendiente", ignoreCase = true)
    }

    val compromisosFiltrados = when (filtroEstado) {
        "Pagados" -> compromisos.filter {
            it.estado.equals("Pagado", ignoreCase = true)
        }

        "Vencidos" -> compromisos.filter {
            it.estado.equals("Vencido", ignoreCase = true)
        }

        "Pendientes" -> compromisos.filter {
            it.estado.equals("Pendiente", ignoreCase = true)
        }

        else -> compromisos
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Mis compromisos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta tus cuotas y su estado de pago.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Filtrar compromisos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroEstado == "Todos",
                    onClick = { filtroEstado = "Todos" },
                    label = {
                        Text("Todos (${compromisos.size})")
                    }
                )

                FilterChip(
                    selected = filtroEstado == "Pagados",
                    onClick = { filtroEstado = "Pagados" },
                    label = {
                        Text("Pagados ($cantidadPagados)")
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroEstado == "Vencidos",
                    onClick = { filtroEstado = "Vencidos" },
                    label = {
                        Text("Vencidos ($cantidadVencidos)")
                    }
                )

                FilterChip(
                    selected = filtroEstado == "Pendientes",
                    onClick = { filtroEstado = "Pendientes" },
                    label = {
                        Text("Pendientes ($cantidadPendientes)")
                    }
                )
            }
        }

        if (compromisosFiltrados.isEmpty()) {
            Text(
                text = if (compromisos.isEmpty()) {
                    "No tienes compromisos registrados."
                } else {
                    "No tienes compromisos en este estado."
                },
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = compromisosFiltrados,
                    key = { it.id }
                ) { compromiso ->

                    val pagoPermitido = puedePagar(compromiso)

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
                                text = compromiso.empresa,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = compromiso.concepto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Cuota: ${compromiso.cuota}",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Monto: ${formatearMonto(compromiso.monto)}",
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Vencimiento: ${compromiso.vencimiento}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Folio: ${compromiso.folio}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Estado: ${compromiso.estado}",
                                style = MaterialTheme.typography.titleSmall,
                                color = when (compromiso.estado.lowercase()) {
                                    "vencido" -> MaterialTheme.colorScheme.error
                                    "pagado" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            if (
                                compromiso.estado.equals(
                                    "Pendiente",
                                    ignoreCase = true
                                ) && !pagoPermitido
                            ) {
                                Text(
                                    text = "Pago bloqueado: tienes una cuota vencida en esta empresa.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.padding(top = 4.dp))

                            Button(
                                onClick = {
                                    compromisoSeleccionado = compromiso
                                    mostrarConfirmacion = true
                                },
                                enabled = pagoPermitido,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = when {
                                        compromiso.estado.equals(
                                            "Pagado",
                                            ignoreCase = true
                                        ) -> "Pagado"

                                        !pagoPermitido -> "Pago bloqueado"

                                        compromiso.estado.equals(
                                            "Vencido",
                                            ignoreCase = true
                                        ) -> "Pagar deuda vencida"

                                        else -> "Pagar"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(
                text = "Volver",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (mostrarConfirmacion && compromisoSeleccionado != null) {
        val compromiso = compromisoSeleccionado!!

        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
                compromisoSeleccionado = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                Text("Confirmar pago")
            },
            text = {
                Column {
                    Text("Empresa: ${compromiso.empresa}")
                    Text("Concepto: ${compromiso.concepto}")
                    Text("Monto total: ${formatearMonto(compromiso.monto)}")
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Text("¿Deseas confirmar este pago simulado?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPagar(compromiso)
                        mostrarConfirmacion = false
                        compromisoSeleccionado = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Confirmar pago")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarConfirmacion = false
                        compromisoSeleccionado = null
                    }
                ) {
                    Text(
                        "Cancelar",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

private fun formatearMonto(monto: Int): String {
    val formato = NumberFormat.getCurrencyInstance(
        Locale("es", "CL")
    )
    return formato.format(monto)
}
