package dev.bandno.app.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import android.util.Log
import dev.bandno.app.data.CallLogRepository
import dev.bandno.app.data.SettingsRepository
import dev.bandno.decision.CallScreener
import dev.bandno.decision.DecisionAction
import dev.bandno.decision.IncomingCall
import dev.bandno.decision.NumberNormalizer
import dev.bandno.decision.ScreenDecision
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ScreeningController(
    private val settingsRepository: SettingsRepository,
    private val callLogRepository: CallLogRepository,
    private val contacts: ContactDirectory,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val clock: () -> Instant = Instant::now,
) {
    fun respond(details: Call.Details): CallScreeningService.CallResponse {
        if (details.callDirection == Call.Details.DIRECTION_OUTGOING) {
            return CallScreeningService.CallResponse.Builder().build()
        }
        // CallScreeningService delivers onScreenCall on the main looper.
        // Room forbids main-thread queries; hop to IO then respondToCall.
        return runBlocking {
            withContext(Dispatchers.IO) { decideAndRecord(details) }
        }
    }

    private fun decideAndRecord(details: Call.Details): CallScreeningService.CallResponse {
        val rawNumber = details.handle?.schemeSpecificPart
        val isPrivate = isPrivateOrUnknown(rawNumber, details.handlePresentation)
        val settings = settingsRepository.cached()
        val now = clock()
        val normalized = NumberNormalizer.normalize(rawNumber)
        val isContact = !isPrivate && !rawNumber.isNullOrBlank() && contacts.isContact(rawNumber)
        val priors = if (normalized != null) {
            callLogRepository.priorsSince(
                normalized,
                now.minusSeconds(settings.r3IntervalMinutes * 60L),
            )
        } else {
            emptyList()
        }

        val decision = CallScreener.decide(
            IncomingCall(
                now = now,
                zoneId = zoneId,
                isPrivateOrUnknown = isPrivate,
                isContact = isContact,
                priorAttempts = priors,
            ),
            settings,
        )

        try {
            callLogRepository.recordSync(
                normalizedNumber = normalized.orEmpty(),
                displayNumber = rawNumber.orEmpty(),
                timestamp = now,
                decision = decision,
                isContact = isContact,
            )
        } catch (t: Throwable) {
            Log.e(TAG, "failed to persist call attempt", t)
        }

        return toResponse(decision)
    }

    private fun isPrivateOrUnknown(rawNumber: String?, presentation: Int): Boolean {
        if (rawNumber.isNullOrBlank()) return true
        return presentation == TelecomManager.PRESENTATION_RESTRICTED ||
            presentation == TelecomManager.PRESENTATION_UNKNOWN ||
            presentation == TelecomManager.PRESENTATION_PAYPHONE
    }

    private fun toResponse(decision: ScreenDecision): CallScreeningService.CallResponse {
        val builder = CallScreeningService.CallResponse.Builder()
        return when (decision.action) {
            DecisionAction.ALLOW -> builder.build()
            DecisionAction.SILENCE -> builder
                .setDisallowCall(true)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            DecisionAction.REJECT -> builder
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }
    }

    private companion object {
        const val TAG = "BandNoScreen"
    }
}
