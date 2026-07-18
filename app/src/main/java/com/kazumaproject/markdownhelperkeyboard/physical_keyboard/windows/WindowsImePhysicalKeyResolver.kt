package com.kazumaproject.markdownhelperkeyboard.physical_keyboard.windows

import android.view.KeyEvent

object WindowsImePhysicalKeyResolver {
    fun resolve(context: WindowsImePhysicalKeyContext): WindowsImePhysicalKeyAction {
        if (!context.isPhysicalKeyboardEvent) {
            return WindowsImePhysicalKeyAction.PassThrough
        }

        if (context.meta || context.ctrl) {
            return WindowsImePhysicalKeyAction.ForwardToApplication
        }

        if (context.alt) {
            val action = if (isHiraganaKey(context.keyCode) && !context.shift) {
                WindowsImePhysicalKeyAction.ToggleRomajiKana
            } else {
                WindowsImePhysicalKeyAction.ForwardToApplication
            }
            return suppressNonRepeatableAction(context, action)
        }

        val action = when (context.keyCode) {
            KeyEvent.KEYCODE_ZENKAKU_HANKAKU ->
                WindowsImePhysicalKeyAction.ToggleJapaneseInput

            KeyEvent.KEYCODE_KATAKANA_HIRAGANA,
            KeyEvent.KEYCODE_KANA -> if (context.shift) {
                WindowsImePhysicalKeyAction.SetFullKatakanaMode
            } else {
                WindowsImePhysicalKeyAction.SetHiraganaMode
            }

            KeyEvent.KEYCODE_MUHENKAN -> resolveMuhenkan(context)
            KeyEvent.KEYCODE_HENKAN -> resolveHenkan(context)
            KeyEvent.KEYCODE_SPACE -> resolveSpace(context)
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> resolveDpad(context)

            KeyEvent.KEYCODE_PAGE_UP,
            KeyEvent.KEYCODE_PAGE_DOWN -> resolvePage(context)

            KeyEvent.KEYCODE_ENTER -> resolveEnter(context)
            KeyEvent.KEYCODE_ESCAPE -> resolveEscape(context)
            KeyEvent.KEYCODE_F6 -> resolveFunctionKey(
                context,
                WindowsImePhysicalKeyAction.ConvertToHiragana
            )

            KeyEvent.KEYCODE_F7 -> resolveFunctionKey(
                context,
                WindowsImePhysicalKeyAction.ConvertToFullKatakana
            )

            KeyEvent.KEYCODE_F8 -> resolveFunctionKey(
                context,
                WindowsImePhysicalKeyAction.ConvertToHalfKatakana
            )

            KeyEvent.KEYCODE_F9 -> resolveFunctionKey(
                context,
                WindowsImePhysicalKeyAction.ConvertToFullWidthAlphanumeric
            )

            KeyEvent.KEYCODE_F10 -> resolveFunctionKey(
                context,
                WindowsImePhysicalKeyAction.ConvertToHalfWidthAlphanumeric
            )

            else -> WindowsImePhysicalKeyAction.PassThrough
        }
        return suppressNonRepeatableAction(context, action)
    }

