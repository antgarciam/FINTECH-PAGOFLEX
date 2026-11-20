package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.api.CiudadDto
import cl.duocuc.miregistro.api.UsuarioDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    usuario: UsuarioDto?,
    ciudades: List<CiudadDto>,
    onGuardar: (UsuarioDto, String, String, String, Int) -> Unit,
    onDesactivar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    if (usuario == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Usuario no encontrado")
            Button(onClick = onVolver, modifier = Modifier.padding(top = 16.dp)) {
                Text("Volver")
            }
        }
        return
    }

    val rolesDisponibles = listOf(
        1 to "Administrador",
        2 to "Agente de Soporte",
        3 to "Usuario Final",
        4 to "Ejecutivo de Cobranza"
    )

    var nombre by remember { mutableStateOf(usuario.nombre) }
    var correo by remember { mutableStateOf(usuario.correo) }
    var ciudad by remember { mutableStateOf(usuario.ciudad) }
    var ciudadExpandida by remember { mutableStateOf(false) }
    var rolIdSeleccionado by remember { mutableIntStateOf(usuario.rolId) }
    var rolExpandido by remember { mutableStateOf(false) }
    var confirmarDesactivar by remember { mutableStateOf(false) }

    val listaCiudades = if (ciudades.isNotEmpty()) ciudades.map { it.nombre }
    else listOf("Santiago", "Valparaíso", "Concepción", "La Serena", "Antofagasta", "Temuco")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Usuario") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmarDesactivar = true }) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "Desactivar",
                            tint = MaterialTheme.colorScheme.onPrimary
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "RUT: ${usuario.rut}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = ciudadExpandida,
                onExpandedChange = { ciudadExpandida = !ciudadExpandida }
            ) {
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciudad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ciudadExpandida) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = ciudadExpandida,
                    onDismissRequest = { ciudadExpandida = false }
                ) {
                    listaCiudades.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                ciudad = opcion
                                ciudadExpandida = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = rolExpandido,
                onExpandedChange = { rolExpandido = !rolExpandido }
            ) {
                OutlinedTextField(
                    value = rolesDisponibles.find { it.first == rolIdSeleccionado }?.second ?: "Usuario Final",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol asignado en el sistema") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpandido) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = rolExpandido,
                    onDismissRequest = { rolExpandido = false }
                ) {
                    rolesDisponibles.forEach { (idRol, etiqueta) ->
                        DropdownMenuItem(
                            text = { Text(etiqueta) },
                            onClick = {
                                rolIdSeleccionado = idRol
                                rolExpandido = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onGuardar(usuario, nombre, correo, ciudad, rolIdSeleccionado) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { confirmarDesactivar = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Desactivar Cuenta (Sin borrado físico)", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (confirmarDesactivar) {
        AlertDialog(
            onDismissRequest = { confirmarDesactivar = false },
            title = { Text("¿Desactivar usuario?") },
            text = {
                Text("La cuenta quedará inactiva (activo = false), conservando todos sus compromisos y pagos históricos.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmarDesactivar = false
                        onDesactivar(usuario.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Desactivar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmarDesactivar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}