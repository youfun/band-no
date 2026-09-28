package dev.bandno.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dev.bandno.app.data.AppDatabase
import dev.bandno.app.data.CallLogRepository
import dev.bandno.app.data.SettingsRepository
import dev.bandno.app.screening.ContactDirectory
import dev.bandno.app.screening.ScreeningController
import dev.bandno.decision.CallingCodes
import dev.bandno.decision.RegionDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

private val Context.settingsStore by preferencesDataStore(name = "band_no_settings")

class AppContainer(
    context: Context,
) {
    private val appContext = context.applicationContext
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "band_no.db",
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    val settingsRepository = SettingsRepository(appContext.settingsStore, applicationScope)
    val callLogRepository = CallLogRepository(database.callAttemptDao())
    val contactDirectory = ContactDirectory(appContext)
    val roleStatus = RoleStatus(appContext)
    val regionDirectory = RegionDirectory.load(
        mobileTable = RegionDirectory::class.java.getResourceAsStream("/region-cn-mobile.bin")!!.use { it.readBytes() },
        landlineTable = RegionDirectory::class.java.getResourceAsStream("/region-cn-landline.bin")!!.use { it.readBytes() },
        countries = CallingCodes.displayNames,
    )
    val screeningController = ScreeningController(
        settingsRepository = settingsRepository,
        callLogRepository = callLogRepository,
        contacts = contactDirectory,
        regions = regionDirectory,
    )

    init {
        applicationScope.launch {
            settingsRepository.preferences.collect { prefs ->
                callLogRepository.purgeOlderThan(
                    Instant.now().minus(prefs.screen.logRetentionDays.toLong(), ChronoUnit.DAYS),
                )
            }
        }
    }
}
