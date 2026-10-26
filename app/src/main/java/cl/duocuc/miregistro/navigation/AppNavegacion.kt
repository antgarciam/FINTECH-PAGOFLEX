package cl.duocuc.miregistro.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.miregistro.ui.screens.EditarScreen
import cl.duocuc.miregistro.ui.screens.FormularioScreen
import cl.duocuc.miregistro.ui.screens.PerfilScreen
import cl.duocuc.miregistro.ui.screens.UsuariosScreen
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel
import cl.duocuc.miregistro.ui.screens.CompromisosScreen
import cl.duocuc.miregistro.ui.screens.ComprobantePagoScreen
import cl.duocuc.miregistro.ui.screens.HistorialPagosScreen
import cl.duocuc.miregistro.ui.screens.LoginScreen
import cl.duocuc.miregistro.ui.screens.ReporteProblema
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.miregistro.ui.screens.RegistroScreen
import cl.duocuc.miregistro.ui.screens.ReportesScreen

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

    NavHost(navController = navController, startDestination = Ruta.Login.ruta) {

        composable(Ruta.Registro.ruta) {
            val context = LocalContext.current

            RegistroScreen(
                onRegistroExitoso = { usuario ->
                    val error = usuariosViewModel.validarRegistro(usuario)

                    if (error != null) {
                        Toast.makeText(
                            context,
                            error,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        usuariosViewModel.registrar(usuario)

                        Toast.makeText(
                            context,
                            "Cuenta creada correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate(Ruta.Formulario.ruta) {
                            popUpTo(Ruta.Login.ruta)
                        }
                    }
                },
                onMostrarMensaje = { mensaje ->
                    Toast.makeText(
                        context,
                        mensaje,
                        Toast.LENGTH_SHORT
                    ).show()
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
                    // 1. Validamos formato de RUT chileno
                    if (!usuariosViewModel.validarRutChileno(rutIngresado)) {
                        Toast.makeText(context, "El RUT ingresado no tiene un formato válido.", Toast.LENGTH_SHORT).show()
                        return@LoginScreen
                    }

                    // 2. Buscamos el usuario en la BD de la app
                    val usuarioEncontrado = usuariosViewModel.buscarPorRut(rutIngresado)

                    if (usuarioEncontrado != null) {
                        // 3. VALIDACIÓN DE CONTRASEÑA REAL
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
        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                onRegistroExitoso = { usuario ->
                    usuariosViewModel.registrar(usuario)
                    navController.navigate(Ruta.Perfil.ruta)
                },
                usuarioActual = usuarioActual,
                onVerMiPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                },
                onVerUsuarios = {
                    navController.navigate(Ruta.Usuarios.ruta)
                },
                onVerCompromisos = {
                    navController.navigate(Ruta.Compromisos.ruta)
                },
                onVerHistorialPagos = {
                    navController.navigate(Ruta.HistorialPagos.ruta)
                },
                onVerReportes = {
                    navController.navigate(Ruta.Reportes.ruta)
                },
                onCerrarSesion = {
                    usuariosViewModel.cerrarSesion()

                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("recuperar") {
            cl.duocuc.miregistro.ui.screens.RecuperarContrasenaScreen(
                onRestablecerExitoso = {
                    navController.popBackStack() // Regresa al Login
                },
                onVolverAlLogin = {
                    navController.popBackStack()
                },
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
                // NUEVO (Cámara / Galería): la pantalla avisa; el ViewModel convierte y guarda en la BD
                onFotoTomada = usuariosViewModel::cambiarAvatarDesdeCamara,
                onImagenElegida = usuariosViewModel::cambiarAvatarDesdeGaleria,

            )
        }

        composable(Ruta.Usuarios.ruta) {
            UsuariosScreen(
                usuarios = usuarios,
                onVolver = { navController.navigateUp() },
                onEditar = { id -> navController.navigate(Ruta.Editar.crear(id)) },
                filtro = filtro,
                onBuscar = usuariosViewModel::buscar,
                onEliminar = usuariosViewModel::eliminar
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
                    onVolver = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Ruta.HistorialPagos.ruta) {
            HistorialPagosScreen(
                historial = historialPagos,
                onVolver = {
                    navController.navigateUp()
                }
            )
        }


        composable(
            route = Ruta.Editar.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L

            val reporteSimulado = remember(id) {
                ReporteProblema(
                    numeroReporte = "REP-00${id}",
                    folioCompromiso = "CP-2026-0106",
                    rutUsuario = "12.476.318-5",
                    motivo = "El monto no corresponde",
                    comentario = "Nose porque me cobran $318.500.",
                    estado = "Abierto"
                )
            }

            EditarScreen(
                reporte = reporteSimulado,
                onVolver = { navController.navigateUp() },
                onGuardarResolucion = { respuesta, estadoCompromiso, estadoReporte ->
                    navController.navigateUp()
                }
            )
        }

        composable(Ruta.Reportes.ruta) {
            ReportesScreen(
                onVolver = { navController.navigateUp() },
                onEnviarReporte = { motivo, detalle ->
                    usuariosViewModel.guardarReporteEnBD(motivo, detalle)
                }
            )
        }
    }
}
