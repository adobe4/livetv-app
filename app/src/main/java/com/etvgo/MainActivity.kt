package com.etvgo

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.etvgo.ui.screens.MainScreen
import com.etvgo.ui.theme.ETVGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen landscape
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            ETVGoTheme {
                MainScreen()
            }
        }
    }

    // Remote control / Keyboard navigation for TV
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_CHANNEL_UP,
            KeyEvent.KEYCODE_CHANNEL_DOWN,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // Handle remote control events
                super.onKeyDown(keyCode, event)
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
