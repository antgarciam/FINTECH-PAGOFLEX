package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegistroScreen(
    onRegistroExitoso: (String, String, String, String, String, String) -> Unit,
    errorNombre: String?, errorRut: String?, errorCorreo: String?, errorPassword: String?, errorGeneral: String?,
    onVolverLogin: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var ciudad by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirmPass by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, isError = errorNombre != null, supportingText = { if (errorNombre != null) Text(errorNombre, color = MaterialTheme.colorScheme.error) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = rut, onValueChange = { rut = it }, label = { Text("RUT") }, isError = errorRut != null, supportingText = { if (errorRut != null) Text(errorRut, color = MaterialTheme.colorScheme.error) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") }, isError = errorCorreo != null, supportingText = { if (errorCorreo != null) Text(errorCorreo, color = MaterialTheme.colorScheme.error) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = ciudad, onValueChange = { ciudad = it }, label = { Text("Ciudad") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, isError = errorPassword != null, supportingText = { if (errorPassword != null) Text(errorPassword, color = MaterialTheme.colorScheme.error) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = confirmPass, onValueChange = { confirmPass = it }, label = { Text("Confirmar Contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        if (errorGeneral != null) { Spacer(modifier = Modifier.height(8.dp)); Text(errorGeneral, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onRegistroExitoso(nombre, rut, correo, ciudad, pass, confirmPass) }, modifier = Modifier.fillMaxWidth()) { Text("Crear cuenta") }
        TextButton(onClick = onVolverLogin) { Text("Volver al login") }
    }
}