    private fun resolveMuhenkan(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction = when (context.inputState) {
        WindowsImePhysicalInputState.COMPOSITION ->
            WindowsImePhysicalKeyAction.CycleCompositionCharacterType

        WindowsImePhysicalInputState.CONVERSION,
        WindowsImePhysicalInputState.BUNSETSU_CONVERSION ->
            WindowsImePhysicalKeyAction.CancelConversion

        WindowsImePhysicalInputState.IDLE -> WindowsImePhysicalKeyAction.PassThrough
    }

    private fun resolveHenkan(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction = when (context.inputState) {
        WindowsImePhysicalInputState.COMPOSITION ->
            WindowsImePhysicalKeyAction.StartConversion

        WindowsImePhysicalInputState.CONVERSION,
        WindowsImePhysicalInputState.BUNSETSU_CONVERSION -> if (context.candidateCount > 0) {
            WindowsImePhysicalKeyAction.SelectNextCandidate
        } else {
            WindowsImePhysicalKeyAction.Consume
        }

        WindowsImePhysicalInputState.IDLE -> if (context.canReconvert) {
            WindowsImePhysicalKeyAction.ReconvertSelection
        } else {
            WindowsImePhysicalKeyAction.PassThrough
        }
    }

    private fun resolveSpace(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction {
        if (context.shift) {
            return if (context.isConvertingWithCandidates()) {
                WindowsImePhysicalKeyAction.SelectPreviousCandidate
            } else {
                WindowsImePhysicalKeyAction.ForwardToApplication
            }
        }
        return when (context.inputState) {
            WindowsImePhysicalInputState.COMPOSITION ->
                WindowsImePhysicalKeyAction.StartConversion

            WindowsImePhysicalInputState.CONVERSION,
            WindowsImePhysicalInputState.BUNSETSU_CONVERSION -> if (context.candidateCount > 0) {
                WindowsImePhysicalKeyAction.SelectNextCandidate
            } else {
                WindowsImePhysicalKeyAction.Consume
            }

            WindowsImePhysicalInputState.IDLE -> WindowsImePhysicalKeyAction.PassThrough
        }
    }

    private fun resolveDpad(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction {
        if (context.inputState == WindowsImePhysicalInputState.BUNSETSU_CONVERSION &&
            context.keyCode in setOf(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT)
        ) {
            return WindowsImePhysicalKeyAction.PassThrough
        }
        if (context.inputState !in setOf(
                WindowsImePhysicalInputState.CONVERSION,
                WindowsImePhysicalInputState.BUNSETSU_CONVERSION
            )
        ) {
            return WindowsImePhysicalKeyAction.ForwardToApplication
        }
        if (context.candidateCount <= 0) {
            return WindowsImePhysicalKeyAction.Consume
        }
        return when (context.keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_DOWN -> WindowsImePhysicalKeyAction.SelectNextCandidate

            else -> WindowsImePhysicalKeyAction.SelectPreviousCandidate
        }
    }

    private fun resolvePage(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction {
        if (!context.isConvertingWithCandidates()) {
            return WindowsImePhysicalKeyAction.ForwardToApplication
        }
        return if (context.keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            WindowsImePhysicalKeyAction.SelectNextPage
        } else {
            WindowsImePhysicalKeyAction.SelectPreviousPage
        }
    }

    private fun resolveEnter(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction = when (context.inputState) {
        WindowsImePhysicalInputState.COMPOSITION,
        WindowsImePhysicalInputState.CONVERSION,
        WindowsImePhysicalInputState.BUNSETSU_CONVERSION ->
            WindowsImePhysicalKeyAction.Commit

        WindowsImePhysicalInputState.IDLE -> WindowsImePhysicalKeyAction.PassThrough
    }

    private fun resolveEscape(
        context: WindowsImePhysicalKeyContext
    ): WindowsImePhysicalKeyAction = when (context.inputState) {
        WindowsImePhysicalInputState.CONVERSION,
        WindowsImePhysicalInputState.BUNSETSU_CONVERSION ->
            WindowsImePhysicalKeyAction.CancelConversion

        WindowsImePhysicalInputState.COMPOSITION ->
            WindowsImePhysicalKeyAction.RevertComposition

        WindowsImePhysicalInputState.IDLE ->
            WindowsImePhysicalKeyAction.ForwardToApplication
    }

    private fun resolveFunctionKey(
        context: WindowsImePhysicalKeyContext,
        action: WindowsImePhysicalKeyAction
    ): WindowsImePhysicalKeyAction {
        return if (context.japaneseInputEnabled &&
            context.inputState != WindowsImePhysicalInputState.IDLE
        ) {
            action
        } else {
            WindowsImePhysicalKeyAction.ForwardToApplication
        }
    }

    private fun suppressNonRepeatableAction(
        context: WindowsImePhysicalKeyContext,
        action: WindowsImePhysicalKeyAction
    ): WindowsImePhysicalKeyAction {
        if (context.repeatCount == 0 || action.isRepeatable()) return action
        return when (action) {
            WindowsImePhysicalKeyAction.PassThrough,
            WindowsImePhysicalKeyAction.ForwardToApplication -> action

            else -> WindowsImePhysicalKeyAction.Consume
        }
    }

    private fun WindowsImePhysicalKeyAction.isRepeatable(): Boolean {
        return this === WindowsImePhysicalKeyAction.SelectNextCandidate ||
            this === WindowsImePhysicalKeyAction.SelectPreviousCandidate ||
            this === WindowsImePhysicalKeyAction.SelectNextPage ||
            this === WindowsImePhysicalKeyAction.SelectPreviousPage
    }

    private fun WindowsImePhysicalKeyContext.isConvertingWithCandidates(): Boolean {
        return inputState in setOf(
            WindowsImePhysicalInputState.CONVERSION,
            WindowsImePhysicalInputState.BUNSETSU_CONVERSION
        ) && candidateCount > 0
    }

    private fun isHiraganaKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_KATAKANA_HIRAGANA ||
            keyCode == KeyEvent.KEYCODE_KANA
    }
}
