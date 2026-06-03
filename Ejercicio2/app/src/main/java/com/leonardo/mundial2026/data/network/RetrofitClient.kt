package com.leonardo.mundial2026.data.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// Configuración de Retrofit para conectarnos a internet
object RetrofitClient {
    private const val TAG = "Mundial2026_Retrofit"
    private const val BASE_URL = "https://v3.football.api-sports.io/"

    // Ignorar errores de certificados SSL en Android 7
    fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            
            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(20, TimeUnit.SECONDS) // Tiempo de espera para conectar
                .readTimeout(20, TimeUnit.SECONDS)    // Tiempo de espera para leer datos
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // Creamos la instancia de la API para usarla en toda la aplicación
    val instance: FootballApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient()) // Usamos el cliente especial para evitar errores de SSL
            .addConverterFactory(GsonConverterFactory.create()) // Para convertir el JSON a objetos Kotlin
            .build()
            .create(FootballApiService::class.java)
    }
}
