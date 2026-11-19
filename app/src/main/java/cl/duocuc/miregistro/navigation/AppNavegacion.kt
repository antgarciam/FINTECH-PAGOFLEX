package cl.duocuc.miregistro.navigation

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
        composable("login") {
            val errGen by viewModel.errorGeneral.collectAsState()
            val errPass by viewModel.errorPassword.collectAsState()
            val exito by viewModel.loginExitoso.collectAsState()

            LaunchedEffect(exito) { if (exito) navController.navigate("home") { popUpTo("login") { inclusive = true } } }

            LoginScreen(
                onLoginExitoso = { r, p -> viewModel.ejecutarLogin(r, p) },
                errorGeneral = errGen, errorPassword = errPass,
                onNavegarRecuperar = {}, onNavegarRegistro = { navController.navigate("registro") }
            )
        }

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

        composable("home") {
            PanelCobranzaScreen(metricas = MetricasCobranza(), onVolver = { navController.navigate("login") })
        }
    }
}