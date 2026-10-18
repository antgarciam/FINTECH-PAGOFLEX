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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    usuarios: List<Usuario>,
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
            title = { Text(stringResource(R.string.dialogo_eliminar_titulo)) },
            text = { Text(stringResource(R.string.dialogo_eliminar_mensaje, usuario.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar(usuario)
                    porEliminar = null
                }) { Text(stringResource(R.string.btn_eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_usuarios)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                }
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
                etiqueta = stringResource(R.string.campo_buscar),
                icono = R.drawable.ic_buscar,
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio)
            )

            if (usuarios.isEmpty()) {
                Text(
                    text = stringResource(R.string.lista_vacia),
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
                    Card(onClick = { onEditar(usuario.id) }) {
                        ListItem(
                            overlineContent = { Text(usuario.rolNombre) },
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                            },
                            headlineContent = { Text(usuario.nombre) },
                            supportingContent = { Text("${usuario.correo} · ${usuario.ciudad}") },
                            trailingContent = {
                                IconButton(onClick = { porEliminar = usuario }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_eliminar),
                                        contentDescription = stringResource(R.string.cd_eliminar),
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
