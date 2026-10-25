package cl.duocuc.miregistro.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import cl.duocuc.miregistro.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun UsuariosScreen(
    usuarios : List<Usuario>,
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit = {},
    filtro: String = "",
    onBuscar: (String) -> Unit = {},
    onEliminar: (Usuario) -> Unit = {}
) {
    var porEliminar by remember { mutableStateOf<Usuario?>(null) }

    porEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Anular Reporte Técnico") },
            text = { Text("Está seguro de que desea rechazar y archivar el caso de soporte de ${usuario.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar(usuario)
                    porEliminar = null

                }) { Text("Confirmar Anulación") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandeja de Reportes") },
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
        ) {
            CampoTexto(
                valor = filtro,
                onValorCambia = onBuscar,
                etiqueta = "Buscar reporte por cliente o ID",
                icono = R.drawable.ic_buscar,
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
            )

            if (usuarios.isEmpty()) {
                Text(
                    text = "No se registran casos o incidencias abiertas.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(usuarios, key = { it.id }) { usuario ->
                    Card(onClick = { onEditar(usuario.id)}) {
                        ListItem(
                            overlineContent = {
                                Text(
                                    text = "Estado: En revisión",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                            },
                            headlineContent = {Text("Caso de: ${usuario.nombre}")},
                            supportingContent = {
                                Text("Contacto: ${usuario.correo} - Sucursal: ${usuario.ciudad}")
                            },
                            trailingContent = {
                                IconButton(onClick = { porEliminar = usuario }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_eliminar),
                                        contentDescription = "Anular ticket",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UsuariosScreenPreview() {
    cl.duocuc.miregistro.ui.theme.MiRegistroTheme {
        UsuariosScreen(
            usuarios = emptyList(),
            onVolver = {},
            onEditar = {},
            filtro = "",
            onBuscar = {},
            onEliminar = {}
        )
    }
}
