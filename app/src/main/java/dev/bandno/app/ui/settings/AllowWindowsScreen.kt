package dev.bandno.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.app.ui.components.BoxedTimeField
import dev.bandno.app.ui.formatHm
import dev.bandno.decision.AllowWindow
import dev.bandno.decision.AllowWindowFormat
import java.time.LocalTime
import kotlinx.coroutines.launch

@Composable
fun AllowWindowsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val screen = prefs.screen
    val scope = rememberCoroutineScope()

    fun updateWindows(windows: List<AllowWindow>) {
        scope.launch {
            container.settingsRepository.updateScreen { it.copy(allowWindows = windows) }
        }
    }

    RuleDetailScaffold(
        title = stringResource(R.string.settings_windows_title),
        onBack = onBack,
    ) {
        SettingsCard {
            ToggleRow(
                title = stringResource(R.string.settings_enabled),
                summary = stringResource(R.string.settings_windows_enable_hint),
                checked = screen.allowWindowsEnabled,
                onChecked = { on ->
                    scope.launch {
                        container.settingsRepository.updateScreen { it.copy(allowWindowsEnabled = on) }
                    }
                },
            )
        }
        SettingsHint(stringResource(R.string.settings_windows_hint))

        screen.allowWindows.forEachIndexed { index, window ->
            WindowEditorCard(
                window = window,
                canDelete = screen.allowWindows.size > 1,
                onNote = { note ->
                    updateWindows(
                        screen.allowWindows.toMutableList().also {
                            it[index] = window.copy(note = AllowWindowFormat.sanitizeNote(note))
                        },
                    )
                },
                onStart = { time ->
                    updateWindows(
                        screen.allowWindows.toMutableList().also {
                            it[index] = window.copy(start = time)
                        },
                    )
                },
                onEnd = { time ->
                    updateWindows(
                        screen.allowWindows.toMutableList().also {
                            it[index] = window.copy(end = time)
                        },
                    )
                },
                onDelete = {
                    if (screen.allowWindows.size > 1) {
                        updateWindows(screen.allowWindows.filterIndexed { i, _ -> i != index })
                    }
                },
            )
        }

        if (screen.allowWindows.size < AllowWindowFormat.MAX_WINDOWS) {
            OutlinedButton(
                onClick = {
                    updateWindows(
                        screen.allowWindows + AllowWindow(
                            start = LocalTime.of(12, 0),
                            end = LocalTime.of(13, 0),
                            note = "",
                        ),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 16.dp),
            ) {
                Text(stringResource(R.string.settings_windows_add))
            }
        }
    }
}

@Composable
private fun WindowEditorCard(
    window: AllowWindow,
    canDelete: Boolean,
    onNote: (String) -> Unit,
    onStart: (LocalTime) -> Unit,
    onEnd: (LocalTime) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val placeholder = stringResource(R.string.settings_windows_note_hint)
                BasicTextField(
                    value = window.note,
                    onValueChange = onNote,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (window.note.isEmpty()) {
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    },
                )
                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.settings_windows_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                BoxedTimeField(
                    label = stringResource(R.string.settings_start),
                    value = window.start,
                    onChange = onStart,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "→",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                BoxedTimeField(
                    label = stringResource(R.string.settings_end),
                    value = window.end,
                    onChange = onEnd,
                    modifier = Modifier.weight(1f),
                )
            }
            val overnight = window.start > window.end
            Text(
                if (overnight) {
                    stringResource(
                        R.string.settings_overnight_hint,
                        window.start.formatHm(),
                        window.end.formatHm(),
                    )
                } else {
                    stringResource(
                        R.string.settings_same_day_hint,
                        window.start.formatHm(),
                        window.end.formatHm(),
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
