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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState()},
    onVerMiPerfil: () -> Unit = {}
) {
    var mostrarDetallePago by remember { mutableStateOf(false) }
    var cuentaPagadaExitosa by remember { mutableStateOf(false) }

    if (mostrarDetallePago) {
        AlertDialog(
            onDismissRequest = { mostrarDetallePago = false },
            title = { Text("Detalle de Cuenta pendiente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Empresa: Club Deportivo Los Halcones", fontWeight = FontWeight.Bold)
                    Text("Concepto: Cuota social octubre (10/12)")
                    Text("Monto original: $15.000")
                    Text("Recargo por atraso: $2.000 (RN-03)", color = MaterialTheme.colorScheme.error)
                    HorizontalDivider()
                    Text("Total a pagar: $17.000", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDetallePago = false
                    cuentaPagadaExitosa = true // Simula la recaudación exitosa cambiando el estado local
                }) { Text("Pagar cuenta ($17.000)") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDetallePago = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tus pagos pendientes") }, // RNF-04: Tono Positivo y empático
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = "Menú")
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
                descripcion = "Banner financiero PagoFlex",
                titulo = "$ 535.000", // RF-02: Total consolidado del mes de Camila
                subtitulo = "Próximo vencimiento: 05-10-2026"
            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_persona),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Tienes 1 compromiso de pago vencido.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }

                TarjetaSeccion(titulo = "Cuentas del Mes") {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {

                        if (!cuentaPagadaExitosa) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Club Los Halcones", fontWeight = FontWeight.Bold)
                                        Text("$17.000", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Cuota 10/12 · Vence: 05-10-2026 (Vencida)", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BotonPrincipal(
                                        texto = "Ver detalle para pagar",
                                        onClick = { mostrarDetallePago = true },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Plaza Oriente", fontWeight = FontWeight.Bold)
                                    Text("$520.000", fontWeight = FontWeight.Bold)
                                }
                                Text("Arriendo octubre · Vence: 10-10-2026", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (cuentaPagadaExitosa) {
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
                                tint = Exito
                            )
                            Text(
                                text = "¡Pago exitoso! Comprobante generado: CPR-000402",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = Dimens.espacioChico)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.espacioGrande))

                BotonSecundario(
                    texto = "Ir a mi perfil PagoFlex",
                    onClick = onVerMiPerfil,
                    modifier = Modifier.fillMaxWidth()
                )
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
