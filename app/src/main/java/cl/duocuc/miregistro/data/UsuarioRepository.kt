package cl.duocuc.miregistro.data

import cl.duocuc.miregistro.api.*

class UsuarioRepository(private val api: AuthApiService, private val sesion: SesionPreferencias) {


    suspend fun login(rut: String, contrasena: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(rut, contrasena))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sesion.guardarToken(body.token)

                sesion.guardarRol(body.rolId.toString())

                Result.success(body)
            } else {
                Result.failure(Exception("RUT o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrar(request: RegistroRequest): Result<Unit> {
        return try {
            val response = api.registrar(request)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error al registrar"))
        } catch (e: Exception) { Result.failure(e) }
    }
}