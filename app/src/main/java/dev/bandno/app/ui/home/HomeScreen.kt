package dev.bandno.app.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.data.CallAttemptEntity
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.app.ui.theme.bandNoTopAppBarColors
import dev.bandno.app.ui.actionLabel
import dev.bandno.app.ui.displayNumber
import dev.bandno.app.ui.formatLocal
import dev.bandno.app.ui.ruleLabel
import dev.bandno.decision.DecisionAction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSeeLogs: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val todayStart = remember {
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
    }
    val todayLogs by container.callLogRepository.observeSince(todayStart).collectAsStateWithLifecycle(emptyList())
    val recent = todayLogs.take(8)

    var roleHeld by remember { mutableStateOf(container.roleStatus.isCallScreeningHeld()) }
    var contactsGranted by remember { mutableStateOf(container.contactDirectory.hasPermission()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                roleHeld = container.roleStatus.isCallScreeningHeld()
                contactsGranted = container.contactDirectory.hasPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        roleHeld = container.roleStatus.isCallScreeningHeld()
    }
    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        contactsGranted = granted
    }

    val allowed = todayLogs.count { it.action == DecisionAction.ALLOW.name }
    val blocked = todayLogs.size - allowed

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = bandNoTopAppBarColors(),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                StatusCard(
                    active = roleHeld,
                    onRequestRole = {
                        val intent = container.roleStatus.requestIntent()
                        if (intent != null) {
                            roleLauncher.launch(intent)
                        }
                    },
                )
            }
            item {
                ContactsCard(
                    granted = contactsGranted,
                    onRequest = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) },
                )
            }
            item {
                Text(stringResource(R.string.today_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.today_allowed, allowed))
                    Text(stringResource(R.string.today_blocked, blocked))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.recent_title), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onSeeLogs) {
                        Text(stringResource(R.string.see_all))
                    }
                }
            }
            if (recent.isEmpty()) {
                item { Text(stringResource(R.string.recent_empty), style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(recent, key = { it.id }) { row ->
                    LogRow(row, mask = prefs.maskNumbers)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatusCard(active: Boolean, onRequestRole: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (active) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
            contentColor = if (active) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(if (active) R.string.role_active else R.string.role_inactive),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(if (active) R.string.role_held_hint else R.string.role_inactive_hint),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!active) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRequestRole) {
                    Text(stringResource(R.string.role_request))
                }
            }
        }
    }
}

@Composable
private fun ContactsCard(granted: Boolean, onRequest: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(if (granted) R.string.contacts_granted else R.string.contacts_missing),
                style = MaterialTheme.typography.titleMedium,
            )
            if (!granted) {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.contacts_missing_hint), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRequest) {
                    Text(stringResource(R.string.contacts_request))
                }
            }
        }
    }
}

@Composable
internal fun LogRow(row: CallAttemptEntity, mask: Boolean) {
    val context = LocalContext.current
    val number = row.displayNumber(mask).ifBlank { stringResource(R.string.logs_unknown_number) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(number, style = MaterialTheme.typography.bodyLarge)
            Text(actionLabel(context, row.action), style = MaterialTheme.typography.labelLarge)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val rule = buildString {
                append(ruleLabel(context, row.ruleHit))
                if (row.isContact) {
                    append(" · ")
                    append(context.getString(R.string.logs_contact_badge))
                }
            }
            Text(rule, style = MaterialTheme.typography.bodySmall)
            Text(
                Instant.ofEpochMilli(row.timestampEpochMillis).formatLocal(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
