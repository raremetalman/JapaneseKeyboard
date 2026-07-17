package com.kazumaproject.markdownhelperkeyboard.application_identity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ForkApplicationIdentityContractTest {

    @Test
    fun gradleUsesForkApplicationIdWithoutChangingNamespace() {
        val gradle = appFile("build.gradle").readText()

        assertTrue(gradle.contains("namespace 'com.kazumaproject.markdownhelperkeyboard'"))
        assertTrue(gradle.contains("applicationId \"$FORK_APPLICATION_ID\""))
        assertTrue(gradle.contains("flavorDimensions \"edition\", \"channel\""))
        assertTrue(gradle.contains("applicationIdSuffix \".lite\""))
        assertTrue(gradle.contains("applicationIdSuffix \".fdroid\""))
        assertFalse(
            gradle.contains("applicationId \"com.kazumaproject.markdownhelperkeyboard\"")
        )
    }

    @Test
    fun activeProductVariantsHaveDistinctForkApplicationIds() {
        assertEquals(
            setOf(
                FORK_APPLICATION_ID,
                "$FORK_APPLICATION_ID.lite",
                "$FORK_APPLICATION_ID.lite.fdroid",
            ),
            activeVariantApplicationIds
        )
    }

    @Test
    fun appAndImeLabelsUseLocalizedForkBranding() {
        val manifest = appFile("src/main/AndroidManifest.xml").readText()
        val method = appFile("src/main/res/xml/method.xml").readText()
        val forkLabel = "android:label=\"@string/fork_app_name\""

        assertEquals(
            4,
            manifest.split(forkLabel).size - 1
        )
        assertTrue(method.contains("android:label=\"@string/fork_app_name\""))
        assertTrue(
            method.contains(
                "android:settingsActivity=\"com.kazumaproject.markdownhelperkeyboard." +
                    "setting_activity.MainActivity\""
            )
        )
        assertEquals(
            "Sumire Fork",
            stringResourceValue("src/main/res/values/fork_branding.xml", "fork_app_name")
        )
        assertEquals(
            "スミレ Fork",
            stringResourceValue("src/main/res/values-ja/fork_branding.xml", "fork_app_name")
        )
    }

    @Test
    fun fileProviderAuthorityFollowsApplicationId() {
        val manifest = appFile("src/main/AndroidManifest.xml").readText()
        val clipboardFragment = appFile(
            "src/main/java/com/kazumaproject/markdownhelperkeyboard/" +
                "clipboard_history/ui/ClipboardHistoryFragment.kt"
        ).readText()
        val imeService = appFile(
            "src/main/java/com/kazumaproject/markdownhelperkeyboard/ime_service/IMEService.kt"
        ).readText()

        assertTrue(manifest.contains("android:authorities=\"\${applicationId}.fileprovider\""))
        assertTrue(
            clipboardFragment.contains("\${requireContext().packageName}.fileprovider")
        )
        assertTrue(imeService.contains("\${applicationContext.packageName}.fileprovider"))
    }

    private fun stringResourceValue(path: String, name: String): String {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(appFile(path))
        val strings = document.getElementsByTagName("string")
        for (index in 0 until strings.length) {
            val item = strings.item(index)
            if (item.attributes.getNamedItem("name")?.nodeValue == name) {
                return item.textContent
            }
        }
        error("Missing string resource $name in $path")
    }

    private fun appFile(path: String): File {
        val moduleFile = File(path)
        return if (moduleFile.exists()) moduleFile else File("app/$path")
    }

    private companion object {
        const val FORK_APPLICATION_ID = "io.github.raremetalman.japanesekeyboard"

        val activeVariantApplicationIds = setOf(
            FORK_APPLICATION_ID,
            "$FORK_APPLICATION_ID.lite",
            "$FORK_APPLICATION_ID.lite.fdroid",
        )
    }
}
