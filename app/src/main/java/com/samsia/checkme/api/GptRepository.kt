package com.samsia.checkme.api

import java.io.IOException

class GptRepository {
    suspend fun getGptResponse(prompt: String): GptResponseResult? {
        return try {

            val systemMessage = Message("system", "지금부터 면접관이라고 생각하고 다음 질문에 대한 답변을 들어보고 " +
            "전체 답변을 긴 설명 없이 답변의 아쉬운 부분을 포함해서 핵심만 짧게 길어도 두줄 정도의 피드백만 주고 " +
            "피드백을 대답할 때 문장의 제일 마지막에 최소 0점부터 100점 까지 중에 몇점을 줄지 (score:80) 이런 포맷으로 표시해줘. " +
            "점수는 되도록 현실적으로 줬으면 좋겠어 대답을 개선할 의지가 생기도록 틀린말은 아니더라도 내용이 너무 없다면 20점부터 시작하면 좋을것 같아")
            val userMessage = Message("user", prompt)
            val messages = listOf(systemMessage, userMessage)
            val response = OpenAiClient.gptApiService.askGpt(GptRequest(messages, "gpt-3.5-turbo"))
            if (response.isSuccessful) {
                GptResponseResult.Success(response.body()!!)
            } else {
                GptResponseResult.Error("API Error: ${response.code()}")
            }
        } catch (e: IOException) {
            GptResponseResult.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            GptResponseResult.Error("Unexpected Error: ${e.message}")
        }
    }
}

sealed class GptResponseResult {
    data class Success(val response: GptResponse) : GptResponseResult()
    data class Error(val message: String) : GptResponseResult()
}