package dev.bandno.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.BuildConfig
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.app.ui.components.TimeField
import dev.bandno.app.ui.formatHm
import dev.bandno.decision.BlockAction
import dev.bandno.decision.PrivateNumberPolicy
import dev.bandno.decision.ScreenSettings
import java.time.LocalTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val screen = prefs.screen
    val scope = rememberCoroutineScope()
    var confirmReject by remember { mutableStateOf(false) }
    var confirmClearLogs by remember { mutableStateOf(false) }
    var confirmClearCache by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val clearedText = stringResource(R.string.settings_cleared)

    fun update(transform: (ScreenSettings) -> ScreenSettings) {
        scope.launch { container.settingsRepository.updateScreen(transform) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionTitle(stringResource(R.string.settings_rules))

            RuleHeader(
                title = stringResource(R.string.settings_r1_title),
                summary = stringResource(R.string.settings_r1_summary),
                enabled = screen.r1Enabled,
                onEnabled = { on -> update { it.copy(r1Enabled = on) } },
            )
            TimeField(stringResource(R.string.settings_start), screen.r1Start) { t ->
                update { it.copy(r1Start = t) }
            }
            TimeField(stringResource(R.string.settings_end), screen.r1End) { t ->
                update { it.copy(r1End = t) }
            }
            WindowHint(screen.r1Start, screen.r1End)
            ToggleRow(
                title = stringResource(R.string.settings_r1_strangers_only),
                checked = screen.r1StrangersOnly,
                onChecked = { checked -> update { it.copy(r1StrangersOnly = checked) } },
            )

            HorizontalDivider()
            RuleHeader(
                title = stringResource(R.string.settings_r2_title),
                summary = stringResource(R.string.settings_r2_summary),
                enabled = screen.r2Enabled,
                onEnabled = { on -> update { it.copy(r2Enabled = on) } },
            )
            TimeField(stringResource(R.string.settings_start), screen.r2Start) { t ->
                update { it.copy(r2Start = t) }
            }
            TimeField(stringResource(R.string.settings_end), screen.r2End) { t ->
                update { it.copy(r2End = t) }
            }
            WindowHint(screen.r2Start, screen.r2End)
            ToggleRow(
                title = stringResource(R.string.settings_r2_force),
                summary = stringResource(R.string.settings_r2_force_hint),
                checked = screen.r2ForceOverride,
                onChecked = { checked -> update { it.copy(r2ForceOverride = checked) } },
            )

            HorizontalDivider()
            RuleHeader(
                title = stringResource(R.string.settings_r3_title),
                summary = stringResource(R.string.settings_r3_summary),
                enabled = screen.r3Enabled,
                onEnabled = { on -> update { it.copy(r3Enabled = on) } },
            )
            Text(
                "${stringResource(R.string.settings_r3_interval)}：${screen.r3IntervalMinutes}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = screen.r3IntervalMinutes.toFloat(),
                onValueChange = { value ->
                    update { it.copy(r3IntervalMinutes = value.toInt().coerceIn(1, 180)) }
                },
                valueRange = 1f..60f,
                steps = 58,
            )
            ToggleRow(
                title = stringResource(R.string.settings_r3_require_blocked),
                summary = stringResource(R.string.settings_r3_require_blocked_hint),
                checked = screen.r3RequireFirstBlocked,
                onChecked = { checked -> update { it.copy(r3RequireFirstBlocked = checked) } },
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_contacts))
            ToggleRow(
                title = stringResource(R.string.settings_always_allow_contacts),
                summary = stringResource(R.string.settings_always_allow_contacts_hint),
                checked = screen.alwaysAllowContacts,
                onChecked = { checked -> update { it.copy(alwaysAllowContacts = checked) } },
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_block))
            RadioRow(
                title = stringResource(R.string.settings_block_silence),
                summary = stringResource(R.string.settings_block_silence_hint),
                selected = screen.blockAction == BlockAction.SILENCE,
                onSelect = { update { it.copy(blockAction = BlockAction.SILENCE) } },
            )
            RadioRow(
                title = stringResource(R.string.settings_block_reject),
                summary = stringResource(R.string.settings_block_reject_hint),
                selected = screen.blockAction == BlockAction.REJECT,
                onSelect = { confirmReject = true },
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_private))
            Text(stringResource(R.string.settings_private_hint), style = MaterialTheme.typography.bodySmall)
            RadioRow(
                title = stringResource(R.string.settings_private_allow),
                selected = screen.privateNumberPolicy == PrivateNumberPolicy.ALLOW,
                onSelect = { update { it.copy(privateNumberPolicy = PrivateNumberPolicy.ALLOW) } },
            )
            RadioRow(
                title = stringResource(R.string.settings_private_follow),
                selected = screen.privateNumberPolicy == PrivateNumberPolicy.FOLLOW_RULES,
                onSelect = { update { it.copy(privateNumberPolicy = PrivateNumberPolicy.FOLLOW_RULES) } },
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_logs))
            Text(
                "${stringResource(R.string.settings_retention)}：${screen.logRetentionDays}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = screen.logRetentionDays.toFloat(),
                onValueChange = { value ->
                    update { it.copy(logRetentionDays = value.toInt().coerceIn(1, 90)) }
                },
                valueRange = 1f..30f,
                steps = 28,
            )
            ToggleRow(
                title = stringResource(R.string.settings_mask_numbers),
                checked = prefs.maskNumbers,
                onChecked = { checked ->
                    scope.launch { container.settingsRepository.setMaskNumbers(checked) }
                },
            )
            TextButton(onClick = { confirmClearLogs = true }) {
                Text(stringResource(R.string.settings_clear_logs))
            }
            TextButton(onClick = { confirmClearCache = true }) {
                Text(stringResource(R.string.settings_clear_cache))
            }

            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_privacy))
            Text(stringResource(R.string.settings_privacy_body), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
        }
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }

    if (confirmReject) {
        AlertDialog(
            onDismissRequest = { confirmReject = false },
            title = { Text(stringResource(R.string.settings_block_reject_confirm_title)) },
            text = { Text(stringResource(R.string.settings_block_reject_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        update { it.copy(blockAction = BlockAction.REJECT) }
                        confirmReject = false
                    },
                ) { Text(stringResource(R.string.settings_block_reject_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReject = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            },
        )
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

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun RuleHeader(
    title: String,
    summary: String,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
) {
    Column {
        ToggleRow(title = title, checked = enabled, onChecked = onEnabled)
        Text(summary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WindowHint(start: LocalTime, end: LocalTime) {
    val overnight = start > end
    Text(
        if (overnight) {
            stringResource(R.string.settings_overnight_hint, start.formatHm(), end.formatHm())
        } else {
            stringResource(R.string.settings_same_day_hint, start.formatHm(), end.formatHm())
        },
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    summary: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun RadioRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    summary: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.padding(start = 8.dp, top = 4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
