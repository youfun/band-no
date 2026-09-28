package dev.bandno.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.BuildConfig
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.app.ui.formatAllowWindowsSummary
import dev.bandno.app.ui.formatBlockedPrefixesSummary
import dev.bandno.app.ui.theme.bandNoTopAppBarColors
import dev.bandno.decision.BlockAction
import dev.bandno.decision.PrivateNumberPolicy
import dev.bandno.decision.ScreenSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenWindows: () -> Unit,
    onOpenRepeat: () -> Unit,
    onOpenPrefix: () -> Unit,
    onOpenPrivate: () -> Unit,
    onOpenLogs: () -> Unit,
) {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val screen = prefs.screen
    val scope = rememberCoroutineScope()
    var confirmReject by remember { mutableStateOf(false) }

    fun update(transform: (ScreenSettings) -> ScreenSettings) {
        scope.launch { container.settingsRepository.updateScreen(transform) }
    }

    val windowsSummary = if (screen.allowWindowsEnabled) {
        formatAllowWindowsSummary(screen.allowWindows).ifBlank {
            stringResource(R.string.settings_prefix_empty)
        }
    } else {
        stringResource(R.string.settings_rule_off)
    }
    val repeatSummary = if (screen.r3Enabled) {
        stringResource(R.string.settings_repeat_summary_on, screen.r3IntervalMinutes)
    } else {
        stringResource(R.string.settings_rule_off)
    }
    val prefixSummary = formatBlockedPrefixesSummary(screen.blockedPrefixes).ifBlank {
        stringResource(R.string.settings_prefix_empty)
    }
    val privateSummary = if (screen.privateNumberPolicy == PrivateNumberPolicy.ALLOW) {
        stringResource(R.string.settings_private_allow)
    } else {
        stringResource(R.string.settings_private_follow)
    }
    val logsSummary = stringResource(
        R.string.settings_logs_summary,
        screen.logRetentionDays,
        stringResource(if (prefs.maskNumbers) R.string.settings_mask_on else R.string.settings_mask_off),
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                colors = bandNoTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SettingsCard {
                Text(
                    text = stringResource(R.string.settings_decision_order),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            SettingsSectionTitle(stringResource(R.string.settings_rules))
            SettingsCard {
                ToggleRow(
                    title = stringResource(R.string.settings_always_allow_contacts),
                    summary = stringResource(R.string.settings_always_allow_contacts_hint),
                    checked = screen.alwaysAllowContacts,
                    onChecked = { checked -> update { it.copy(alwaysAllowContacts = checked) } },
                )
                SettingsDivider()
                ChevronRow(
                    title = stringResource(R.string.settings_windows_title),
                    summary = windowsSummary,
                    onClick = onOpenWindows,
                )
                SettingsDivider()
                ChevronRow(
                    title = stringResource(R.string.settings_repeat_title),
                    summary = repeatSummary,
                    onClick = onOpenRepeat,
                )
                SettingsDivider()
                ChevronRow(
                    title = stringResource(R.string.settings_prefix_title),
                    summary = prefixSummary,
                    onClick = onOpenPrefix,
                )
            }

            SettingsSectionTitle(stringResource(R.string.settings_block))
            SettingsCard {
                RadioRow(
                    title = stringResource(R.string.settings_block_silence),
                    summary = stringResource(R.string.settings_block_silence_hint),
                    selected = screen.blockAction == BlockAction.SILENCE,
                    onSelect = { update { it.copy(blockAction = BlockAction.SILENCE) } },
                )
                SettingsDivider()
                RadioRow(
                    title = stringResource(R.string.settings_block_reject),
                    summary = stringResource(R.string.settings_block_reject_hint),
                    selected = screen.blockAction == BlockAction.REJECT,
                    onSelect = { confirmReject = true },
                )
            }

            SettingsSectionTitle(stringResource(R.string.settings_other))
            SettingsCard {
                ChevronRow(
                    title = stringResource(R.string.settings_private),
                    summary = privateSummary,
                    onClick = onOpenPrivate,
                )
                SettingsDivider()
                ChevronRow(
                    title = stringResource(R.string.settings_logs),
                    summary = logsSummary,
                    onClick = onOpenLogs,
                )
            }

            SettingsHint(stringResource(R.string.settings_privacy_body))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
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
}
