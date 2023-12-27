package com.samsia.checkme.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GptApiService {
    @POST("v1/chat/completions")
    suspend fun askGpt(@Body request: GptRequest): Response<GptResponse>
}