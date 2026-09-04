package dev.bandno.app.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import dev.bandno.app.BandNoApplication

class BandNoCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val response = try {
            (application as BandNoApplication).container.screeningController.respond(callDetails)
        } catch (t: Throwable) {
            Log.e(TAG, "screening failed; allowing call", t)
            CallResponse.Builder().build()
        }
        respondToCall(callDetails, response)
    }

    private companion object {
        const val TAG = "BandNoScreen"
    }
}
