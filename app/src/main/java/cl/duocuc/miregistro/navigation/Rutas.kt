package cl.duocuc.miregistro.navigation

sealed class Ruta(val ruta: String) {
    data object Formulario : Ruta("formulario")
    data object Perfil : Ruta("perfil")
    data object Usuarios : Ruta("usuarios")

    data object Compromisos : Ruta("compromisos")

    data object Editar : Ruta("editar/{id}") {
        fun crear(id: Long) = "editar/$id"
    }
}