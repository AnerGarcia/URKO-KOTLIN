package com.example.azterketa.core

import com.example.azterketa.network.WebService
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TIMEOUT_SECONDS = 30L

    // Interceptor de logging para ver cuerpos de request/response
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente OkHttp configurado
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        // Forzar sólo IPv4
        .dns(Dns { hostname ->
            Dns.SYSTEM.lookup(hostname)
                .filter { it.address.size == 4 }
        })
        .build()

    // Servicio Retrofit
    val webService: WebService by lazy {
        Retrofit.Builder()
            .baseUrl(Constantes.BASE_URL)              // tu URL base
            .client(okHttpClient)                      // usa el OkHttpClient mejorado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WebService::class.java)
    }
}
