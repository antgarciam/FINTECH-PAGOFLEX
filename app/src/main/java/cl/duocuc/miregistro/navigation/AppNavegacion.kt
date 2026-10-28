package cl.duocuc.miregistro.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duocuc.miregistro.model.Reporte
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.screens.CompromisosScreen
import cl.duocuc.miregistro.ui.screens.ComprobantePagoScreen
import cl.duocuc.miregistro.ui.screens.EditarScreen
import cl.duocuc.miregistro.ui.screens.FormularioScreen
import cl.duocuc.miregistro.ui.screens.GestionarReporteScreen
import cl.duocuc.miregistro.ui.screens.HistorialPagosScreen
import cl.duocuc.miregistro.ui.screens.LoginScreen
import cl.duocuc.miregistro.ui.screens.NuevoCompromisoScreen
import cl.duocuc.miregistro.ui.screens.PanelCobranzaScreen
import cl.duocuc.miregistro.ui.screens.PerfilScreen
import cl.duocuc.miregistro.ui.screens.RegistroScreen
import cl.duocuc.miregistro.ui.screens.ReportesScreen
import cl.duocuc.miregistro.ui.screens.UsuariosScreen
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel
import kotlin.collections.emptyList



@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val filtro by usuariosViewModel.filtro.collectAsStateWithLifecycle()
    val compromisos by usuariosViewModel.compromisos.collectAsStateWithLifecycle()
    val comprobanteActual by usuariosViewModel.comprobanteActual.collectAsStateWithLifecycle()
    val historialPagos by usuariosViewModel.historialPagos.collectAsStateWithLifecycle()
    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null)}
    var reporteSeleccionado by remember { mutableStateOf<Reporte?>(null) }

    NavHost(navController = navController, startDestination = Ruta.Login.ruta) {

        composable(Ruta.Registro.ruta) {
            val context = LocalContext.current

            RegistroScreen(
                onRegistroExitoso = { usuario ->
                    val error = usuariosViewModel.validarRegistro(usuario)

                    if (error != null) {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    } else {
                        usuariosViewModel.registrar(usuario)
                        Toast.makeText(context, "Cuenta creada correctamente.", Toast.LENGTH_SHORT).show()

                        navController.navigate(Ruta.Formulario.ruta) {
                            popUpTo(Ruta.Login.ruta)
                        }
                    }
                },
                onMostrarMensaje = { mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                },
                onVolverLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Ruta.Login.ruta) {
            val context = LocalContext.current

            LoginScreen(
                onLoginExitoso = { rutIngresado, contraseniaIngresada ->
                    if (!usuariosViewModel.validarRutChileno(rutIngresado)) {
                        Toast.makeText(context, "El RUT ingresado no tiene un formato válido.", Toast.LENGTH_SHORT).show()
                        return@LoginScreen
                    }

                    val usuarioEncontrado = usuariosViewModel.buscarPorRut(rutIngresado)

                    if (usuarioEncontrado != null) {
                        if (usuarioEncontrado.contrasena == contraseniaIngresada) {
                            usuariosViewModel.iniciarSesion(rutIngresado)
                            navController.navigate(Ruta.Formulario.ruta) {
                                popUpTo(Ruta.Login.ruta) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Contraseña incorrecta. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Usuario no encontrado. Utilice un RUT registrado.", Toast.LENGTH_LONG).show()
                    }
                },
                onMostrarMensaje = { mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                },
                onNavegarRecuperar = {
                    navController.navigate("recuperar")
                },
                onNavegarRegistro = {
                    navController.navigate(Ruta.Registro.ruta)
                }
            )
        }

        composable(route = Ruta.Formulario.ruta) {
            val compromisosDb by usuariosViewModel.compromisos.collectAsStateWithLifecycle(initialValue = emptyList())

            FormularioScreen(
                compromisos = compromisosDb,
                onRegistroExitoso = { usuario ->
                    usuariosViewModel.registrar(usuario)
                    navController.navigate(route = Ruta.Perfil.ruta)
                },
                usuarioActual = usuarioActual,
                onVerMiPerfil = { navController.navigate(route = Ruta.Perfil.ruta) },
                onVerUsuarios = { navController.navigate(route = Ruta.Usuarios.ruta) },
                onVerCompromisos = { navController.navigate(route = Ruta.Compromisos.ruta) },
                onVerHistorialPagos = { navController.navigate(route = Ruta.HistorialPagos.ruta) },
                onVerReportes = { navController.navigate(route = Ruta.Reportes.ruta) },
                onVerPanelCobranza = { navController.navigate(route = Ruta.PanelCobranza.ruta) },
                onCrearCompromiso = { navController.navigate(route = Ruta.NuevoCompromiso.ruta) },
                onCerrarSesion = {
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(route = Ruta.Login.ruta) {
                        popUpTo(id = 0) { inclusive = true }
                    }
                }
            )
        }
        composable("recuperar") {
            cl.duocuc.miregistro.ui.screens.RecuperarContrasenaScreen(
                onRestablecerExitoso = { navController.popBackStack() },
                onVolverAlLogin = { navController.popBackStack() },
                onActualizarClaveEnBD = { rut, nuevaClave ->
                    usuariosViewModel.actualizarContrasena(rut, nuevaClave)
                }
            )
        }

        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuario = usuarioActual,
                onVolver = { navController.navigateUp() },
                onVerCompromisos = { navController.navigate(Ruta.Compromisos.ruta) },
                onVerHistorialPagos = { navController.navigate(Ruta.HistorialPagos.ruta) },
                onFotoTomada = usuariosViewModel::cambiarAvatarDesdeCamara,
                onImagenElegida = usuariosViewModel::cambiarAvatarDesdeGaleria,
            )
        }

        composable(route = Ruta.Usuarios.ruta) {
            EditarScreen(
                usuarios = usuarios,
                onUsuarioSeleccionado = { usuario ->
                    usuarioAEditar = usuario
                    navController.navigate("gestionar_usuarios")
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(route = "gestionar_usuarios") {
            UsuariosScreen(
                usuario = usuarioAEditar,
                onGuardar = { usuarioActualizado ->
                    usuariosViewModel.actualizar(usuarioActualizado)
                    navController.navigateUp()
                },
                onEliminar = { usuarioAEliminar ->
                    usuariosViewModel.eliminar(usuarioAEliminar)
                    usuarioAEditar = null
                    navController.navigateUp()
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.Compromisos.ruta) {
            CompromisosScreen(
                compromisos = compromisos,
                puedePagar = usuariosViewModel::puedePagar,
                onVolver = { navController.navigateUp() },
                onPagar = { compromiso ->
                    val comprobante = usuariosViewModel.pagarCompromiso(compromiso)

                    if (comprobante != null) {
                        navController.navigate(Ruta.Comprobante.ruta)
                    }
                }
            )
        }

        composable(Ruta.Comprobante.ruta) {
            comprobanteActual?.let { comprobante ->
                ComprobantePagoScreen(
                    comprobante = comprobante,
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable(Ruta.HistorialPagos.ruta) {
            HistorialPagosScreen(
                historial = historialPagos,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.Reportes.ruta) {
            ReportesScreen(
                onVolver = { navController.navigateUp() },
                onEnviarReporte = { motivo, detalle ->
                    usuariosViewModel.guardarReporteEnBD(motivo, detalle)
                },
                onAtenderReporte = { reporte ->
                    reporteSeleccionado = reporte
                    navController.navigate("gestionar_reporte")
                }
            )
        }

        composable("gestionar_reporte") {
            reporteSeleccionado?.let { reporte ->
                GestionarReporteScreen(
                    reporte = reporte,
                    onVolver = { navController.navigateUp() },
                    onGuardarResolucion = { reporteId, respuesta, nuevoEstado ->
                        usuariosViewModel.actualizarEstadoReporteEnBD(reporteId, nuevoEstado, respuesta)
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Ruta.PanelCobranza.ruta) {
            val metricas = usuariosViewModel.obtenerMetricasCobranza("Altos del Maipo")
            PanelCobranzaScreen(
                metricas = metricas,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.NuevoCompromiso.ruta) {
            NuevoCompromisoScreen(
                onVolver = { navController.navigateUp() },
                onValidarRut = { rut -> usuariosViewModel.validarRutChileno(rut) },
                onCrearCompromiso = { rut, concepto, monto, vencimiento ->
                    usuariosViewModel.crearNuevoCompromiso(rut, concepto, monto, vencimiento)
                }
            )
        }
    }
}