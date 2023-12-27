package com.samsia.checkme.view

import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsia.checkme.CheckMeApp
import com.samsia.checkme.R
import com.samsia.checkme.vm.InterviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    interviewViewModel: InterviewViewModel,
    answer: String,
    isListening: Boolean,
    onMic: () -> Unit,
    onTextChange:(String) -> Unit) {

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            items(interviewViewModel.messages.size) { index ->
                val (type, text) = interviewViewModel.messages[index]
                val annotatedText = buildAnnotatedString {
                    val style = when(type) {
                        "Q" -> SpanStyle(fontSize = 16.sp)
                        "A" -> SpanStyle(color = Color.DarkGray, fontSize = 14.sp)
                        else -> SpanStyle(color = Color.LightGray, fontSize = 12.sp)
                    }
                    withStyle(style = style) {
                        append(text)
                    }
                }
                Text(text = annotatedText, modifier = Modifier.padding(bottom = 8.dp).pointerInput(text) { ->
                    detectTapGestures(
                        onLongPress = {
                            if (type == "Q") {
                                interviewViewModel.removeQuestion(text)
                                Toast.makeText(CheckMeApp.instance, "removed Question", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                })
            }
        }

        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)) {
            TextField(
                value = answer,
                onValueChange = { newText -> onTextChange(newText) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("answer", color = Color.DarkGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    interviewViewModel.askFeedback(answer)
                    onTextChange("")
                }),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent))

            Button(onClick = {
                onMic()
            },
                modifier = Modifier.padding(horizontal = 5.dp)) {
                Image(painter = painterResource(id = if (isListening) R.drawable.mic_speaking_icon else R.drawable.mic_icon),
                    contentDescription = "",
                    modifier = Modifier.size(20.dp))
            }
            Button(onClick = {
                interviewViewModel.addQuestion(answer)
                Toast.makeText(CheckMeApp.instance, "Added Question", Toast.LENGTH_LONG).show()
                onTextChange("")
            },
                modifier = Modifier.padding(horizontal = 0.dp)) {
                Image(painter = painterResource(id = R.drawable.add),
                    contentDescription = "",
                    modifier = Modifier.size(20.dp))
            }
            Button(onClick = {
                if (!TextUtils.isEmpty(answer)) {
                    interviewViewModel.askFeedback(answer)
                } else {
                    interviewViewModel.getNextQuestion()
                }

                onTextChange("") },
                modifier = Modifier.padding(horizontal = 5.dp)) {

                if (interviewViewModel.isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Image(painter = painterResource(id = R.drawable.ic_next_icon),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}