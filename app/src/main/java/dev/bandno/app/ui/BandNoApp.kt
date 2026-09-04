package dev.bandno.app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.bandno.app.R
import dev.bandno.app.ui.home.HomeScreen
import dev.bandno.app.ui.logs.LogsScreen
import dev.bandno.app.ui.oem.OemGuideScreen
import dev.bandno.app.ui.onboarding.OnboardingScreen
import dev.bandno.app.ui.settings.SettingsScreen
import dev.bandno.app.ui.theme.BandNoTheme

private data class Tab(val route: String, val label: Int, val icon: ImageVector)

private val Tabs = listOf(
    Tab("home", R.string.nav_home, Icons.Outlined.Home),
    Tab("logs", R.string.nav_logs, Icons.AutoMirrored.Outlined.List),
    Tab("settings", R.string.nav_settings, Icons.Outlined.Settings),
    Tab("oem", R.string.nav_oem, Icons.Outlined.Phone),
)

@Composable
fun BandNoApp() {
    BandNoTheme {
        val container = LocalAppContainer.current
        val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
        val navController = rememberNavController()
        val start = if (prefs.onboardingComplete) "home" else "onboarding"

        LaunchedEffect(prefs.onboardingComplete) {
            if (prefs.onboardingComplete &&
                navController.currentDestination?.route == "onboarding"
            ) {
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }
        }

        val backStack by navController.currentBackStackEntryAsState()
        val current = backStack?.destination?.route
        val showBar = current in Tabs.map { it.route }

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                if (showBar) {
                    NavigationBar {
                        Tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = current == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = stringResource(tab.label)) },
                                label = { Text(stringResource(tab.label)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = start,
                modifier = Modifier.padding(padding),
            ) {
                composable("onboarding") {
                    OnboardingScreen(onFinished = { })
                }
                composable("home") {
                    HomeScreen(onSeeLogs = { navController.navigate("logs") })
                }
                composable("logs") { LogsScreen() }
                composable("settings") { SettingsScreen() }
                composable("oem") { OemGuideScreen() }
            }
        }
    }
}
