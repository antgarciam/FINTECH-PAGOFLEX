package cl.duocuc.miregistro.data

import cl.duocuc.miregistro.api.ReportesApiService
import cl.duocuc.miregistro.model.Reporte

class ReporteRepository(private val api: ReportesApiService) {
    suspend fun listarReportes(): Result<List<Reporte>> {
        return try {
            val res = api.listarReportes()
            if (res.isSuccessful) Result.success(res.body() ?: emptyList()) else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }
    suspend fun crearReporte(reporte: Reporte): Result<Reporte> {
        return try {
            val res = api.crearReporte(reporte)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }
}