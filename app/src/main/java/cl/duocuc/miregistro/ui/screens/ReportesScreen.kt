package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ReportesScreen(
    onVolver: () -> Unit,
    onEnviarReporte: (motivo: String, detalle: String) -> Unit = { _, _ -> }
) {
    val opcionesMotivo = listOf(
        "Error en el cobro",
        "Pago no reflejado",
        "Falla técnica en la App",
        "Otro"
    )

    var expandido by remember { mutableStateOf(false) }
    var motivoSeleccionado by remember  {mutableStateOf ("Error en el cobro")}
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

            Text(
                text = "Por favor, selecciona el motivo de tu requerimiento. Tu solicitus ingresará directamente con el estado 'En revisión'.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = !expandido }
            ) {
                OutlinedTextField(
                    value = motivoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text( "Motivo de la incidencia" ) },
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
                supportingText = {
                    if(errorDetalle) {
                        Text(
                            text = "Debe explicar el problema al seleccionar 'Otro'",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
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
                        onEnviarReporte(
                            motivoSeleccionado,
                            detalleTexto)
                        scope.launch {
                            snackBarHostState.showSnackbar("Reporte ingresado con éxito (Estado: En revisión)")
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportesScreenPreview() {
    MiRegistroTheme {
        ReportesScreen(
            onVolver = {},
            onEnviarReporte = { _, _ -> }
        )
    }
}