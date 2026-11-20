package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object BazmoServerConfig {
    val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(35, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build()
    }

    val longOpOkHttpClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .readTimeout(95, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .callTimeout(100, TimeUnit.SECONDS)
            .build()
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    fun baseUrl(): String {
        val raw = BuildConfig.BAZMO_API_BASE_URL.trim().trimEnd('/')
        return raw.ifBlank { "https://bazmo.weblines.ir" }
    }

    fun wsUrl(base: String = baseUrl()): String {
        val normalized = base.trim().trimEnd('/')
        val wsBase = when {
            normalized.startsWith("https://") -> "wss://" + normalized.removePrefix("https://")
            normalized.startsWith("http://") -> "ws://" + normalized.removePrefix("http://")
            else -> "wss://$normalized"
        }
        return "$wsBase/ws"
    }
}
