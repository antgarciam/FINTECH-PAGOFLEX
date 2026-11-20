package cl.duocuc.miregistro.data

import cl.duocuc.miregistro.api.CrearReporteRequest
import cl.duocuc.miregistro.api.ReportesApiService
import cl.duocuc.miregistro.model.Reporte

class ReporteRepository(private val api: ReportesApiService) {
    suspend fun listarReportes(): Result<List<Reporte>> {
        return try {
            val res = api.listarReportes()
            if (res.isSuccessful) {
                Result.success(res.body() ?: emptyList())
            } else {
                Result.failure(Exception("No se pudieron obtener los reportes."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión con el servicio de reportes."))
        }
    }

    suspend fun crearReporte(request: CrearReporteRequest): Result<Reporte> {
        return try {
            val res = api.crearReporte(request)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("No se pudo enviar el reporte de incidencia."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red al enviar el reporte."))
        }
    }

    suspend fun actualizarReporte(id: Long, estado: String, respuesta: String): Result<Reporte> {
        return try {
            val res = api.actualizarReporte(
                id,
                mapOf("estado" to estado, "respuestaAgente" to respuesta)
            )
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("No se pudo actualizar la resolución del reporte."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al actualizar el reporte."))
        }
    }
}