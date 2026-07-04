package com.viniptv.ui.splash

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.MainActivity
import com.viniptv.ui.theme.VinColors
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    private var soundPool: SoundPool? = null
    private var openSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attrs)
            .build()

        try {
            openSoundId = soundPool?.load(this, resources.getIdentifier("open_sound", "raw", packageName), 1) ?: 0
        } catch (e: Exception) { }

        setContent {
            SplashContent()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try { soundPool?.play(openSoundId, 1f, 1f, 1, 0, 1f) } catch (_: Exception) {}
        }, 200)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }

    override fun onDestroy() {
        soundPool?.release()
        soundPool = null
        super.onDestroy()
    }
}

@Composable
private fun SplashContent() {
    var visible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(800)
        subtitleVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF080808), Color(0xFF1A1A2E)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (visible) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(800)) + scaleIn(initialScale = 0.8f, animationSpec = tween(1000))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(VinColors.Accent.copy(alpha = 0.3f), Color.Transparent),
                                        radius = 50f
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("V", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = VinColors.Accent)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Vin IPTV", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = VinColors.TextPrimary, letterSpacing = 2.sp)
                        Text("Premium Television Experience", fontSize = 14.sp, color = VinColors.TextTertiary, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(60.dp))

            if (subtitleVisible) {
                Text("Loading your channels...", fontSize = 13.sp, color = VinColors.TextSecondary, modifier = Modifier.alpha(0.7f))
            }
        }
    }
}
