package cl.duocuc.miregistro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.api.CuotaDto
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioUsuarioScreen(
    usuario: Usuario?,
    cuotas: List<CuotaDto>,
    onPagarCuota: (CuotaDto) -> Unit,
    onIrPerfil: () -> Unit,
    onIrHistorial: () -> Unit,
    onIrReportes: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val formatoClp = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))

    var filtro by remember { mutableStateOf("Por pagar") }
    var cuotaConfirmar by remember { mutableStateOf<CuotaDto?>(null) }

    val porPagar = cuotas.filter { !it.estado.equals("Pagado", ignoreCase = true) }
    val pagadas = cuotas.filter { it.estado.equals("Pagado", ignoreCase = true) }
    val totalDeuda = porPagar.sumOf { it.monto }

    val listaVisible = when (filtro) {
        "Pagadas" -> pagadas
        "Todas" -> porPagar + pagadas
        else -> porPagar
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PagoFlex",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = usuario?.nombre ?: "Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Usuario Final · ${usuario?.ciudad ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Mis compromisos por pagar") },
                    icon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Historial de pagos") },
                    icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onIrHistorial()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Mi perfil y seguridad") },
                    icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onIrPerfil()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Reportar incidencia") },
                    icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onIrReportes()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mis Pagos Pendientes", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(R.drawable.ic_menu), contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = onIrPerfil) {
                            Icon(Icons.Outlined.AccountCircle, contentDescription = "Mi perfil")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Total por pagar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Text(
                            text = formatoClp.format(totalDeuda),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (porPagar.isEmpty()) "Estás al día con todos tus compromisos"
                            else "${porPagar.size} cuota(s) pendiente(s) lista(s) para pagar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filtro == "Por pagar",
                        onClick = { filtro = "Por pagar" },
                        label = { Text("Por pagar (${porPagar.size})") }
                    )
                    FilterChip(
                        selected = filtro == "Pagadas",
                        onClick = { filtro = "Pagadas" },
                        label = { Text("Pagadas (${pagadas.size})") }
                    )
                    FilterChip(
                        selected = filtro == "Todas",
                        onClick = { filtro = "Todas" },
                        label = { Text("Todas (${cuotas.size})") }
                    )
                }

                if (listaVisible.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No tienes compromisos en esta categoría.",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listaVisible, key = { it.idCuota }) { cuota ->
                            val yaPagada = cuota.estado.equals("Pagado", ignoreCase = true)
                            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cuota.empresa,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = formatoClp.format(cuota.monto),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (yaPagada) MaterialTheme.colorScheme.secondary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(text = cuota.concepto, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "Cuota ${cuota.cuotaActual}/${cuota.totalCuotas} · Vence: ${cuota.vencimiento}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (yaPagada) {
                                            OutlinedButton(
                                                onClick = {},
                                                enabled = false,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Cuota pagada")
                                            }
                                        } else {
                                            Button(
                                                onClick = { cuotaConfirmar = cuota },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Payments,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.size(8.dp))
                                                Text("Pagar ahora", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (cuotaConfirmar != null) {
        val cuota = cuotaConfirmar!!
        AlertDialog(
            onDismissRequest = { cuotaConfirmar = null },
            title = { Text("Confirmar pago rápido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Empresa: ${cuota.empresa}")
                    Text("Concepto: ${cuota.concepto}")
                    Text("Cuota: ${cuota.cuotaActual}/${cuota.totalCuotas}")
                    Text(
                        text = "Total a pagar: ${formatoClp.format(cuota.monto)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cuotaConfirmar = null
                        onPagarCuota(cuota)
                    }
                ) {
                    Text("Confirmar pago")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cuotaConfirmar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}