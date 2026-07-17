package com.kazumaproject.markdownhelperkeyboard.setting_activity.ui.setting

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.kazumaproject.markdownhelperkeyboard.R
import com.kazumaproject.markdownhelperkeyboard.setting_activity.AppPreference
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PhysicalKeyboardFloatingDockPreferenceTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        AppPreference.init(context)
    }

    @Test
    fun floatingDockVisibilityDefaultsToEnabled() {
        assertTrue(AppPreference.physical_keyboard_floating_dock_visibility_preference)
    }

    @Test
    fun floatingDockVisibilityUsesDefaultSharedPreferencesKey() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.edit()
            .putBoolean(PREFERENCE_KEY, false)
            .commit()

        assertFalse(AppPreference.physical_keyboard_floating_dock_visibility_preference)

        AppPreference.physical_keyboard_floating_dock_visibility_preference = true

        assertTrue(preferences.getBoolean(PREFERENCE_KEY, false))
    }

    @Test
    fun floatingDockVisibilityLivesInHardwareKeyboardSettingsAndIsSearchable() {
        assertTrue(PREFERENCE_KEY in preferenceKeys(R.xml.pref_hardware_keyboard))
        assertTrue(preferenceDefaultValue(R.xml.pref_hardware_keyboard, PREFERENCE_KEY))

        val newHomeKeys = SettingSearchIndex.searchable(context, SettingSearchScope.NEW_HOME)
            .map { it.key }
            .toSet()
        val legacyKeys = SettingSearchIndex.searchable(context, SettingSearchScope.LEGACY_TABS)
            .map { it.key }
            .toSet()

        assertTrue(PREFERENCE_KEY in newHomeKeys)
        assertTrue(PREFERENCE_KEY in legacyKeys)
    }

    private fun preferenceKeys(xmlRes: Int): Set<String> {
        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val parser = context.resources.getXml(xmlRes)
        return parser.use {
            buildSet {
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG) {
                        parser.getAttributeValue(androidNamespace, "key")?.let(::add)
                    }
                    parser.next()
                }
            }
        }
    }

    private fun preferenceDefaultValue(xmlRes: Int, preferenceKey: String): Boolean {
        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val parser = context.resources.getXml(xmlRes)
        return parser.use {
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (
                    parser.eventType == XmlPullParser.START_TAG &&
                    parser.getAttributeValue(androidNamespace, "key") == preferenceKey
                ) {
                    return@use parser.getAttributeBooleanValue(
                        androidNamespace,
                        "defaultValue",
                        false
                    )
                }
                parser.next()
            }
            false
        }
    }

    private companion object {
        const val PREFERENCE_KEY =
            "physical_keyboard_floating_dock_visibility_preference"
    }
}
