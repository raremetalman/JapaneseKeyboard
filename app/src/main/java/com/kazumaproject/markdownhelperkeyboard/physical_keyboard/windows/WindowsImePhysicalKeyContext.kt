package com.kazumaproject.markdownhelperkeyboard.physical_keyboard.windows

import com.kazumaproject.core.domain.physical_keyboard.PhysicalKeyboardInputMode

enum class WindowsImePhysicalInputState {
    IDLE,
    COMPOSITION,
    CONVERSION,
    BUNSETSU_CONVERSION
}

data class WindowsImePhysicalKeyContext(
    val keyCode: Int,
    val scanCode: Int = 0,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false,
    val meta: Boolean = false,
    val repeatCount: Int = 0,
    val inputState: WindowsImePhysicalInputState = WindowsImePhysicalInputState.IDLE,
    val physicalInputMode: PhysicalKeyboardInputMode = PhysicalKeyboardInputMode.ROMAJI,
    val japaneseInputEnabled: Boolean = true,
    val candidateCount: Int = 0,
    val selectedCandidateIndex: Int = -1,
    val canReconvert: Boolean = false,
    val isPhysicalKeyboardEvent: Boolean = true
)
