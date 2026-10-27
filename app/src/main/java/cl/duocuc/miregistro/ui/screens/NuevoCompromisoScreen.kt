package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoCompromisoScreen(
    onVolver: () -> Unit,
    onValidarRut: (String) -> Boolean,
    onCrearCompromiso: (rut: String, concepto: String, monto: Int, vencimiento: String) -> Boolean
) {
    var rut by remember { mutableStateOf("") }
    var concepto by remember { mutableStateOf("") }
    var montoTexto by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }

    var errorRut by remember { mutableStateOf(false) }
    var errorFecha by remember { mutableStateOf(false) }
    var errorCampos by remember { mutableStateOf(false) }

    val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nuevo Compromiso") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Formulario de Obligaciones Financieras",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Campo RUT con validador DV
            OutlinedTextField(
                value = rut,
                onValueChange = {
                    rut = it
                    errorRut = false
                },
                label = { Text("RUT Cliente (Obligatorio)") },
                isError = errorRut,
                supportingText = { if (errorRut) Text("RUT inválido. Verifique el dígito verificador.") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Concepto
            OutlinedTextField(
                value = concepto,
                onValueChange = { concepto = it },
                label = { Text("Concepto de la deuda") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Monto
            OutlinedTextField(
                value = montoTexto,
                onValueChange = { montoTexto = it.filter { c -> c.isDigit() } },
                label = { Text("Monto ($)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Fecha (dd-MM-yyyy)
            OutlinedTextField(
                value = fechaVencimiento,
                onValueChange = {
                    fechaVencimiento = it
                    errorFecha = false
                },
                label = { Text("Fecha Vencimiento (ej. 30-10-2026)") },
                isError = errorFecha,
                supportingText = { if (errorFecha) Text("La fecha no puede ser anterior a la fecha actual del sistema.") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorCampos) {
                Text(
                    text = "Por favor complete todos los campos obligatorios adecuadamente.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val monto = montoTexto.toIntOrNull() ?: 0
                    val esRutValido = onValidarRut(rut)

                    var esFechaValida = true
                    try {
                        val fechaIngresada = formatoFecha.parse(fechaVencimiento)
                        val hoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(formatoFecha.format(Date()))
                        if (fechaIngresada != null && hoy != null && fechaIngresada.before(hoy)) {
                            esFechaValida = false
                        }
                    } catch (e: Exception) {
                        esFechaValida = false
                    }

                    if (!esRutValido) errorRut = true
                    if (!esFechaValida) errorFecha = true

                    if (esRutValido && esFechaValida && concepto.isNotBlank() && monto > 0) {
                        val creado = onCrearCompromiso(rut, concepto, monto, fechaVencimiento)
                        if (creado) onVolver()
                    } else {
                        errorCampos = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear y Registrar Obligación")
            }
        }
    }
}