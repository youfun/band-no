package dev.bandno.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.decision.PrivateNumberPolicy
import kotlinx.coroutines.launch

@Composable
fun PrivateNumberScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    RuleDetailScaffold(
        title = stringResource(R.string.settings_private),
        onBack = onBack,
    ) {
        SettingsHint(stringResource(R.string.settings_private_hint))
        SettingsCard {
            RadioRow(
                title = stringResource(R.string.settings_private_allow),
                selected = prefs.screen.privateNumberPolicy == PrivateNumberPolicy.ALLOW,
                onSelect = {
                    scope.launch {
                        container.settingsRepository.updateScreen {
                            it.copy(privateNumberPolicy = PrivateNumberPolicy.ALLOW)
                        }
                    }
                },
            )
            SettingsDivider()
            RadioRow(
                title = stringResource(R.string.settings_private_follow),
                selected = prefs.screen.privateNumberPolicy == PrivateNumberPolicy.FOLLOW_RULES,
                onSelect = {
                    scope.launch {
                        container.settingsRepository.updateScreen {
                            it.copy(privateNumberPolicy = PrivateNumberPolicy.FOLLOW_RULES)
                        }
                    }
                },
            )
        }
    }
}
