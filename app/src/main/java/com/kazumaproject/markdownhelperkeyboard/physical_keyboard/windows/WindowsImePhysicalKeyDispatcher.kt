package com.kazumaproject.markdownhelperkeyboard.physical_keyboard.windows

sealed class WindowsImePhysicalKeyDispatchDecision {
    object UserShortcutConsumed : WindowsImePhysicalKeyDispatchDecision()
    object ContinueExistingHandling : WindowsImePhysicalKeyDispatchDecision()
    object ForwardToApplication : WindowsImePhysicalKeyDispatchDecision()
    data class Execute(val action: WindowsImePhysicalKeyAction) :
        WindowsImePhysicalKeyDispatchDecision()
}

object WindowsImePhysicalKeyDispatcher {
    fun resolve(
        context: WindowsImePhysicalKeyContext,
        userShortcutConsumed: Boolean
    ): WindowsImePhysicalKeyDispatchDecision {
        if (userShortcutConsumed) {
            return WindowsImePhysicalKeyDispatchDecision.UserShortcutConsumed
        }
        return when (val action = WindowsImePhysicalKeyResolver.resolve(context)) {
            WindowsImePhysicalKeyAction.PassThrough ->
                WindowsImePhysicalKeyDispatchDecision.ContinueExistingHandling

            WindowsImePhysicalKeyAction.ForwardToApplication ->
                WindowsImePhysicalKeyDispatchDecision.ForwardToApplication

            else -> WindowsImePhysicalKeyDispatchDecision.Execute(action)
        }
    }
}
