package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class OpenAiRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenAiMessage>
)

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiResponse(
    @Json(name = "choices") val choices: List<OpenAiChoice>?
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    @Json(name = "message") val message: OpenAiMessage?
)

interface OpenAiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenAiRequest
    ): OpenAiResponse
}

object GenericAiClient {
    private val client = OkHttpClient.Builder().build()

    val service: OpenAiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/") // Base url isn't strictly used since we pass @Url
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenAiService::class.java)
    }
}
