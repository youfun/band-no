package dev.bandno.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.bandno.app.R
import dev.bandno.app.ui.LocalAppContainer
import kotlinx.coroutines.launch

private data class Page(val title: Int, val body: Int)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        Page(R.string.onboarding_title_1, R.string.onboarding_body_1),
        Page(R.string.onboarding_title_2, R.string.onboarding_body_2),
        Page(R.string.onboarding_title_3, R.string.onboarding_body_3),
        Page(R.string.onboarding_title_4, R.string.onboarding_body_4),
    )
    var index by remember { mutableIntStateOf(0) }
    val last = index == pages.lastIndex
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val page = pages[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(32.dp))
            Text(stringResource(page.title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge)
        }
        Column {
            Button(
                onClick = {
                    if (last) {
                        scope.launch {
                            container.settingsRepository.setOnboardingComplete()
                            onFinished()
                        }
                    } else {
                        index += 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (last) R.string.onboarding_done else R.string.onboarding_next))
            }
            if (!last) {
                TextButton(
                    onClick = {
                        scope.launch {
                            container.settingsRepository.setOnboardingComplete()
                            onFinished()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }
    }
}
