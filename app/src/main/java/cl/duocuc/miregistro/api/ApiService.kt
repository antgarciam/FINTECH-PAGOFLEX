package cl.duocuc.miregistro.api

import cl.duocuc.miregistro.model.Reporte
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class LoginRequest(
    val rut: String,
    val contrasena: String
)

data class LoginResponse(
    val token: String,
    val id: Long,
    val nombre: String,
    val correo: String,
    val rolId: Int,
    val ciudad: String
)

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val rut: String,
    val contrasena: String,
    val ciudad: String,
    val preguntaSeguridad: String,
    val respuestaSeguridad: String
)

data class RecuperarRequest(
    val rut: String,
    val respuestaSeguridad: String,
    val nuevaContrasena: String
)

data class CambiarClaveRequest(
    val contrasenaActual: String,
    val contrasenaNueva: String
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val correo: String,
    val ciudad: String,
    val rolId: Int? = null
)

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rut: String,
    val ciudad: String,
    val rolId: Int,
    val activo: Boolean
)

data class CiudadDto(
    val id: Long,
    val nombre: String,
    val region: String
)

data class CrearCompromisoRequest(
    val usuarioId: Long,
    val empresa: String,
    val concepto: String,
    val montoTotal: Int,
    val numeroCuotas: Int,
    val fechaInicio: String
)

data class CuotaDto(
    val idCuota: Long,
    val compromisoId: Long,
    val empresa: String,
    val concepto: String,
    val cuotaActual: Int,
    val totalCuotas: Int,
    val monto: Int,
    val vencimiento: String,
    val estado: String
)

data class NotificacionDto(
    val id: Long,
    val usuarioId: Long,
    val mensaje: String,
    val leida: Boolean,
    val fechaHora: String
)

data class CrearReporteRequest(
    val clienteNombre: String,
    val clienteCorreo: String,
    val motivo: String,
    val detalle: String
)

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/registro")
    suspend fun registrar(@Body request: RegistroRequest): Response<LoginResponse>

    @POST("api/auth/recuperar")
    suspend fun recuperarClave(@Body request: RecuperarRequest): Response<Unit>

    @POST("api/auth/cambiar-clave")
    suspend fun cambiarClave(
        @Header("Authorization") bearerToken: String,
        @Body request: CambiarClaveRequest
    ): Response<Unit>
}

interface UsuariosApiService {
    @GET("api/ciudades")
    suspend fun listarCiudades(): Response<List<CiudadDto>>

    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioDto>>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body request: ActualizarUsuarioRequest
    ): Response<UsuarioDto>

    @DELETE("api/usuarios/{id}")
    suspend fun desactivarUsuario(@Path("id") id: Long): Response<Unit>
}

interface PagosApiService {
    @GET("api/pagos/cuotas/{usuarioId}")
    suspend fun listarCuotasUsuario(@Path("usuarioId") usuarioId: Long): Response<List<CuotaDto>>

    @POST("api/pagos/compromisos")
    suspend fun crearCompromiso(@Body request: CrearCompromisoRequest): Response<Unit>

    @POST("api/pagos/pagar/{cuotaId}")
    suspend fun pagarCuota(@Path("cuotaId") cuotaId: Long): Response<Unit>

    @DELETE("api/pagos/compromisos/{id}")
    suspend fun desactivarCompromiso(@Path("id") id: Long): Response<Unit>
}

interface NotificacionesApiService {
    @GET("api/notificaciones/{usuarioId}")
    suspend fun listarNotificaciones(@Path("usuarioId") usuarioId: Long): Response<List<NotificacionDto>>
}

interface ReportesApiService {
    @GET("api/reportes")
    suspend fun listarReportes(): Response<List<Reporte>>

    @POST("api/reportes")
    suspend fun crearReporte(@Body reporte: CrearReporteRequest): Response<Reporte>

    @PUT("api/reportes/{id}")
    suspend fun actualizarReporte(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<Reporte>
}