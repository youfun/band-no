package dev.bandno.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import kotlinx.coroutines.launch

@Composable
fun LogsSettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val screen = prefs.screen
    val scope = rememberCoroutineScope()
    var confirmClearLogs by remember { mutableStateOf(false) }
    var confirmClearCache by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val clearedText = stringResource(R.string.settings_cleared)

    RuleDetailScaffold(
        title = stringResource(R.string.settings_logs),
        onBack = onBack,
    ) {
        Text(
            "${stringResource(R.string.settings_retention)}：${screen.logRetentionDays}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Slider(
            value = screen.logRetentionDays.toFloat(),
            onValueChange = { value ->
                scope.launch {
                    container.settingsRepository.updateScreen {
                        it.copy(logRetentionDays = value.toInt().coerceIn(1, 90))
                    }
                }
            },
            valueRange = 1f..30f,
            steps = 28,
        )
        SettingsCard {
            ToggleRow(
                title = stringResource(R.string.settings_mask_numbers),
                checked = prefs.maskNumbers,
                onChecked = { checked ->
                    scope.launch { container.settingsRepository.setMaskNumbers(checked) }
                },
            )
        }
        TextButton(onClick = { confirmClearLogs = true }) {
            Text(stringResource(R.string.settings_clear_logs))
        }
        TextButton(onClick = { confirmClearCache = true }) {
            Text(stringResource(R.string.settings_clear_cache))
        }
    }

    if (confirmClearLogs || confirmClearCache) {
        val logs = confirmClearLogs
        AlertDialog(
            onDismissRequest = {
                confirmClearLogs = false
                confirmClearCache = false
            },
            text = {
                Text(
                    stringResource(
                        if (logs) R.string.settings_clear_logs_confirm else R.string.settings_clear_cache_confirm,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            container.callLogRepository.clearAll()
                            message = clearedText
                        }
                        confirmClearLogs = false
                        confirmClearCache = false
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmClearLogs = false
                        confirmClearCache = false
                    },
                ) { Text(stringResource(R.string.settings_cancel)) }
            },
        )
    }
    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text(stringResource(R.string.ok)) }
            },
        )
    }
}
