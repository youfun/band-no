package dev.bandno.app

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent

class RoleStatus(
    private val context: Context,
) {
    private val roleManager: RoleManager?
        get() = context.getSystemService(RoleManager::class.java)

    fun isCallScreeningHeld(): Boolean =
        roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true

    fun requestIntent(): Intent? =
        roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
}
