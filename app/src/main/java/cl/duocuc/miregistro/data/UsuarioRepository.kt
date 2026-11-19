package cl.duocuc.miregistro.data

import cl.duocuc.miregistro.api.*

class UsuarioRepository(private val api: AuthApiService, private val sesion: SesionPreferencias) {
    suspend fun login(rut: String, contrasena: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(rut, contrasena))
            if (response.isSuccessful && response.body() != null) {
                sesion.guardarToken(response.body()!!.token)
                sesion.guardarRol(response.body()!!.rol)
                Result.success(response.body()!!)
            } else Result.failure(Exception("Credenciales incorrectas"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun registrar(request: RegistroRequest): Result<Unit> {
        return try {
            val response = api.registrar(request)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error al registrar"))
        } catch (e: Exception) { Result.failure(e) }
    }
}