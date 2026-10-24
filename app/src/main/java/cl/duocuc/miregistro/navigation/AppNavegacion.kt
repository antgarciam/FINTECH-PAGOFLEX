package cl.duocuc.miregistro.navigation

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

    NavHost(navController = navController, startDestination = Ruta.Formulario.ruta) {
        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                onRegistroExitoso = { usuario ->
                    usuariosViewModel.registrar(usuario)
                    navController.navigate(Ruta.Perfil.ruta)
                },
                usuarioActual = usuarioActual,
                onVerMiPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onVerUsuarios = { navController.navigate(Ruta.Usuarios.ruta) },
                onCerrarSesion = { usuariosViewModel.cerrarSesion() }
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
            val usuario = remember(id) { usuariosViewModel.obtener(id) }
            if (usuario != null) {
                EditarScreen(
                    usuario = usuario,
                    roles = usuariosViewModel.roles,
                    onVolver = { navController.navigateUp() },
                    onGuardar = { editado ->
                        usuariosViewModel.actualizar(editado)
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}
