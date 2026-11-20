package cl.duocuc.miregistro.data

import cl.duocuc.miregistro.api.CrearCompromisoRequest
import cl.duocuc.miregistro.api.CuotaDto
import cl.duocuc.miregistro.api.PagosApiService

class PagoRepository(private val api: PagosApiService) {
    suspend fun listarCuotasUsuario(usuarioId: Long): Result<List<CuotaDto>> {
        return try {
            val res = api.listarCuotasUsuario(usuarioId)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("No se pudieron cargar los compromisos del usuario."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión con el servicio de pagos."))
        }
    }

    suspend fun crearCompromiso(request: CrearCompromisoRequest): Result<Unit> {
        return try {
            val res = api.crearCompromiso(request)
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo crear el compromiso. Revisa los datos ingresados."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al registrar el compromiso."))
        }
    }

    suspend fun pagarCuota(cuotaId: Long): Result<Unit> {
        return try {
            val res = api.pagarCuota(cuotaId)
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo procesar el pago de la cuota."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al procesar el pago."))
        }
    }

    suspend fun desactivarCompromiso(compromisoId: Long): Result<Unit> {
        return try {
            val res = api.desactivarCompromiso(compromisoId)
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo desactivar el compromiso."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al desactivar el compromiso."))
        }
    }
}