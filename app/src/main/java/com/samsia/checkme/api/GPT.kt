package com.samsia.checkme.api

data class GptRequest(val messages: List<Message>, val model: String)
data class Message(val role: String, val content: String)

data class GptResponse(val choices: List<Choice>)
data class Choice(val message: Message)

