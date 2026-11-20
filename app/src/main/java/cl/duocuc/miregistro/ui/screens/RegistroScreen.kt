package cl.duocuc.miregistro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.api.CiudadDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    ciudades: List<CiudadDto>,
    preguntasSeguridad: List<String>,
    isLoading: Boolean = false,
    onRegistroExitoso: (String, String, String, String, String, String, String, String) -> Unit,
    errorNombre: String?,
    errorRut: String?,
    errorCorreo: String?,
    errorCiudad: String?,
    errorPregunta: String?,
    errorRespuesta: String?,
    errorPassword: String?,
    errorConfirmPassword: String?,
    errorGeneral: String?,
    onVolverLogin: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var ciudad by rememberSaveable { mutableStateOf("") }
    var ciudadExpandida by rememberSaveable { mutableStateOf(false) }

    var pregunta by rememberSaveable { mutableStateOf(preguntasSeguridad.firstOrNull() ?: "") }
    var preguntaExpandida by rememberSaveable { mutableStateOf(false) }
    var respuesta by rememberSaveable { mutableStateOf("") }

    var pass by rememberSaveable { mutableStateOf("") }
    var confirmPass by rememberSaveable { mutableStateOf("") }
    var passVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    val listaCiudades = if (ciudades.isNotEmpty()) {
        ciudades.map { it.nombre }
    } else {
        listOf("Santiago", "Valparaíso", "Concepción", "La Serena", "Antofagasta", "Temuco")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Crear cuenta PagoFlex",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Completa tus datos personales y configura tu clave segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = errorNombre != null,
            supportingText = { if (errorNombre != null) Text(errorNombre, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rut,
            onValueChange = { rut = it },
            label = { Text("RUT (ej: 12345678-9)") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            isError = errorRut != null,
            supportingText = { if (errorRut != null) Text(errorRut, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            isError = errorCorreo != null,
            supportingText = { if (errorCorreo != null) Text(errorCorreo, color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = ciudadExpandida,
            onExpandedChange = { ciudadExpandida = !ciudadExpandida }
        ) {
            OutlinedTextField(
                value = ciudad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Ciudad (desde Base de Datos)") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ciudadExpandida) },
                isError = errorCiudad != null,
                supportingText = { if (errorCiudad != null) Text(errorCiudad, color = MaterialTheme.colorScheme.error) },
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
            expanded = preguntaExpandida,
            onExpandedChange = { preguntaExpandida = !preguntaExpandida }
        ) {
            OutlinedTextField(
                value = pregunta,
                onValueChange = {},
                readOnly = true,
                label = { Text("Pregunta de seguridad") },
                leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = preguntaExpandida) },
                isError = errorPregunta != null,
                supportingText = { if (errorPregunta != null) Text(errorPregunta, color = MaterialTheme.colorScheme.error) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = preguntaExpandida,
                onDismissRequest = { preguntaExpandida = false }
            ) {
                preguntasSeguridad.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            pregunta = opcion
                            preguntaExpandida = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = respuesta,
            onValueChange = { respuesta = it },
            label = { Text("Respuesta de seguridad") },
            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
            isError = errorRespuesta != null,
            supportingText = { if (errorRespuesta != null) Text(errorRespuesta, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña (mín. 8, Mayús, Minús, Núm, Símbolo)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            isError = errorPassword != null,
            supportingText = { if (errorPassword != null) Text(errorPassword, color = MaterialTheme.colorScheme.error) },
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        imageVector = if (passVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Confirmar contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            isError = errorConfirmPassword != null,
            supportingText = {
                if (errorConfirmPassword != null) {
                    Text(errorConfirmPassword, color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        imageVector = if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (confirmVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = errorGeneral != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorGeneral ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                onRegistroExitoso(nombre, rut, correo, ciudad, pregunta, respuesta, pass, confirmPass)
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear cuenta", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(
            onClick = onVolverLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio de sesión")
        }
    }
}