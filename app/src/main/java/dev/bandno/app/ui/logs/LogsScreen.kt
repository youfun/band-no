package dev.bandno.app.ui.logs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import dev.bandno.app.ui.home.LogRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen() {
    val container = LocalAppContainer.current
    val prefs by container.settingsRepository.preferences.collectAsStateWithLifecycle()
    val rows by container.callLogRepository.observeRecent().collectAsStateWithLifecycle(emptyList())

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.logs_title)) }) },
    ) { padding ->
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.logs_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            ) {
                items(rows, key = { it.id }) { row ->
                    LogRow(row, mask = prefs.maskNumbers)
                }
            }
        }
    }
}
