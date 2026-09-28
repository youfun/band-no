package dev.bandno.app.ui.oem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.bandno.app.R
import dev.bandno.app.ui.theme.bandNoTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OemGuideScreen() {
    val sections = listOf(
        R.string.oem_generic_title to R.string.oem_generic_body,
        R.string.oem_pixel_title to R.string.oem_pixel_body,
        R.string.oem_xiaomi_title to R.string.oem_xiaomi_body,
        R.string.oem_huawei_title to R.string.oem_huawei_body,
        R.string.oem_oppo_title to R.string.oem_oppo_body,
        R.string.oem_vivo_title to R.string.oem_vivo_body,
    )
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.oem_title)) },
                colors = bandNoTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.oem_intro), style = MaterialTheme.typography.bodyLarge)
            sections.forEach { (title, body) ->
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
