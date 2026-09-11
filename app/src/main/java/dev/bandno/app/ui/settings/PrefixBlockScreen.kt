package dev.bandno.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.decision.BlockedPrefixes
import kotlinx.coroutines.launch

private val SuggestedPrefixes = listOf("170", "171", "162", "165", "167")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrefixBlockScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val prefixes = prefs.screen.blockedPrefixes
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val errorText = stringResource(R.string.settings_prefix_error)
    val fullText = stringResource(R.string.settings_prefix_full)

    fun persist(next: List<String>) {
        scope.launch {
            container.settingsRepository.updateScreen { it.copy(blockedPrefixes = next) }
        }
    }

    fun tryAdd(raw: String) {
        val sanitized = BlockedPrefixes.sanitize(raw)
        if (sanitized == null) {
            error = errorText
            return
        }
        if (prefixes.size >= BlockedPrefixes.MAX_COUNT && sanitized !in prefixes) {
            error = fullText
            return
        }
        error = null
        persist(BlockedPrefixes.add(prefixes, sanitized))
        input = ""
    }

    RuleDetailScaffold(
        title = stringResource(R.string.settings_prefix_title),
        onBack = onBack,
    ) {
        SettingsHint(stringResource(R.string.settings_prefix_hint))

        SettingsSectionTitle(stringResource(R.string.settings_prefix_added))
        if (prefixes.isEmpty()) {
            Text(
                stringResource(R.string.settings_prefix_none),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp),
            ) {
                prefixes.forEach { prefix ->
                    InputChip(
                        selected = true,
                        onClick = { persist(BlockedPrefixes.remove(prefixes, prefix)) },
                        label = { Text(prefix) },
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.settings_prefix_remove, prefix),
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { raw ->
                    input = raw.filter { it.isDigit() }.take(BlockedPrefixes.MAX_LENGTH)
                    error = null
                },
                placeholder = { Text(stringResource(R.string.settings_prefix_placeholder)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { tryAdd(input) }),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { tryAdd(input) }) {
                Text(stringResource(R.string.settings_prefix_add))
            }
        }
        Text(
            text = error.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        )

        SettingsSectionTitle(stringResource(R.string.settings_prefix_suggest))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SuggestedPrefixes.forEach { prefix ->
                val selected = prefix in prefixes
                FilterChip(
                    selected = selected,
                    onClick = {
                        error = null
                        persist(
                            if (selected) {
                                BlockedPrefixes.remove(prefixes, prefix)
                            } else {
                                BlockedPrefixes.add(prefixes, prefix)
                            },
                        )
                    },
                    label = { Text(if (selected) "$prefix ✓" else prefix) },
                )
            }
        }
        Text(
            stringResource(R.string.settings_prefix_suggest_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
