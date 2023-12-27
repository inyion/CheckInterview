package com.samsia.checkme.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.samsia.checkme.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashEnd: () -> Unit) {
    // 스플래시 화면 디자인
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.splash), contentDescription = "Splash Icon", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }

    // 일정 시간 후에 스플래시 화면 종료
    LaunchedEffect(key1 = true) {
        delay(1500) // 3초 대기
        onSplashEnd()
    }
}