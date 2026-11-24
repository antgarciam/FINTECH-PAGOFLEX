package cl.duocuc.miregistro.data

import cl.duocuc.miregistro.api.ActualizarUsuarioRequest
import cl.duocuc.miregistro.api.AuthApiService
import cl.duocuc.miregistro.api.CambiarClaveRequest
import cl.duocuc.miregistro.api.CiudadDto
import cl.duocuc.miregistro.api.LoginRequest
import cl.duocuc.miregistro.api.LoginResponse
import cl.duocuc.miregistro.api.RecuperarRequest
import cl.duocuc.miregistro.api.RegistroRequest
import cl.duocuc.miregistro.api.UsuarioDto
import cl.duocuc.miregistro.api.UsuariosApiService
import org.json.JSONObject
import retrofit2.Response

class UsuarioRepository(
    private val authApi: AuthApiService,
    private val usuariosApi: UsuariosApiService,
    private val sesion: SesionPreferencias
) {
    private fun extraerError(response: Response<*>, porDefecto: String): String {
        val raw = try {
            response.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            ""
        }
        val mensajeJson = Regex("\"(?:mensaje|message|error)\"\\s*:\\s*\"([^\"]+)\"")
            .find(raw)
            ?.groupValues
            ?.getOrNull(1)
        return when {
            !mensajeJson.isNullOrBlank() -> "$mensajeJson (HTTP ${response.code()})"
            raw.isNotBlank() -> "${raw.take(120)} (HTTP ${response.code()})"
            else -> "$porDefecto (HTTP ${response.code()})"
        }
    }

    suspend fun login(rut: String, contrasena: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(rut, contrasena))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sesion.guardarSesion(
                    token = body.token,
                    id = body.id,
                    nombre = body.nombre,
                    correo = body.correo,
                    rut = rut,
                    ciudad = body.ciudad,
                    rolId = body.rolId
                )
                Result.success(body)
            } else {
                Result.failure(
                    Exception(
                        extraerError(
                            response,
                            "RUT o contraseña incorrectos. Verifica tus datos e inténtalo nuevamente."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo conectar con el servidor. Verifica tu red y el API Gateway."))
        }
    }

    suspend fun registrar(request: RegistroRequest): Result<LoginResponse> {
        return try {
            val response = authApi.registrar(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        extraerError(
                            response,
                            "No se pudo completar el registro. Revisa que el RUT o correo no estén duplicados."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al registrar. Intenta nuevamente en unos segundos."))
        }
    }

    suspend fun recuperarClave(request: RecuperarRequest): Result<Unit> {
        return try {
            val response = authApi.recuperarClave(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        extraerError(
                            response,
                            "RUT o respuesta de seguridad incorrectos. Verifica ambos datos e intenta otra vez."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión con el servicio de recuperación."))
        }
    }

    suspend fun cambiarClave(actual: String, nueva: String): Result<Unit> {
        return try {
            val token = sesion.leerToken()
                ?: return Result.failure(Exception("Tu sesión expiró. Inicia sesión nuevamente."))
            val response = authApi.cambiarClave(
                "Bearer $token",
                CambiarClaveRequest(actual, nueva)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        extraerError(
                            response,
                            "La contraseña actual es incorrecta. Ingrésala nuevamente."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo actualizar la clave por un error de red."))
        }
    }

    suspend fun listarCiudades(): Result<List<CiudadDto>> {
        return try {
            val response = usuariosApi.listarCiudades()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("No se pudieron cargar las ciudades desde el servidor."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarUsuarios(): Result<List<UsuarioDto>> {
        return try {
            val response = usuariosApi.listarUsuarios()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("No se pudo obtener la lista de usuarios."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarUsuario(
        id: Long,
        nombre: String,
        correo: String,
        ciudad: String,
        rolId: Int? = null
    ): Result<UsuarioDto> {
        return try {
            val response = usuariosApi.actualizarUsuario(
                id,
                ActualizarUsuarioRequest(nombre, correo, ciudad, rolId)
            )
            if (response.isSuccessful && response.body() != null) {
                if (id == sesion.leerUsuarioId()) {
                    sesion.actualizarDatosLocales(nombre, correo, ciudad)
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        extraerError(
                            response,
                            "No se pudieron guardar los cambios del perfil."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red al actualizar los datos."))
        }
    }

    suspend fun desactivarUsuario(id: Long): Result<Unit> {
        return try {
            val response = usuariosApi.desactivarUsuario(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo desactivar el usuario seleccionado."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al desactivar el usuario."))
        }
    }
}