package cl.duocuc.miregistro.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.data.ConvertidorImagen
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.Perfil
import cl.duocuc.miregistro.model.PerfilUiState
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.permisos.Permisos
import cl.duocuc.miregistro.ui.components.AvatarCircular
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.EtiquetaInteres
import cl.duocuc.miregistro.ui.components.TarjetaEstadistica
import cl.duocuc.miregistro.ui.components.TarjetaSeccion
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme
import cl.duocuc.miregistro.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    usuario: Usuario?,
    compromisos: List<Compromiso> = emptyList(),
    onVolver: () -> Unit,
    onVerCompromisos: () -> Unit = {},
    onFotoTomada: (Bitmap) -> Unit = {},
    onImagenElegida: (Uri) -> Unit = {},
    viewModel: PerfilViewModel = viewModel(),
    onVerHistorialPagos: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoGaleriaDenegada = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

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

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {}
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirCamara() else avisarPermisoDenegado(textoCamaraDenegada)
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) onImagenElegida(uri)
    }

    fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            lanzadorGaleria.launch(intent)
        } catch (e: ActivityNotFoundException) {}
    }

    val pedirPermisoImagenes = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirGaleria() else avisarPermisoDenegado(textoGaleriaDenegada)
    }

    val foto = remember(usuario?.avatar) {
        usuario?.avatar?.let { bytes -> ConvertidorImagen.aBitmap(bytes)?.asImageBitmap() }
    }

    val perfil = if (usuario == null) uiState.perfil else uiState.perfil.copy(
        nombre = usuario.nombre,
        profesion = "${usuario.rolNombre} · ${usuario.correo}",
        ciudad = usuario.ciudad
    )

    PerfilContent(
        uiState = uiState.copy(perfil = perfil),
        usuario = usuario,
        compromisos = compromisos,
        onVolver = onVolver,
        onVerCompromisos = onVerCompromisos,
        onVerHistorialPagos = onVerHistorialPagos,
        foto = foto,
        puedeCambiarFoto = usuario != null,
        snackbarHostState = snackbarHostState,
        onTomarFoto = {
            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
            else pedirPermisoCamara.launch(Permisos.CAMARA)
        },
        onAbrirGaleria = {
            if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
            else pedirPermisoImagenes.launch(Permisos.IMAGENES)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilContent(
    uiState: PerfilUiState,
    usuario: Usuario?, // CORREGIDO: Reemplazado '=' por ':'
    compromisos: List<Compromiso> = emptyList(), // CORREGIDO: Reemplazado '=' por ':'
    onVolver: () -> Unit = {},
    onVerCompromisos: () -> Unit = {},
    foto: ImageBitmap? = null,
    puedeCambiarFoto: Boolean = false,
    onTomarFoto: () -> Unit = {},
    onAbrirGaleria: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onVerHistorialPagos: () -> Unit = {}
) {
    val perfil = uiState.perfil

    val nombreMostrar = usuario?.nombre ?: perfil?.nombre ?: "Camila Rojas"
    val correoMostrar = usuario?.correo ?: perfil?.descripcion ?: "c.rojas@duocuc.cl"

    val rutOculto = remember(usuario?.correo) {
        val rutReal = usuario?.correo ?: "15.834.207-3"
        val partes = rutReal.split(".")
        if (partes.size >= 3) {
            val digitoVerificador = rutReal.substringAfter("-")
            "RUT: ${partes[0]}.*.*-$digitoVerificador"
        } else {
            "RUT: 15.*.*-3"
        }
    }

    // RN-10 y RN-11: Lógica matemática estricta para Indicador y Categoría Fintech
    val (indicadorTexto, categoriaTexto, categoriaColor) = remember(compromisos) {
        val validos = compromisos.filter {
            !it.estado.equals("Anulado", ignoreCase = true) &&
                    !it.estado.equals("En revisión", ignoreCase = true) &&
                    !it.estado.equals("Pendiente", ignoreCase = true)
        }

        if (validos.size < 3) {
            Triple("Historial insuficiente", "En camino", androidx.compose.ui.graphics.Color(0xFFE65100))
        } else {
            val pagadosATiempo = validos.count { it.estado.equals("Pagado", ignoreCase = true) }
            val porcentaje = (pagadosATiempo.toFloat() / validos.size) * 100
            val porcentajeFormateado = String.format(java.util.Locale.US, "%.1f%%", porcentaje)

            val (cat, color) = when {
                porcentaje >= 95.0f -> "Ejemplar" to androidx.compose.ui.graphics.Color(0xFF2E7D32)
                porcentaje >= 80.0f -> "Al día" to androidx.compose.ui.graphics.Color(0xFF1565C0)
                else -> "En camino" to androidx.compose.ui.graphics.Color(0xFFE65100)
            }
            Triple(porcentajeFormateado, cat, color)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi perfil PagoFlex") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
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
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            AvatarCircular(
                imagen = R.drawable.avatar_camila_rojas,
                descripcion = "Foto de perfil de usuario",
                foto = foto,
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = nombreMostrar,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = rutOculto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                TextButton(onClick = onTomarFoto) {
                    Text("Tomar Foto")
                }
                TextButton(onClick = onAbrirGaleria) {
                    Text("Abrir galería")
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Text(
                        text = "Indicador de cumplimiento",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = indicadorTexto,
                        style = if (indicadorTexto.contains("%")) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = categoriaColor
                    )
                    Text(
                        text = "Clasificación interna: $categoriaTexto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            TarjetaSeccion(titulo = "Información del cliente") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Text(text = "Correo electrónico: $correoMostrar", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Rol asignado: R-01 (Usuario Final)", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(Dimens.espacioGrande))
            OutlinedButton(
                onClick = onVerCompromisos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mis compromisos")
            }
            OutlinedButton(
                onClick = onVerHistorialPagos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Historial de pagos")
            }
            BotonPrincipal(
                texto = "Volver a mis cuentas",
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
private fun BotonConIcono(icono: Int, texto: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
        Spacer(Modifier.width(Dimens.espacioChico))
        Text(texto)
    }
}
@Composable
private fun EncabezadoPerfil(foto: ImageBitmap? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.altoPortada + Dimens.tamanoAvatar / 2)
    ) {
        Image(
            painter = painterResource(R.drawable.portada_perfil),
            contentDescription = stringResource(R.string.cd_portada),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.altoPortada)
                .align(Alignment.TopCenter)
        )
        AvatarCircular(
            imagen = R.drawable.avatar_usuario,
            descripcion = stringResource(R.string.cd_avatar),
            modifier = Modifier.align(Alignment.BottomCenter),
            foto = foto
        )
    }
}
private val perfilPreview = Perfil(
    nombre = "Camila Rojas",
    profesion = "Desarrolladora móvil",
    ciudad = "Santiago, Chile",
    descripcion = "Texto de ejemplo para la vista previa.",
    seguidores = 128,
    siguiendo = 96,
    publicaciones = 24,
    intereses = listOf("Kotlin", "Compose", "Diseño UI")
)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PerfilPreview() {
    MiRegistroTheme {
        PerfilContent(uiState = PerfilUiState(perfil = perfilPreview), usuario = null)
    }
}
@Preview(showBackground = true, showSystemUi = true, name = "Modo oscuro")
@Composable
private fun PerfilOscuroPreview() {
    MiRegistroTheme(darkTheme = true) {
        PerfilContent(uiState = PerfilUiState(perfil = perfilPreview, loSigo = true), usuario = null)
    }
}