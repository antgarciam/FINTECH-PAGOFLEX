package cl.duocuc.miregistro.api

import cl.duocuc.miregistro.model.Reporte
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val rut: String, val contrasena: String)

// 👇 Alineado con el backend (devuelve id y rolId)
data class LoginResponse(
    val token: String,
    val id: Long,
    val nombre: String,
    val correo: String,
    val rolId: Int,
    val ciudad: String
)

// 👇 Este estaba perfecto
data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val rut: String,
    val contrasena: String,
    val ciudad: String,
    val preguntaSeguridad: String,
    val respuestaSeguridad: String
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

interface UsuariosApiService {
    // Aquí irán tus endpoints de usuarios (GET /api/usuarios, etc.)
}

interface PagosApiService {
    // Aquí irán tus endpoints de pagos (GET /api/pagos/cuotas/{id}, etc.)
}

interface NotificacionesApiService {
    // Aquí irán tus endpoints de notificaciones
}