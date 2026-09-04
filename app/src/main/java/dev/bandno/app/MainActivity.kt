package dev.bandno.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import dev.bandno.app.ui.BandNoApp
import dev.bandno.app.ui.LocalAppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as BandNoApplication).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                BandNoApp()
            }
        }
    }
}
