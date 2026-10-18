package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.styles.EstilosCampo
import cl.duocuc.miregistro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarScreen(
    usuario: Usuario,
    roles: List<Rol>,
    onVolver: () -> Unit,
    onGuardar: (Usuario) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf(usuario.nombre) }
    var correo by rememberSaveable { mutableStateOf(usuario.correo) }
    var ciudad by rememberSaveable { mutableStateOf(usuario.ciudad) }
    var rolId by rememberSaveable { mutableStateOf(usuario.rolId) }
    var expandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_editar)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), stringResource(R.string.cd_volver))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onGuardar(usuario.copy(nombre = nombre, correo = correo, ciudad = ciudad, rolId = rolId))
                        },
                        enabled = nombre.isNotBlank()
                    ) {
                        Icon(painterResource(R.drawable.ic_check), stringResource(R.string.cd_guardar))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            CampoTexto(
                valor = nombre,
                onValorCambia = { nombre = it },
                etiqueta = stringResource(R.string.campo_nombre),
                icono = R.drawable.ic_persona
            )
            CampoTexto(
                valor = correo,
                onValorCambia = { correo = it },
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = ciudad,
                onValorCambia = { ciudad = it },
                etiqueta = stringResource(R.string.campo_ciudad),
                icono = R.drawable.ic_ubicacion
            )

            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = roles.firstOrNull { it.id == rolId }?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.campo_rol)) },
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
                    roles.forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol.nombre) },
                            onClick = {
                                rolId = rol.id
                                expandido = false
                            }
                        )
                    }
                }
            }
        }
    }
}
