package dev.bandno.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dev.bandno.decision.BlockAction
import dev.bandno.decision.PrivateNumberPolicy
import dev.bandno.decision.ScreenSettings
import java.time.LocalTime
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
        val next = transform(cached())
        dataStore.edit { prefs -> writeScreen(prefs, next) }
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
        val R1_STRANGERS_ONLY = booleanPreferencesKey("r1_strangers_only")
        val R2_ENABLED = booleanPreferencesKey("r2_enabled")
        val R2_START_MIN = intPreferencesKey("r2_start_min")
        val R2_END_MIN = intPreferencesKey("r2_end_min")
        val R2_FORCE = booleanPreferencesKey("r2_force")
        val R3_ENABLED = booleanPreferencesKey("r3_enabled")
        val R3_INTERVAL = intPreferencesKey("r3_interval")
        val R3_REQUIRE_BLOCKED = booleanPreferencesKey("r3_require_blocked")
        val ALWAYS_ALLOW_CONTACTS = booleanPreferencesKey("always_allow_contacts")
        val BLOCK_ACTION = intPreferencesKey("block_action")
        val PRIVATE_POLICY = intPreferencesKey("private_policy")
        val LOG_RETENTION_DAYS = intPreferencesKey("log_retention_days")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val MASK_NUMBERS = booleanPreferencesKey("mask_numbers")

        fun Preferences.toAppPreferences(): AppPreferences {
            val defaults = ScreenSettings.Default
            val screen = ScreenSettings(
                r1Enabled = this[R1_ENABLED] ?: defaults.r1Enabled,
                r1Start = minutesToTime(this[R1_START_MIN], defaults.r1Start),
                r1End = minutesToTime(this[R1_END_MIN], defaults.r1End),
                r1StrangersOnly = this[R1_STRANGERS_ONLY] ?: defaults.r1StrangersOnly,
                r2Enabled = this[R2_ENABLED] ?: defaults.r2Enabled,
                r2Start = minutesToTime(this[R2_START_MIN], defaults.r2Start),
                r2End = minutesToTime(this[R2_END_MIN], defaults.r2End),
                r2ForceOverride = this[R2_FORCE] ?: defaults.r2ForceOverride,
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
            )
            return AppPreferences(
                screen = screen,
                onboardingComplete = this[ONBOARDING_COMPLETE] ?: false,
                maskNumbers = this[MASK_NUMBERS] ?: true,
            )
        }

        fun writeScreen(prefs: MutablePreferences, settings: ScreenSettings) {
            prefs[R1_ENABLED] = settings.r1Enabled
            prefs[R1_START_MIN] = settings.r1Start.toMinuteOfDay()
            prefs[R1_END_MIN] = settings.r1End.toMinuteOfDay()
            prefs[R1_STRANGERS_ONLY] = settings.r1StrangersOnly
            prefs[R2_ENABLED] = settings.r2Enabled
            prefs[R2_START_MIN] = settings.r2Start.toMinuteOfDay()
            prefs[R2_END_MIN] = settings.r2End.toMinuteOfDay()
            prefs[R2_FORCE] = settings.r2ForceOverride
            prefs[R3_ENABLED] = settings.r3Enabled
            prefs[R3_INTERVAL] = settings.r3IntervalMinutes
            prefs[R3_REQUIRE_BLOCKED] = settings.r3RequireFirstBlocked
            prefs[ALWAYS_ALLOW_CONTACTS] = settings.alwaysAllowContacts
            prefs[BLOCK_ACTION] = if (settings.blockAction == BlockAction.REJECT) 1 else 0
            prefs[PRIVATE_POLICY] = if (settings.privateNumberPolicy == PrivateNumberPolicy.FOLLOW_RULES) 1 else 0
            prefs[LOG_RETENTION_DAYS] = settings.logRetentionDays
        }

        fun minutesToTime(minutes: Int?, fallback: LocalTime): LocalTime {
            if (minutes == null) return fallback
            val safe = minutes.coerceIn(0, 24 * 60 - 1)
            return LocalTime.of(safe / 60, safe % 60)
        }

        fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute
    }
}
