package com.samsia.checkme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samsia.checkme.storage.AppDatabase
import com.samsia.checkme.storage.SecureStorage
import com.samsia.checkme.ui.theme.CheckMeTheme
import com.samsia.checkme.view.ApiKeyInputScreen
import com.samsia.checkme.view.InterviewScreen
import com.samsia.checkme.view.SplashScreen
import com.samsia.checkme.vm.InterviewViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    private val apiKey = mutableStateOf<String?>(null)
    private val isListeningFlow = MutableStateFlow(false)
    lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorage = SecureStorage(context = this)
        apiKey.value = secureStorage.getApiKey()

        setContent {
            val navController = rememberNavController()
            CheckMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(onSplashEnd = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("main") {
                            MainScreen()
                        }
                    }

                }
            }
        }
    }

    private fun startListening(speechRecognizer: SpeechRecognizer, onResult: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                isListeningFlow.update { true }
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(p0: Float) {
            }

            override fun onBufferReceived(p0: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                isListeningFlow.update { false }
            }

            override fun onError(p0: Int) {
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onPartialResults(p0: Bundle?) {
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
            }
        })

        speechRecognizer.startListening(intent)
    }

    @Composable
    fun MainScreen() {
        if (apiKey.value == null) {
            ApiKeyInputScreen(onApiKeySubmit = { key ->
                secureStorage.saveApiKey(key)
                apiKey.value = key
            })
        } else {

            val interviewViewModel: InterviewViewModel = viewModel()
            var answer by remember { mutableStateOf("") }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    startListening(speechRecognizer) { text ->
                        answer = text
                        isListeningFlow.update { false }
                    }
                    isListeningFlow.update { true }
                } else {
                    // 권한 거부 처리
                }
            }
            val isListening by isListeningFlow.asStateFlow().collectAsState()

            InterviewScreen(interviewViewModel, answer, isListening,
                onMic = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) -> {
                            startListening(speechRecognizer) { text ->
                                answer = text
                                isListeningFlow.update { false }
                            }
                            isListeningFlow.update { true }
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onTextChange = { newText ->
                    answer = newText
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}