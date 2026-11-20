package cl.duocuc.miregistro.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duocuc.miregistro.api.UsuarioDto
import cl.duocuc.miregistro.model.Reporte
import cl.duocuc.miregistro.ui.screens.ComprobantePagoScreen
import cl.duocuc.miregistro.ui.screens.GestionarReporteScreen
import cl.duocuc.miregistro.ui.screens.HistorialPagosScreen
import cl.duocuc.miregistro.ui.screens.InicioUsuarioScreen
import cl.duocuc.miregistro.ui.screens.LoginScreen
import cl.duocuc.miregistro.ui.screens.NuevoCompromisoScreen
import cl.duocuc.miregistro.ui.screens.PanelCobranzaScreen
import cl.duocuc.miregistro.ui.screens.PerfilScreen
import cl.duocuc.miregistro.ui.screens.RecuperarContrasenaScreen
import cl.duocuc.miregistro.ui.screens.RegistroScreen
import cl.duocuc.miregistro.ui.screens.ReportesScreen
import cl.duocuc.miregistro.ui.screens.UsuariosScreen
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val viewModel: UsuariosViewModel = viewModel()

    var usuarioEditar by remember { mutableStateOf<UsuarioDto?>(null) }
    var reporteGestionar by remember { mutableStateOf<Reporte?>(null) }

    val rolInicial = viewModel.obtenerRolActual()
    val destinoInicial = when (rolInicial) {
        1 -> "home_admin"
        2 -> "home_agente"
        4 -> "home_ejecutivo"
        3 -> "home_usuario"
        else -> "login"
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() }
    ) {
        composable("login") {
            val isLoading by viewModel.isLoading.collectAsState()
            val errRut by viewModel.errorRut.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val errGen by viewModel.errorGeneral.collectAsState()
            val msgExito by viewModel.mensajeExito.collectAsState()
            val exito by viewModel.loginExitoso.collectAsState()

            LaunchedEffect(exito) {
                if (exito) {
                    val destino = when (viewModel.obtenerRolActual()) {
                        1 -> "home_admin"
                        2 -> "home_agente"
                        4 -> "home_ejecutivo"
                        else -> "home_usuario"
                    }
                    navController.navigate(destino) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginScreen(
                onLoginExitoso = { r, p -> viewModel.ejecutarLogin(r, p) },
                isLoading = isLoading,
                errorRut = errRut,
                errorPassword = errPass,
                errorGeneral = errGen,
                mensajeExito = msgExito,
                onNavegarRecuperar = {
                    viewModel.resetFlagsAuth()
                    navController.navigate("recuperar")
                },
                onNavegarRegistro = {
                    viewModel.resetFlagsAuth()
                    navController.navigate("registro")
                }
            )
        }

        composable("registro") {
            val ciudades by viewModel.ciudades.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errNom by viewModel.errorNombre.collectAsState()
            val errRut by viewModel.errorRut.collectAsState()
            val errCor by viewModel.errorCorreo.collectAsState()
            val errCiu by viewModel.errorCiudad.collectAsState()
            val errPreg by viewModel.errorPregunta.collectAsState()
            val errResp by viewModel.errorRespuesta.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val errConf by viewModel.errorConfirmPassword.collectAsState()
            val errGen by viewModel.errorGeneral.collectAsState()
            val exito by viewModel.registroExitoso.collectAsState()

            LaunchedEffect(exito) {
                if (exito) {
                    navController.navigate("login") {
                        popUpTo("registro") { inclusive = true }
                    }
                }
            }

            RegistroScreen(
                ciudades = ciudades,
                preguntasSeguridad = viewModel.preguntasSeguridad,
                isLoading = isLoading,
                onRegistroExitoso = { n, r, c, ciu, preg, resp, p, cp ->
                    viewModel.ejecutarRegistro(n, r, c, ciu, preg, resp, p, cp)
                },
                errorNombre = errNom,
                errorRut = errRut,
                errorCorreo = errCor,
                errorCiudad = errCiu,
                errorPregunta = errPreg,
                errorRespuesta = errResp,
                errorPassword = errPass,
                errorConfirmPassword = errConf,
                errorGeneral = errGen,
                onVolverLogin = {
                    viewModel.resetFlagsAuth()
                    navController.popBackStack()
                }
            )
        }

        composable("recuperar") {
            val isLoading by viewModel.isLoading.collectAsState()
            val errRut by viewModel.errorRut.collectAsState()
            val errResp by viewModel.errorRespuesta.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val errConf by viewModel.errorConfirmPassword.collectAsState()
            val errGen by viewModel.errorGeneral.collectAsState()
            val exito by viewModel.recuperacionExitosa.collectAsState()

            LaunchedEffect(exito) {
                if (exito) {
                    navController.navigate("login") {
                        popUpTo("recuperar") { inclusive = true }
                    }
                }
            }

            RecuperarContrasenaScreen(
                isLoading = isLoading,
                errorRut = errRut,
                errorRespuesta = errResp,
                errorPassword = errPass,
                errorConfirmPassword = errConf,
                errorGeneral = errGen,
                onSolicitarRecuperacion = { r, resp, nueva, conf ->
                    viewModel.ejecutarRecuperarClave(r, resp, nueva, conf)
                },
                onVolverAlLogin = {
                    viewModel.resetFlagsAuth()
                    navController.popBackStack()
                }
            )
        }

        composable("home_usuario") {
            val usuario by viewModel.usuarioActual.collectAsState()
            val cuotas by viewModel.cuotasUsuario.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.cargarCuotasUsuario()
            }

            InicioUsuarioScreen(
                usuario = usuario,
                cuotas = cuotas,
                onPagarCuota = { cuota ->
                    viewModel.pagarCuotaSeleccionada(cuota) {
                        navController.navigate("comprobante")
                    }
                },
                onIrPerfil = {
                    viewModel.limpiarMensajes()
                    navController.navigate("perfil")
                },
                onIrHistorial = { navController.navigate("historial") },
                onIrReportes = { navController.navigate("reportes_usuario") },
                onCerrarSesion = {
                    viewModel.sesionBorrar()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("comprobante") {
            val comprobante by viewModel.ultimoComprobante.collectAsState()
            comprobante?.let { comp ->
                ComprobantePagoScreen(
                    comprobante = comp,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable("historial") {
            val historial by viewModel.historialPagos.collectAsState()
            HistorialPagosScreen(
                historial = historial,
                onVolver = { navController.popBackStack() }
            )
        }

        composable("perfil") {
            val usuario by viewModel.usuarioActual.collectAsState()
            val ciudades by viewModel.ciudades.collectAsState()
            val errNom by viewModel.errorNombre.collectAsState()
            val errCor by viewModel.errorCorreo.collectAsState()
            val errCiu by viewModel.errorCiudad.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val errConf by viewModel.errorConfirmPassword.collectAsState()
            val errGen by viewModel.errorGeneral.collectAsState()
            val msgExito by viewModel.mensajeExito.collectAsState()

            PerfilScreen(
                usuario = usuario,
                ciudades = ciudades,
                errorNombre = errNom,
                errorCorreo = errCor,
                errorCiudad = errCiu,
                errorPassword = errPass,
                errorConfirmPassword = errConf,
                errorGeneral = errGen,
                mensajeExito = msgExito,
                onGuardarPerfil = { n, c, ciu -> viewModel.actualizarPerfilPropio(n, c, ciu) },
                onCambiarClave = { act, nueva, conf -> viewModel.cambiarClavePropia(act, nueva, conf) },
                onFotoTomada = { bmp -> viewModel.actualizarAvatarLocal(bmp) },
                onImagenElegida = { uri -> viewModel.actualizarAvatarDesdeUri(uri) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("reportes_usuario") {
            ReportesScreen(
                onVolver = { navController.popBackStack() },
                onEnviarReporte = { motivo, detalle ->
                    viewModel.crearReporteUsuario(motivo, detalle)
                },
                viewModel = viewModel
            )
        }

        composable("home_agente") {
            ReportesScreen(
                onVolver = {
                    viewModel.sesionBorrar()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onAtenderReporte = { reporte ->
                    reporteGestionar = reporte
                    navController.navigate("gestionar_reporte")
                },
                viewModel = viewModel
            )
        }

        composable("gestionar_reporte") {
            reporteGestionar?.let { rep ->
                GestionarReporteScreen(
                    reporte = rep,
                    onVolver = { navController.popBackStack() },
                    onGuardarResolucion = { id, resp, estado ->
                        viewModel.resolverReporteAgente(id, resp, estado) {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }

        composable("home_ejecutivo") {
            val usuario by viewModel.usuarioActual.collectAsState()
            LaunchedEffect(Unit) {
                viewModel.cargarUsuariosSistema()
            }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Panel Ejecutivo · ${usuario?.nombre ?: ""}") },
                        actions = {
                            IconButton(onClick = {
                                viewModel.sesionBorrar()
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                            }) {
                                Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PanelCobranzaScreen(
                        metricas = viewModel.calcularMetricasCobranza(),
                        onVolver = {
                            viewModel.limpiarMensajes()
                            navController.navigate("nuevo_compromiso")
                        }
                    )
                }
            }
        }

        composable("nuevo_compromiso") {
            val errRut by viewModel.errorRut.collectAsState()
            val errGen by viewModel.errorGeneral.collectAsState()

            NuevoCompromisoScreen(
                errorRut = errRut,
                errorGeneral = errGen,
                onPrevisualizarCuotas = { monto, cuotas, fecha ->
                    viewModel.generarVistaPreviaCuotas(monto, cuotas, fecha)
                },
                onCrearCompromiso = { rut, emp, conc, monto, cuotas, fecha ->
                    viewModel.crearCompromisoConCuotas(rut, emp, conc, monto, cuotas, fecha) {
                        navController.popBackStack()
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("home_admin") {
            val usuarios by viewModel.usuariosSistema.collectAsState()
            val msgExito by viewModel.mensajeExito.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.cargarUsuariosSistema()
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Administración de Usuarios") },
                        actions = {
                            IconButton(onClick = {
                                viewModel.sesionBorrar()
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                            }) {
                                Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (msgExito != null) {
                        Text(msgExito!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.limpiarMensajes()
                                navController.navigate("nuevo_compromiso")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.AddCircleOutline, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Crear Compromiso")
                        }
                        OutlinedButton(
                            onClick = { navController.navigate("perfil") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mi Perfil")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(usuarios, key = { it.id }) { u ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        usuarioEditar = u
                                        navController.navigate("editar_usuario_admin")
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column(modifier = Modifier.padding(start = 12.dp)) {
                                            Text(u.nombre, fontWeight = FontWeight.Bold)
                                            Text("RUT: ${u.rut} · ${u.ciudad}", style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                text = "Rol: ${viewModel.nombreDeRol(u.rolId)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text("Editar", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        composable("editar_usuario_admin") {
            val ciudades by viewModel.ciudades.collectAsState()
            UsuariosScreen(
                usuario = usuarioEditar,
                ciudades = ciudades,
                onGuardar = { u, nom, cor, ciu, rol ->
                    viewModel.actualizarUsuarioAdmin(u, nom, cor, ciu, rol)
                    navController.popBackStack()
                },
                onDesactivar = { id ->
                    viewModel.desactivarUsuarioAdmin(id) {
                        navController.popBackStack()
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}