package cl.duocuc.miregistro.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duocuc.miregistro.model.MetricasCobranza
import cl.duocuc.miregistro.ui.screens.*
import cl.duocuc.miregistro.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val viewModel: UsuariosViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        // --- LOGIN ---
        composable("login") {
            val errGen by viewModel.errorGeneral.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val exito by viewModel.loginExitoso.collectAsState()

            LaunchedEffect(exito) {
                if (exito) {
                    // 👇 AQUÍ SE DECIDE A DÓNDE IR SEGÚN EL ROL
                    val rol = viewModel.obtenerRolActual()
                    when (rol) {
                        "1" -> navController.navigate("home_admin") { popUpTo("login") { inclusive = true } }
                        "2" -> navController.navigate("home_agente") { popUpTo("login") { inclusive = true } }
                        else -> navController.navigate("home_usuario") { popUpTo("login") { inclusive = true } } // Rol 3 por defecto
                    }
                }
            }

            LoginScreen(
                onLoginExitoso = { r, p -> viewModel.ejecutarLogin(r, p) },
                errorGeneral = errGen, errorPassword = errPass,
                onNavegarRecuperar = {}, onNavegarRegistro = { navController.navigate("registro") }
            )
        }

        // --- REGISTRO ---
        composable("registro") {
            val errNom by viewModel.errorNombre.collectAsState()
            val errRut by viewModel.errorRut.collectAsState()
            val errCor by viewModel.errorCorreo.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val errGen by viewModel.errorGeneral.collectAsState()
            val exito by viewModel.registroExitoso.collectAsState()

            LaunchedEffect(exito) { if (exito) navController.navigate("login") { popUpTo("registro") { inclusive = true } } }

            RegistroScreen(
                onRegistroExitoso = { n, r, c, ciu, p, cp -> viewModel.ejecutarRegistro(n, r, c, ciu, p, cp) },
                errorNombre = errNom, errorRut = errRut, errorCorreo = errCor, errorPassword = errPass, errorGeneral = errGen,
                onVolverLogin = { navController.popBackStack() }
            )
        }

        // --- PANTALLAS DE INICIO SEGÚN EL ROL ---

        // 1. Inicio del Usuario Final (Rol 3)
        composable("home_usuario") {
            PanelCobranzaScreen(
                metricas = MetricasCobranza(),
                onVolver = {
                    viewModel.sesionBorrar() // Cerramos sesión
                    navController.navigate("login") { popUpTo("home_usuario") { inclusive = true } }
                }
            )
        }

        // 2. Inicio del Agente (Rol 2)
        composable("home_agente") {
            ReportesScreen(
                onVolver = {
                    viewModel.sesionBorrar()
                    navController.navigate("login") { popUpTo("home_agente") { inclusive = true } }
                }
            )
        }

        // 3. Inicio del Administrador (Rol 1)
        composable("home_admin") {
            Text("Bienvenido Administrador (Aquí iría tu pantalla de gestión de usuarios)")
        }
    }
}