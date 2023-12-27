package com.samsia.checkme.api

import com.samsia.checkme.CheckMeApp
import com.samsia.checkme.storage.SecureStorage
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenAiClient {
    private const val BASE_URL = "https://api.openai.com/"

    val gptRepository by lazy { GptRepository() }

    val gptApiService: GptApiService by lazy {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val secureStorage = SecureStorage(context = CheckMeApp.instance)
        val client = OkHttpClient.Builder()
            .addInterceptor(AddHeaderInterceptor(secureStorage.getApiKey()!!))
            .addInterceptor(loggingInterceptor)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GptApiService::class.java)
    }

    class AddHeaderInterceptor(private val apiKey: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            return chain.proceed(request)
        }
    }
}