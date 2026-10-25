package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import cl.duocuc.miregistro.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.styles.EstilosCampo
import cl.duocuc.miregistro.ui.theme.Dimens

data class ReporteProblema(
    val numeroReporte: String,
    val folioCompromiso: String,
    val rutUsuario: String,
    val motivo: String,
    val comentario: String,
    val estado: String

)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarScreen(
    reporte: ReporteProblema,
    onVolver: () -> Unit,
    onGuardarResolucion: (respuesta: String, estadoCompromiso: String, estadoReporte: String) -> Unit
) {
    var respuestaParaUsuario by rememberSaveable { mutableStateOf("") }
    var estadoFinalCompromiso by rememberSaveable { mutableStateOf("Pendiente") }
    var expandido by remember { mutableStateOf(false) }

    val opcionesEstadoFinal = listOf("Anulado", "Pendiente", "Vencido")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Reporte ${reporte.numeroReporte}")},
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onGuardarResolucion(respuestaParaUsuario, estadoFinalCompromiso, "Resuelto")
                        },
                        enabled = respuestaParaUsuario.isNotBlank()
                    ) {
                        Icon(painterResource(R.drawable.ic_check), "Resolver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(Dimens.espacioPantalla)) {
                    Text(
                        "Folio: ${reporte.folioCompromiso}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Text("RUT: ${reporte.rutUsuario}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Text("Motivo: ${reporte.motivo}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Text(
                        "Comentario: ${reporte.comentario}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.espacioChico))

            CampoTexto(
                valor = respuestaParaUsuario,
                onValorCambia = { respuestaParaUsuario = it },
                etiqueta = "Respuesta para el usuario",
                icono = R.drawable.ic_correo
            )

            Spacer(modifier = Modifier.height(Dimens.espacioChico))

            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = estadoFinalCompromiso,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado final del compromiso") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    shape = EstilosCampo.forma,
                    colors = EstilosCampo.colores(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    opcionesEstadoFinal.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                estadoFinalCompromiso = opcion
                                expandido = false
                            }
                        )
                    }
                }
            }
        }
    }
}