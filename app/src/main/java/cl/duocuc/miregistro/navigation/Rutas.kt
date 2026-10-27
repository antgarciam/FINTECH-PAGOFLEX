package cl.duocuc.miregistro.navigation

sealed class Ruta(val ruta: String) {
    data object Formulario : Ruta("formulario")

    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Perfil : Ruta("perfil")
    data object Usuarios : Ruta("usuarios")

    data object Compromisos : Ruta("compromisos")

    data object Comprobante : Ruta("comprobante")

    data object HistorialPagos : Ruta("historial_pagos")

    data object Reportes : Ruta("reportes")

    data object PanelCobranza : Ruta("panel_cobranza")

    data object NuevoCompromiso : Ruta("nuevo_compromiso")

    data object Editar : Ruta("editar/{id}") {
        fun crear(id: Long) = "editar/$id"
    }
}