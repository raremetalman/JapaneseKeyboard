package com.kazumaproject.markdownhelperkeyboard.physical_keyboard.windows

import android.view.KeyEvent
import com.kazumaproject.markdownhelperkeyboard.physical_keyboard.shortcut.PhysicalKeyboardShortcutAction
import com.kazumaproject.markdownhelperkeyboard.physical_keyboard.shortcut.PhysicalKeyboardShortcutContext
import com.kazumaproject.markdownhelperkeyboard.physical_keyboard.shortcut.database.PhysicalKeyboardShortcutItem

object LegacyPhysicalShortcutCompatibilityPolicy {
    fun shouldDeferToWindowsMapping(shortcut: PhysicalKeyboardShortcutItem): Boolean {
        if (!shortcut.enabled || shortcut.scanCode != null || shortcut.shift ||
            shortcut.alt || shortcut.meta
        ) {
            return false
        }
        if (shortcut.context == PhysicalKeyboardShortcutContext.COMPOSITION.id &&
            shortcut.keyCode == KeyEvent.KEYCODE_MUHENKAN &&
            !shortcut.ctrl &&
            shortcut.actionId == PhysicalKeyboardShortcutAction.SWITCH_TO_ENGLISH.id &&
            shortcut.sortOrder == 13
        ) {
            return true
        }
        if (shortcut.context != PhysicalKeyboardShortcutContext.ANY.id || !shortcut.ctrl) {
            return false
        }
        return when (shortcut.keyCode) {
            KeyEvent.KEYCODE_C -> shortcut.actionId == PhysicalKeyboardShortcutAction.COPY.id &&
                shortcut.sortOrder == 0

            KeyEvent.KEYCODE_V -> shortcut.actionId == PhysicalKeyboardShortcutAction.PASTE.id &&
                shortcut.sortOrder == 1

            KeyEvent.KEYCODE_X -> shortcut.actionId == PhysicalKeyboardShortcutAction.CUT.id &&
                shortcut.sortOrder == 2

            KeyEvent.KEYCODE_A ->
                shortcut.actionId == PhysicalKeyboardShortcutAction.SELECT_ALL.id &&
                    shortcut.sortOrder == 3

            else -> false
        }
    }
}
