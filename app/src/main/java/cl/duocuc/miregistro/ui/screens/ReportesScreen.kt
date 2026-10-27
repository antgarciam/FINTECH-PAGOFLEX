package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Reporte
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel
import kotlinx.coroutines.launch

@Composable
fun ReportesScreen(
    onVolver: () -> Unit,
    onEnviarReporte: (motivo: String, detalle: String) -> Unit = { _, _ -> },
    onAtenderReporte: (Reporte) -> Unit = {},
    viewModel: UsuariosViewModel = viewModel()
) {
    val usuarioActual by viewModel.usuarioActual.collectAsStateWithLifecycle()

    if (usuarioActual?.rolId == 2L) {
        ReportesAgenteView(viewModel = viewModel, onVolver = onVolver, onAtenderReporte = onAtenderReporte)
    } else {
        ReportesUsuarioView(onVolver = onVolver, onEnviarReporte = onEnviarReporte)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportesUsuarioView(
    onVolver: () -> Unit,
    onEnviarReporte: (motivo: String, detalle: String) -> Unit
) {
    val opcionesMotivo = listOf(
        "Error en el cobro",
        "Pago no reflejado",
        "Falla técnica en la App",
        "Otro"
    )

    var expandido by remember { mutableStateOf(false) }
    var motivoSeleccionado by remember { mutableStateOf("Error en el cobro") }
    var detalleTexto by remember { mutableStateOf("") }
    var errorDetalle by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reporte de Incidencias") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Generar constancia de error / reclamo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = !expandido }
            ) {
                OutlinedTextField(
                    value = motivoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Motivo de la incidencia") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    opcionesMotivo.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                motivoSeleccionado = opcion
                                expandido = false
                                if (opcion != "Otro") errorDetalle = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = detalleTexto,
                onValueChange = {
                    detalleTexto = it
                    if (it.isNotBlank()) errorDetalle = false
                },
                label = {
                    Text(
                        if (motivoSeleccionado == "Otro") "Detalle del problema (Obligatorio)"
                        else "Detalle opcional o comentarios"
                    )
                },
                isError = errorDetalle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (motivoSeleccionado == "Otro" && detalleTexto.trim().isEmpty()) {
                        errorDetalle = true
                    } else {
                        errorDetalle = false
                        onEnviarReporte(motivoSeleccionado, detalleTexto)
                        scope.launch {
                            snackBarHostState.showSnackbar("Reporte ingresado con éxito (Estado: Abierto)")
                        }
                        detalleTexto = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar reporte de incidencia")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportesAgenteView(
    viewModel: UsuariosViewModel,
    onVolver: () -> Unit,
    onAtenderReporte: (Reporte) -> Unit
) {
    val reportes by viewModel.reportes.collectAsStateWithLifecycle()
    var filtroEstado by remember { mutableStateOf("Todos") }

    LaunchedEffect(Unit) {
        viewModel.cargarReportes()
    }

    // Filtrado en tiempo real
    val reportesFiltrados = reportes.filter { reporte ->
        if (filtroEstado == "Todos") true
        else reporte.estado.equals(filtroEstado, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Soporte / Agente") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Filtros rápidos por estado
            Text("Filtrar por estado:", style = MaterialTheme.typography.labelMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                listOf("Todos", "En revisión", "En gestión", "Resuelto").forEach { estado ->
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick = { filtroEstado = estado },
                        label = { Text(estado) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reportesFiltrados.isEmpty()) {
                Text(
                    text = "No hay reportes en la categoría seleccionada.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        // Alerta visual de color rojo si el reporte está en estado 'En revisión' / 'Abierto'
                        val esUrgente = reporte.estado.contains("revisión", ignoreCase = true) || reporte.estado.contains("Abierto", ignoreCase = true)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esUrgente) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (esUrgente) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (esUrgente) {
                                    Text(
                                        text = "⚠️ ATENCIÓN: Pendiente de atención (> 2 días o nuevo)",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Text(
                                    text = "Cliente: ${reporte.clienteNombre} (${reporte.clienteCorreo})",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Motivo: ${reporte.motivo}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = "Detalle: ${reporte.detalle}")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Estado: ${reporte.estado} | Fecha: ${reporte.fechaHora}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Botón para pasar a 'En gestión' o Atender
                                Button(
                                    onClick = { onAtenderReporte(reporte) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Gestionar / Responder ID Agente")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}