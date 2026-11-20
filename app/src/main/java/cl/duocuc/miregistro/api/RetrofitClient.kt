package cl.duocuc.miregistro.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val usuariosApi: UsuariosApiService by lazy { retrofit.create(UsuariosApiService::class.java) }
    val pagosApi: PagosApiService by lazy { retrofit.create(PagosApiService::class.java) }
    val notificacionesApi: NotificacionesApiService by lazy { retrofit.create(NotificacionesApiService::class.java) }
    val reportesApi: ReportesApiService by lazy { retrofit.create(ReportesApiService::class.java) }
}