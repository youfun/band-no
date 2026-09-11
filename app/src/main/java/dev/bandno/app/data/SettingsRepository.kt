package dev.bandno.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.bandno.decision.AllowWindowFormat
import dev.bandno.decision.AllowWindowsMigration
import dev.bandno.decision.BlockAction
import dev.bandno.decision.BlockedPrefixes
import dev.bandno.decision.PrivateNumberPolicy
import dev.bandno.decision.ScreenSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AppPreferences(
    val screen: ScreenSettings = ScreenSettings.Default,
    val onboardingComplete: Boolean = false,
    val maskNumbers: Boolean = true,
)

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope,
) {
    val preferences: StateFlow<AppPreferences> = dataStore.data
        .map { it.toAppPreferences() }
        .stateIn(scope, SharingStarted.Eagerly, AppPreferences())

    fun cached(): ScreenSettings = preferences.value.screen

    suspend fun updateScreen(transform: (ScreenSettings) -> ScreenSettings) {
        dataStore.edit { prefs ->
            val current = prefs.toAppPreferences().screen
            writeScreen(prefs, transform(current))
        }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { it[ONBOARDING_COMPLETE] = true }
    }

    suspend fun setMaskNumbers(value: Boolean) {
        dataStore.edit { it[MASK_NUMBERS] = value }
    }

    private companion object {
        val R1_ENABLED = booleanPreferencesKey("r1_enabled")
        val R1_START_MIN = intPreferencesKey("r1_start_min")
        val R1_END_MIN = intPreferencesKey("r1_end_min")
        val R2_ENABLED = booleanPreferencesKey("r2_enabled")
        val R2_START_MIN = intPreferencesKey("r2_start_min")
        val R2_END_MIN = intPreferencesKey("r2_end_min")
        val R3_ENABLED = booleanPreferencesKey("r3_enabled")
        val R3_INTERVAL = intPreferencesKey("r3_interval")
        val R3_REQUIRE_BLOCKED = booleanPreferencesKey("r3_require_blocked")
        val ALWAYS_ALLOW_CONTACTS = booleanPreferencesKey("always_allow_contacts")
        val BLOCK_ACTION = intPreferencesKey("block_action")
        val PRIVATE_POLICY = intPreferencesKey("private_policy")
        val LOG_RETENTION_DAYS = intPreferencesKey("log_retention_days")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val MASK_NUMBERS = booleanPreferencesKey("mask_numbers")
        val ALLOW_WINDOWS = stringPreferencesKey("allow_windows")
        val ALLOW_WINDOWS_ENABLED = booleanPreferencesKey("allow_windows_enabled")
        val BLOCKED_PREFIXES = stringPreferencesKey("blocked_prefixes")

        fun Preferences.toAppPreferences(): AppPreferences {
            val defaults = ScreenSettings.Default
            val windows = AllowWindowsMigration.resolve(
                encodedWindows = this[ALLOW_WINDOWS],
                windowsEnabled = this[ALLOW_WINDOWS_ENABLED],
                hasLegacyKeys = hasLegacyWindowKeys(),
                r1Enabled = this[R1_ENABLED],
                r1StartMin = this[R1_START_MIN],
                r1EndMin = this[R1_END_MIN],
                r2Enabled = this[R2_ENABLED],
                r2StartMin = this[R2_START_MIN],
                r2EndMin = this[R2_END_MIN],
            )
            val screen = ScreenSettings(
                allowWindowsEnabled = windows.enabled,
                allowWindows = windows.windows,
                r3Enabled = this[R3_ENABLED] ?: defaults.r3Enabled,
                r3IntervalMinutes = this[R3_INTERVAL] ?: defaults.r3IntervalMinutes,
                r3RequireFirstBlocked = this[R3_REQUIRE_BLOCKED] ?: defaults.r3RequireFirstBlocked,
                alwaysAllowContacts = this[ALWAYS_ALLOW_CONTACTS] ?: defaults.alwaysAllowContacts,
                blockAction = if ((this[BLOCK_ACTION] ?: 0) == 1) BlockAction.REJECT else BlockAction.SILENCE,
                privateNumberPolicy = if ((this[PRIVATE_POLICY] ?: 0) == 1) {
                    PrivateNumberPolicy.FOLLOW_RULES
                } else {
                    PrivateNumberPolicy.ALLOW
                },
                logRetentionDays = this[LOG_RETENTION_DAYS] ?: defaults.logRetentionDays,
                blockedPrefixes = BlockedPrefixes.decode(this[BLOCKED_PREFIXES]),
            )
            return AppPreferences(
                screen = screen,
                onboardingComplete = this[ONBOARDING_COMPLETE] ?: false,
                maskNumbers = this[MASK_NUMBERS] ?: true,
            )
        }

        fun Preferences.hasLegacyWindowKeys(): Boolean =
            this[R1_ENABLED] != null ||
                this[R1_START_MIN] != null ||
                this[R1_END_MIN] != null ||
                this[R2_ENABLED] != null ||
                this[R2_START_MIN] != null ||
                this[R2_END_MIN] != null

        fun writeScreen(prefs: MutablePreferences, settings: ScreenSettings) {
            prefs[ALLOW_WINDOWS_ENABLED] = settings.allowWindowsEnabled
            prefs[ALLOW_WINDOWS] = AllowWindowFormat.encode(settings.allowWindows)
            prefs[BLOCKED_PREFIXES] = BlockedPrefixes.encode(settings.blockedPrefixes)
            prefs[R3_ENABLED] = settings.r3Enabled
            prefs[R3_INTERVAL] = settings.r3IntervalMinutes
            prefs[R3_REQUIRE_BLOCKED] = settings.r3RequireFirstBlocked
            prefs[ALWAYS_ALLOW_CONTACTS] = settings.alwaysAllowContacts
            prefs[BLOCK_ACTION] = if (settings.blockAction == BlockAction.REJECT) 1 else 0
            prefs[PRIVATE_POLICY] = if (settings.privateNumberPolicy == PrivateNumberPolicy.FOLLOW_RULES) 1 else 0
            prefs[LOG_RETENTION_DAYS] = settings.logRetentionDays
        }
    }
}
