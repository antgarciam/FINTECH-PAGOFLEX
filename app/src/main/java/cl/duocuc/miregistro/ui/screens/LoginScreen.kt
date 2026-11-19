package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginExitoso: (String, String) -> Unit, errorGeneral: String?, errorPassword: String?,
    onNavegarRecuperar: () -> Unit, onNavegarRegistro: () -> Unit
) {
    var rut by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Bienvenido a PagoFlex", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(value = rut, onValueChange = { rut = it }, label = { Text("RUT") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") },
            isError = errorPassword != null, supportingText = { if (errorPassword != null) Text(errorPassword, color = MaterialTheme.colorScheme.error) },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null) } },
            modifier = Modifier.fillMaxWidth()
        )
        if (errorGeneral != null) { Spacer(modifier = Modifier.height(8.dp)); Text(errorGeneral, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onLoginExitoso(rut, pass) }, modifier = Modifier.fillMaxWidth()) { Text("Iniciar Sesión") }

        TextButton(onClick = onNavegarRecuperar) {
            Text("¿Olvidaste tu contraseña?")
        }

        TextButton(onClick = onNavegarRegistro) { Text("Crear cuenta") }
    }
}
