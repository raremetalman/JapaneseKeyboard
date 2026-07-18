package com.kazumaproject.markdownhelperkeyboard.physical_keyboard.windows

import android.view.KeyEvent
import com.kazumaproject.markdownhelperkeyboard.physical_keyboard.shortcut.database.PhysicalKeyboardShortcutItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyPhysicalShortcutCompatibilityPolicyTest {
    @Test
    fun seededApplicationShortcuts_areForwardedToTheApplication() {
        val seeded = listOf(
            PhysicalKeyboardShortcutItem(
                context = "any",
                keyCode = KeyEvent.KEYCODE_C,
                ctrl = true,
                actionId = "copy",
                sortOrder = 0
            ),
            PhysicalKeyboardShortcutItem(
                context = "any",
                keyCode = KeyEvent.KEYCODE_V,
                ctrl = true,
                actionId = "paste",
                sortOrder = 1
            ),
            PhysicalKeyboardShortcutItem(
                context = "any",
                keyCode = KeyEvent.KEYCODE_X,
                ctrl = true,
                actionId = "cut",
                sortOrder = 2
            ),
            PhysicalKeyboardShortcutItem(
                context = "any",
                keyCode = KeyEvent.KEYCODE_A,
                ctrl = true,
                actionId = "select_all",
                sortOrder = 3
            )
        )
        seeded.forEach {
            assertTrue(
                LegacyPhysicalShortcutCompatibilityPolicy.shouldDeferToWindowsMapping(it)
            )
        }
    }

    @Test
    fun seededMuhenkan_doesNotOverrideWindowsCharacterCycle() {
        val shortcut = PhysicalKeyboardShortcutItem(
            context = "composition",
            keyCode = KeyEvent.KEYCODE_MUHENKAN,
            actionId = "switch_to_english",
            sortOrder = 13
        )
        assertTrue(
            LegacyPhysicalShortcutCompatibilityPolicy.shouldDeferToWindowsMapping(shortcut)
        )
    }

    @Test
    fun customizedShortcut_stillHasPriority() {
        val changedAction = PhysicalKeyboardShortcutItem(
            context = "composition",
            keyCode = KeyEvent.KEYCODE_MUHENKAN,
            actionId = "commit",
            sortOrder = 13
        )
        val scanCodeSpecificCopy = PhysicalKeyboardShortcutItem(
            context = "any",
            keyCode = KeyEvent.KEYCODE_C,
            scanCode = 46,
            ctrl = true,
            actionId = "copy",
            sortOrder = 0
        )
        assertFalse(
            LegacyPhysicalShortcutCompatibilityPolicy.shouldDeferToWindowsMapping(changedAction)
        )
        assertFalse(
            LegacyPhysicalShortcutCompatibilityPolicy.shouldDeferToWindowsMapping(
                scanCodeSpecificCopy
            )
        )
    }
}
