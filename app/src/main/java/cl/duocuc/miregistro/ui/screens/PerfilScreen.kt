package cl.duocuc.miregistro.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.api.CiudadDto
import cl.duocuc.miregistro.data.ConvertidorImagen
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.permisos.Permisos
import cl.duocuc.miregistro.ui.components.AvatarCircular
import cl.duocuc.miregistro.ui.components.TarjetaSeccion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    ciudades: List<CiudadDto>,
    errorNombre: String?,
    errorCorreo: String?,
    errorCiudad: String?,
    errorPassword: String?,
    errorConfirmPassword: String?,
    errorGeneral: String?,
    mensajeExito: String?,
    onGuardarPerfil: (nombre: String, correo: String, ciudad: String) -> Unit,
    onCambiarClave: (actual: String, nueva: String, confirmar: String) -> Unit,
    onFotoTomada: (Bitmap) -> Unit,
    onImagenElegida: (Uri) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoGaleriaDenegada = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    var nombre by rememberSaveable(usuario?.nombre) { mutableStateOf(usuario?.nombre ?: "") }
    var correo by rememberSaveable(usuario?.correo) { mutableStateOf(usuario?.correo ?: "") }
    var ciudad by rememberSaveable(usuario?.ciudad) { mutableStateOf(usuario?.ciudad ?: "Santiago") }
    var ciudadExpandida by rememberSaveable { mutableStateOf(false) }

    var claveActual by rememberSaveable { mutableStateOf("") }
    var claveNueva by rememberSaveable { mutableStateOf("") }
    var confirmarNueva by rememberSaveable { mutableStateOf("") }
    var verActual by rememberSaveable { mutableStateOf(false) }
    var verNueva by rememberSaveable { mutableStateOf(false) }
    var verConfirmar by rememberSaveable { mutableStateOf(false) }

    val listaCiudades = if (ciudades.isNotEmpty()) ciudades.map { it.nombre }
    else listOf("Santiago", "Valparaíso", "Concepción", "La Serena", "Antofagasta", "Temuco")

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = textoAjustes,
                duration = SnackbarDuration.Long
            )
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) onFotoTomada(foto)
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            try { lanzadorCamara.launch(null) } catch (_: ActivityNotFoundException) {}
        } else avisarPermisoDenegado(textoCamaraDenegada)
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) onImagenElegida(uri)
    }

    val pedirPermisoImagenes = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            try { lanzadorGaleria.launch(intent) } catch (_: ActivityNotFoundException) {}
        } else avisarPermisoDenegado(textoGaleriaDenegada)
    }

    val foto = remember(usuario?.avatar) {
        usuario?.avatar?.let { bytes -> ConvertidorImagen.aBitmap(bytes)?.asImageBitmap() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil y Seguridad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(id = R.drawable.ic_volver), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarCircular(
                imagen = R.drawable.avatar_camila_rojas,
                descripcion = "Foto de perfil",
                foto = foto,
                modifier = Modifier.size(104.dp)
            )
            Text(
                text = "RUT: ${usuario?.rut ?: ""} · ${usuario?.rolNombre ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    if (Permisos.tiene(context, Permisos.CAMARA)) {
                        try { lanzadorCamara.launch(null) } catch (_: ActivityNotFoundException) {}
                    } else pedirPermisoCamara.launch(Permisos.CAMARA)
                }) { Text("Tomar foto") }

                TextButton(onClick = {
                    if (Permisos.tiene(context, Permisos.IMAGENES)) {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        try { lanzadorGaleria.launch(intent) } catch (_: ActivityNotFoundException) {}
                    } else pedirPermisoImagenes.launch(Permisos.IMAGENES)
                }) { Text("Galería") }
            }

            AnimatedVisibility(visible = mensajeExito != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensajeExito ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            AnimatedVisibility(visible = errorGeneral != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorGeneral ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            TarjetaSeccion(titulo = "Modificar datos personales") {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    isError = errorNombre != null,
                    supportingText = { if (errorNombre != null) Text(errorNombre) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    isError = errorCorreo != null,
                    supportingText = { if (errorCorreo != null) Text(errorCorreo) },
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
                        label = { Text("Ciudad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ciudadExpandida) },
                        isError = errorCiudad != null,
                        supportingText = { if (errorCiudad != null) Text(errorCiudad) },
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

                Button(
                    onClick = { onGuardarPerfil(nombre, correo, ciudad) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar cambios de perfil")
                }
            }

            TarjetaSeccion(titulo = "Cambiar contraseña") {
                OutlinedTextField(
                    value = claveActual,
                    onValueChange = { claveActual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { verActual = !verActual }) {
                            Icon(
                                imageVector = if (verActual) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = claveNueva,
                    onValueChange = { claveNueva = it },
                    label = { Text("Nueva contraseña") },
                    isError = errorPassword != null,
                    supportingText = { if (errorPassword != null) Text(errorPassword) },
                    visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { verNueva = !verNueva }) {
                            Icon(
                                imageVector = if (verNueva) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmarNueva,
                    onValueChange = { confirmarNueva = it },
                    label = { Text("Repetir nueva contraseña") },
                    isError = errorConfirmPassword != null,
                    supportingText = { if (errorConfirmPassword != null) Text(errorConfirmPassword) },
                    visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { verConfirmar = !verConfirmar }) {
                            Icon(
                                imageVector = if (verConfirmar) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { onCambiarClave(claveActual, claveNueva, confirmarNueva) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar contraseña")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}