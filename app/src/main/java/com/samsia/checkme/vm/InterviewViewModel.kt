package com.samsia.checkme.vm

import android.text.TextUtils
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsia.checkme.CheckMeApp
import com.samsia.checkme.api.GptResponseResult
import com.samsia.checkme.api.OpenAiClient
import com.samsia.checkme.storage.AppDatabase
import com.samsia.checkme.storage.Question
import com.samsia.checkme.storage.QuestionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class InterviewViewModel : ViewModel() {
    private val questions = mutableStateListOf<Question>()
//        "PNG와 JPG의 차이점은?", "Dynamic Programming이란?", "Virtual Memory란?" // 예시 질문들
    val messages = mutableStateListOf<Pair<String, String>>()
    private val ask = MutableStateFlow("")
    private var removeIndex = -1
    var isLoading = mutableStateOf(false)
        private set

    var questionDao: QuestionDao = AppDatabase.getDatabase().questionDao()

    init {
        viewModelScope.launch {
            questions.addAll(questionDao.getAllQuestions())
        }
    }

    fun addQuestion(question: String) {
        viewModelScope.launch {
            questionDao.insert(Question(questionText = question))
        }
    }

    fun removeQuestion(questionStr: String) {
        viewModelScope.launch {
            for(questionData in questions) {
                if (questionData.questionText == questionStr) {
                    questionDao.remove(questionData)
                    return@launch
                }
            }
        }
    }

    fun getNextQuestion() {
        viewModelScope.launch {

            if(questions.size == 0) {
                Toast.makeText(CheckMeApp.instance, "Please Add Question!", Toast.LENGTH_LONG).show()
                return@launch
            }

            val index = Random.nextInt(questions.size)
            messages.add("Q" to questions[index].questionText)
            ask.value = questions[index].questionText
            removeIndex = index
        }
    }

    fun askFeedback(answer: String?) {
        isLoading.value = true
        viewModelScope.launch {

            if (TextUtils.isEmpty(answer)) {
                Toast.makeText(CheckMeApp.instance, "check answer value", Toast.LENGTH_LONG).show()
                return@launch
            }

            messages.add("A" to answer!!)

            val prompt = "질문: \"${ask.value}\"\n" +
                    "답변: \"${answer}\""

            val feedback = when (val response = OpenAiClient.gptRepository.getGptResponse(prompt)) {
                is GptResponseResult.Success -> {
                    questions.removeAt(removeIndex)
                    response.response.choices.first().message.content
                }
                is GptResponseResult.Error -> "Error: ${response.message}\n${ask.value}"
                null -> "No response yet\n${ask.value}"
            }
            messages.add("F" to feedback)
            isLoading.value = false
        }
    }
}