package dev.bandno.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import dev.bandno.app.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
