package cl.duocuc.miregistro.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.FormularioUiState
import cl.duocuc.miregistro.model.NivelExperiencia
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.components.BotonPrincipal
import cl.duocuc.miregistro.ui.components.BotonSecundario
import cl.duocuc.miregistro.ui.components.CampoContrasena
import cl.duocuc.miregistro.ui.components.CampoTexto
import cl.duocuc.miregistro.ui.components.CasillaConTexto
import cl.duocuc.miregistro.ui.components.GrupoRadio
import cl.duocuc.miregistro.ui.components.ImagenEncabezado
import cl.duocuc.miregistro.ui.components.InterruptorConTexto
import cl.duocuc.miregistro.ui.components.TarjetaSeccion
import cl.duocuc.miregistro.ui.styles.EstilosCampo
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.Exito
import cl.duocuc.miregistro.ui.theme.ExitoFondo
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme
import cl.duocuc.miregistro.viewmodel.FormularioViewModel
import kotlinx.coroutines.launch

@Composable
fun FormularioScreen(
    onRegistroExitoso: (Usuario) -> Unit,
    usuarioActual: Usuario?,
    onVerMiPerfil: () -> Unit,
    onVerUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var mostrarDialogo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val textoSesionCerrada = stringResource(R.string.snackbar_sesion_cerrada)

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_titulo)) },
            text = { Text(stringResource(R.string.dialogo_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onCerrarSesion()

                    scope.launch { snackbarHostState.showSnackbar(textoSesionCerrada) }
                }) { Text(stringResource(R.string.menu_cerrar_sesion)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = if (usuarioActual != null) stringResource(R.string.saludo, usuarioActual.nombre)
                           else stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                if (usuarioActual != null) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_mi_perfil)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerMiPerfil()
                        }
                    )
                }

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_ver_usuarios)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerUsuarios()
                    }
                )

                if (usuarioActual != null) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarDialogo = true
                        }
                    )
                }
            }
        }
    ) {
        FormularioContent(
            uiState = uiState,
            onNombreCambia = viewModel::onNombreCambia,
            onCorreoCambia = viewModel::onCorreoCambia,
            onContrasenaCambia = viewModel::onContrasenaCambia,
            onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
            onNivelSeleccionado = viewModel::onNivelSeleccionado,
            onNoticiasCambia = viewModel::onNoticiasCambia,
            onTerminosCambia = viewModel::onTerminosCambia,
            onRegistrar = { viewModel.onRegistrar()?.let { usuario -> onRegistroExitoso(usuario) } },
            onLimpiar = viewModel::onLimpiar,
            onCiudadSeleccionada = viewModel::onCiudadSeleccionada,
            usuarioActual = usuarioActual,
            onAbrirMenu = { scope.launch { drawerState.open() } },
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onNivelSeleccionado: (NivelExperiencia) -> Unit,
    onNoticiasCambia: (Boolean) -> Unit,
    onTerminosCambia: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onCiudadSeleccionada: (String) -> Unit = {},
    usuarioActual: Usuario? = null,
    onAbrirMenu: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (usuarioActual != null) stringResource(R.string.saludo, usuarioActual.nombre)
                        else stringResource(R.string.titulo_formulario)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.cd_menu)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = stringResource(R.string.cd_banner),
                titulo = stringResource(R.string.banner_titulo),
                subtitulo = stringResource(R.string.banner_subtitulo)
            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {
                    CampoTexto(
                        valor = uiState.nombre,
                        onValorCambia = onNombreCambia,
                        etiqueta = stringResource(R.string.campo_nombre),
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre
                    )
                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoContrasena(
                        valor = uiState.contrasena,
                        onValorCambia = onContrasenaCambia,
                        etiqueta = stringResource(R.string.campo_contrasena),
                        visible = uiState.contrasenaVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorContrasena
                    )

                    var expandido by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandido,
                        onExpandedChange = { expandido = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.ciudad,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.campo_ciudad)) },
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
                            uiState.ciudades.forEach { ciudad ->
                                DropdownMenuItem(
                                    text = { Text(ciudad) },
                                    onClick = {
                                        onCiudadSeleccionada(ciudad)
                                        expandido = false
                                    }
                                )
                            }
                        }
                    }
                }

                TarjetaSeccion(titulo = stringResource(R.string.seccion_preferencias)) {
                    Text(
                        text = stringResource(R.string.etiqueta_nivel),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    GrupoRadio(
                        opciones = NivelExperiencia.entries,
                        seleccionada = uiState.nivel,
                        onSeleccionar = onNivelSeleccionado,
                        textoDe = { it.etiqueta }
                    )
                    InterruptorConTexto(
                        texto = stringResource(R.string.opcion_noticias),
                        activado = uiState.recibirNoticias,
                        onCambio = onNoticiasCambia
                    )
                }

                Column {
                    CasillaConTexto(
                        texto = stringResource(R.string.opcion_terminos),
                        marcado = uiState.aceptaTerminos,
                        onCambio = onTerminosCambia
                    )

                    uiState.errorTerminos?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_limpiar),
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_registrar),
                        onClick = onRegistrar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (uiState.registroExitoso) {
                    MensajeExito(nombre = uiState.nombre)
                }

                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}

@Composable
private fun MensajeExito(nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(
                text = stringResource(R.string.registro_exitoso, nombre),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FormularioVacioPreview() {
    MiRegistroTheme {
        FormularioContent(
            uiState = FormularioUiState(),
            onNombreCambia = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onAlternarVisibilidad = {}, onNivelSeleccionado = {}, onNoticiasCambia = {},
            onTerminosCambia = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1400, name = "Con errores")
@Composable
private fun FormularioConErroresPreview() {
    MiRegistroTheme {
        FormularioContent(
            uiState = FormularioUiState(
                correo = "ana@",
                errorNombre = "El nombre es obligatorio",
                errorCorreo = "Ingresa un correo válido",
                errorContrasena = "Mínimo 6 caracteres",
                errorTerminos = "Debes aceptar los términos"
            ),
            onNombreCambia = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onAlternarVisibilidad = {}, onNivelSeleccionado = {}, onNoticiasCambia = {},
            onTerminosCambia = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}
