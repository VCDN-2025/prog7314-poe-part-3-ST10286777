
package com.arcadia.trivora

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context, languageCode: String): Context {
        persistLanguage(context, languageCode)
        return updateResources(context, languageCode)
    }

    private fun persistLanguage(context: Context, languageCode: String) {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        preferences.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    fun getPersistedLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            configuration.setLayoutDirection(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        return context
    }

    fun getCurrentLanguageName(context: Context): String {
        val languageCode = getPersistedLanguage(context)
        return when (languageCode) {
            "en" -> context.getString(R.string.english)
            "af" -> context.getString(R.string.afrikaans)
            else -> "English"
        }
    }
}