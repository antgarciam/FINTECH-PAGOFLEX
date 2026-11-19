package cl.duocuc.miregistro.api

import cl.duocuc.miregistro.model.Reporte
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val rut: String, val contrasena: String)
data class LoginResponse(val token: String, val rol: String, val idUsuario: Long)
data class RegistroRequest(
    val rut: String, val nombre: String, val correo: String,
    val contrasena: String, val ciudad: String,
    val preguntaSeguridad: String, val respuestaSeguridad: String
)

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/registro")
    suspend fun registrar(@Body request: RegistroRequest): Response<Unit>
}

interface ReportesApiService {
    @GET("api/reportes")
    suspend fun listarReportes(): Response<List<Reporte>>

    @POST("api/reportes")
    suspend fun crearReporte(@Body reporte: Reporte): Response<Reporte>
}

// Los otros 3 microservicios (listos para cuando los necesiten)
interface UsuariosApiService
interface PagosApiService
interface NotificacionesApiService