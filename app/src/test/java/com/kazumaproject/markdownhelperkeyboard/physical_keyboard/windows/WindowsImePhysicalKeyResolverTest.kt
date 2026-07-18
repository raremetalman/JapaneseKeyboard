package com.kazumaproject.markdownhelperkeyboard.physical_keyboard.windows

import android.view.KeyEvent
import com.kazumaproject.core.domain.physical_keyboard.PhysicalKeyboardInputMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class WindowsImePhysicalKeyResolverTest {
    @Test
    fun softwareKeyboardEvent_isNotHandled() {
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_SPACE, physical = false, state = composition())
        )
    }

    @Test
    fun unknownKey_passesThroughToExistingHandling() {
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_TAB)
        )
    }

    @Test
    fun applicationAndSystemShortcuts_areForwarded() {
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_C, ctrl = true, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_DPAD_LEFT, ctrl = true, state = conversion())
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_TAB, alt = true)
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_S, meta = true)
        )
    }

    @Test
    fun zenkakuHankaku_togglesJapaneseInput() {
        assertAction(
            WindowsImePhysicalKeyAction.ToggleJapaneseInput,
            context(KeyEvent.KEYCODE_ZENKAKU_HANKAKU, japanese = false)
        )
    }

    @Test
    fun hiraganaKey_resolvesPlainShiftAndAlt() {
        assertAction(
            WindowsImePhysicalKeyAction.SetHiraganaMode,
            context(KeyEvent.KEYCODE_KATAKANA_HIRAGANA)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SetFullKatakanaMode,
            context(KeyEvent.KEYCODE_KATAKANA_HIRAGANA, shift = true)
        )
        assertAction(
            WindowsImePhysicalKeyAction.ToggleRomajiKana,
            context(KeyEvent.KEYCODE_KATAKANA_HIRAGANA, alt = true)
        )
        assertAction(
            WindowsImePhysicalKeyAction.ToggleRomajiKana,
            context(
                KeyEvent.KEYCODE_KANA,
                alt = true,
                physicalInputMode = PhysicalKeyboardInputMode.KANA
            )
        )
    }

    @Test
    fun muhenkan_cyclesCompositionAndCancelsConversion() {
        assertAction(
            WindowsImePhysicalKeyAction.CycleCompositionCharacterType,
            context(KeyEvent.KEYCODE_MUHENKAN, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.CancelConversion,
            context(KeyEvent.KEYCODE_MUHENKAN, state = conversion())
        )
        assertAction(
            WindowsImePhysicalKeyAction.CancelConversion,
            context(KeyEvent.KEYCODE_MUHENKAN, state = bunsetsu())
        )
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_MUHENKAN)
        )
    }

    @Test
    fun henkan_startsAdvancesOrSafelyReconverts() {
        assertAction(
            WindowsImePhysicalKeyAction.StartConversion,
            context(KeyEvent.KEYCODE_HENKAN, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextCandidate,
            context(KeyEvent.KEYCODE_HENKAN, state = conversion(), candidates = 5)
        )
        assertAction(
            WindowsImePhysicalKeyAction.ReconvertSelection,
            context(KeyEvent.KEYCODE_HENKAN, canReconvert = true)
        )
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_HENKAN, canReconvert = false)
        )
    }

    @Test
    fun space_matchesWindowsConversionBehavior() {
        assertAction(
            WindowsImePhysicalKeyAction.StartConversion,
            context(KeyEvent.KEYCODE_SPACE, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextCandidate,
            context(KeyEvent.KEYCODE_SPACE, state = conversion(), candidates = 5)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectPreviousCandidate,
            context(
                KeyEvent.KEYCODE_SPACE,
                shift = true,
                state = conversion(),
                candidates = 5
            )
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_SPACE, shift = true, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_SPACE, japanese = false)
        )
    }

    @Test
    fun arrows_onlySelectCandidatesDuringConversion() {
        assertAction(
            WindowsImePhysicalKeyAction.SelectPreviousCandidate,
            context(KeyEvent.KEYCODE_DPAD_LEFT, state = conversion(), candidates = 5)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectPreviousCandidate,
            context(KeyEvent.KEYCODE_DPAD_UP, state = conversion(), candidates = 5)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextCandidate,
            context(KeyEvent.KEYCODE_DPAD_RIGHT, state = conversion(), candidates = 5)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextCandidate,
            context(KeyEvent.KEYCODE_DPAD_DOWN, state = conversion(), candidates = 5)
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_DPAD_LEFT, state = composition())
        )
    }

    @Test
    fun bunsetsuArrows_preserveExistingSegmentNavigation() {
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_DPAD_LEFT, state = bunsetsu(), candidates = 3)
        )
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_DPAD_RIGHT, shift = true, state = bunsetsu(), candidates = 3)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectPreviousCandidate,
            context(KeyEvent.KEYCODE_DPAD_UP, state = bunsetsu(), candidates = 3)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextCandidate,
            context(KeyEvent.KEYCODE_DPAD_DOWN, state = bunsetsu(), candidates = 3)
        )
    }

    @Test
    fun pageKeys_onlyMoveVisibleCandidatePages() {
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextPage,
            context(KeyEvent.KEYCODE_PAGE_DOWN, state = conversion(), candidates = 10)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectPreviousPage,
            context(KeyEvent.KEYCODE_PAGE_UP, state = conversion(), candidates = 10)
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_PAGE_DOWN)
        )
    }

    @Test
    fun enter_commitsOnlyWhenInputExists() {
        assertAction(
            WindowsImePhysicalKeyAction.Commit,
            context(KeyEvent.KEYCODE_ENTER, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.Commit,
            context(KeyEvent.KEYCODE_ENTER, state = conversion())
        )
        assertAction(
            WindowsImePhysicalKeyAction.PassThrough,
            context(KeyEvent.KEYCODE_ENTER)
        )
    }

    @Test
    fun escape_cancelsInStagesAndForwardsWhenIdle() {
        assertAction(
            WindowsImePhysicalKeyAction.CancelConversion,
            context(KeyEvent.KEYCODE_ESCAPE, state = conversion())
        )
        assertAction(
            WindowsImePhysicalKeyAction.RevertComposition,
            context(KeyEvent.KEYCODE_ESCAPE, state = composition())
        )
        assertAction(
            WindowsImePhysicalKeyAction.ForwardToApplication,
            context(KeyEvent.KEYCODE_ESCAPE)
        )
    }

    @Test
    fun functionKeys_convertOnlyActiveJapaneseComposition() {
        val expected = listOf(
            KeyEvent.KEYCODE_F6 to WindowsImePhysicalKeyAction.ConvertToHiragana,
            KeyEvent.KEYCODE_F7 to WindowsImePhysicalKeyAction.ConvertToFullKatakana,
            KeyEvent.KEYCODE_F8 to WindowsImePhysicalKeyAction.ConvertToHalfKatakana,
            KeyEvent.KEYCODE_F9 to WindowsImePhysicalKeyAction.ConvertToFullWidthAlphanumeric,
            KeyEvent.KEYCODE_F10 to WindowsImePhysicalKeyAction.ConvertToHalfWidthAlphanumeric
        )
        expected.forEach { (keyCode, action) ->
            assertAction(action, context(keyCode, state = composition()))
            assertAction(action, context(keyCode, state = conversion(), candidates = 5))
            assertAction(
                WindowsImePhysicalKeyAction.ForwardToApplication,
                context(keyCode)
            )
        }
    }

    @Test
    fun numberKeys_areNotClaimedWithoutCandidateNumberLabels() {
        (KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_9).forEach { keyCode ->
            assertAction(
                WindowsImePhysicalKeyAction.PassThrough,
                context(keyCode, state = conversion(), candidates = 5)
            )
        }
    }

    @Test
    fun repeat_suppressesModeToggleButAllowsCandidateMovement() {
        assertAction(
            WindowsImePhysicalKeyAction.Consume,
            context(KeyEvent.KEYCODE_ZENKAKU_HANKAKU, repeatCount = 1)
        )
        assertAction(
            WindowsImePhysicalKeyAction.SelectNextCandidate,
            context(
                KeyEvent.KEYCODE_DPAD_DOWN,
                repeatCount = 2,
                state = conversion(),
                candidates = 5
            )
        )
    }

    @Test
    fun dispatcher_givesUserShortcutHighestPriority() {
        val decision = WindowsImePhysicalKeyDispatcher.resolve(
            context = context(KeyEvent.KEYCODE_SPACE, state = composition()),
            userShortcutConsumed = true
        )
        assertSame(
            WindowsImePhysicalKeyDispatchDecision.UserShortcutConsumed,
            decision
        )
    }

    @Test
    fun dispatcher_distinguishesExistingAndApplicationFallbacks() {
        assertSame(
            WindowsImePhysicalKeyDispatchDecision.ContinueExistingHandling,
            WindowsImePhysicalKeyDispatcher.resolve(
                context(KeyEvent.KEYCODE_TAB),
                userShortcutConsumed = false
            )
        )
        assertSame(
            WindowsImePhysicalKeyDispatchDecision.ForwardToApplication,
            WindowsImePhysicalKeyDispatcher.resolve(
                context(KeyEvent.KEYCODE_C, ctrl = true),
                userShortcutConsumed = false
            )
        )
        assertEquals(
            WindowsImePhysicalKeyDispatchDecision.Execute(
                WindowsImePhysicalKeyAction.StartConversion
            ),
            WindowsImePhysicalKeyDispatcher.resolve(
                context(KeyEvent.KEYCODE_SPACE, state = composition()),
                userShortcutConsumed = false
            )
        )
    }

    private fun assertAction(
        expected: WindowsImePhysicalKeyAction,
        context: WindowsImePhysicalKeyContext
    ) {
        assertEquals(expected, WindowsImePhysicalKeyResolver.resolve(context))
    }

    private fun context(
        keyCode: Int,
        ctrl: Boolean = false,
        shift: Boolean = false,
        alt: Boolean = false,
        meta: Boolean = false,
        repeatCount: Int = 0,
        state: WindowsImePhysicalInputState = WindowsImePhysicalInputState.IDLE,
        physicalInputMode: PhysicalKeyboardInputMode = PhysicalKeyboardInputMode.ROMAJI,
        japanese: Boolean = true,
        candidates: Int = 0,
        canReconvert: Boolean = false,
        physical: Boolean = true
    ) = WindowsImePhysicalKeyContext(
        keyCode = keyCode,
        ctrl = ctrl,
        shift = shift,
        alt = alt,
        meta = meta,
        repeatCount = repeatCount,
        inputState = state,
        physicalInputMode = physicalInputMode,
        japaneseInputEnabled = japanese,
        candidateCount = candidates,
        canReconvert = canReconvert,
        isPhysicalKeyboardEvent = physical
    )

    private fun composition() = WindowsImePhysicalInputState.COMPOSITION
    private fun conversion() = WindowsImePhysicalInputState.CONVERSION
    private fun bunsetsu() = WindowsImePhysicalInputState.BUNSETSU_CONVERSION
}
