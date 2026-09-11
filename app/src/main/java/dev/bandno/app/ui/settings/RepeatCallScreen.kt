package dev.bandno.app.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import kotlinx.coroutines.launch

@Composable
fun RepeatCallScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val screen = prefs.screen
    val scope = rememberCoroutineScope()
    var intervalText by remember(screen.r3IntervalMinutes) {
        mutableStateOf(screen.r3IntervalMinutes.toString())
    }

    RuleDetailScaffold(
        title = stringResource(R.string.settings_repeat_title),
        onBack = onBack,
    ) {
        SettingsCard {
            ToggleRow(
                title = stringResource(R.string.settings_enabled),
                summary = stringResource(R.string.settings_repeat_enable_hint),
                checked = screen.r3Enabled,
                onChecked = { on ->
                    scope.launch {
                        container.settingsRepository.updateScreen { it.copy(r3Enabled = on) }
                    }
                },
            )
        }
        SettingsHint(stringResource(R.string.settings_repeat_hint))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        ) {
            OutlinedTextField(
                value = intervalText,
                onValueChange = { raw ->
                    val digits = raw.filter { it.isDigit() }.take(3)
                    intervalText = digits
                    val minutes = digits.toIntOrNull() ?: return@OutlinedTextField
                    if (minutes in 1..180) {
                        scope.launch {
                            container.settingsRepository.updateScreen {
                                it.copy(r3IntervalMinutes = minutes)
                            }
                        }
                    }
                },
                label = { Text(stringResource(R.string.settings_r3_interval)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }

        SettingsCard {
            ToggleRow(
                title = stringResource(R.string.settings_r3_require_blocked),
                summary = stringResource(R.string.settings_r3_require_blocked_hint),
                checked = screen.r3RequireFirstBlocked,
                onChecked = { checked ->
                    scope.launch {
                        container.settingsRepository.updateScreen {
                            it.copy(r3RequireFirstBlocked = checked)
                        }
                    }
                },
            )
        }
    }
}
