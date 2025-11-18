package com.arcadia.trivora

import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceIdManager {
    private const val PREFS_DEVICE_ID = "device_id"
    private const val PREFS_NAME = "device_prefs"

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(PREFS_DEVICE_ID, null)

        if (deviceId == null) {
            // Try to get Android ID, or generate UUID
            deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: UUID.randomUUID().toString()

            prefs.edit().putString(PREFS_DEVICE_ID, deviceId).apply()
        }

        return deviceId
    }
}