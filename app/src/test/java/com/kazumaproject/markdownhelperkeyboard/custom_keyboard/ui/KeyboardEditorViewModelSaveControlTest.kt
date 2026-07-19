package com.kazumaproject.markdownhelperkeyboard.custom_keyboard.ui

import com.kazumaproject.custom_keyboard.data.KeyboardLayoutUsageMode
import com.kazumaproject.custom_keyboard.layout.KeyboardDefaultLayouts
import com.kazumaproject.markdownhelperkeyboard.repository.KeyboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class KeyboardEditorViewModelSaveControlTest {

    @Test
    fun discardEditingSession_reloadsPersistedLayoutOnNextStart() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = mock<KeyboardRepository>()
            val savedLayout = KeyboardDefaultLayouts.createPcQwertyTemplateLayout()
            whenever(repository.getLayoutName(42L)).thenReturn("Saved layout")
            whenever(repository.getFullLayout(42L)).thenReturn(flowOf(savedLayout))
            val viewModel = KeyboardEditorViewModel(repository)

            viewModel.start(42L)
            advanceUntilIdle()
            viewModel.updateName("Unsaved draft")
            viewModel.updateIsRomaji(!savedLayout.isRomaji)
            assertTrue(viewModel.hasUnsavedChanges())

            viewModel.onCancelEditing()
            viewModel.start(42L)
            advanceUntilIdle()

            assertEquals("Saved layout", viewModel.uiState.value.name)
            assertEquals(savedLayout, viewModel.uiState.value.layout)
            assertFalse(viewModel.hasUnsavedChanges())
            verify(repository, times(2)).getFullLayout(42L)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun returningFromKeyEditor_keepsUnsavedEditingSession() {
        val repository = mock<KeyboardRepository>()
        val viewModel = KeyboardEditorViewModel(repository)
        viewModel.start(-1L)
        viewModel.updateName("Draft layout")

        viewModel.start(-1L)

        assertEquals("Draft layout", viewModel.uiState.value.name)
        assertTrue(viewModel.hasUnsavedChanges())
        verifyNoInteractions(repository)
    }

    @Test
    fun usageModeChange_updatesOnlyUiUntilSave() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = mock<KeyboardRepository>()
            whenever(repository.doesNameExist(any(), isNull())).thenReturn(false)
            whenever(repository.saveLayout(any(), any(), isNull())).thenReturn(7L)
            val viewModel = KeyboardEditorViewModel(repository)
            viewModel.start(-1L)

            viewModel.updateLayoutUsageMode(KeyboardLayoutUsageMode.Number)

            assertEquals(
                KeyboardLayoutUsageMode.Number,
                viewModel.uiState.value.layout.usageMode
            )
            assertTrue(viewModel.hasUnsavedChanges())
            verify(repository, never()).saveLayout(any(), any(), any())

            viewModel.saveLayout()
            advanceUntilIdle()

            val layoutCaptor = argumentCaptor<com.kazumaproject.custom_keyboard.data.KeyboardLayout>()
            verify(repository).saveLayout(layoutCaptor.capture(), any(), isNull())
            assertEquals(KeyboardLayoutUsageMode.Number, layoutCaptor.firstValue.usageMode)
            assertTrue(viewModel.uiState.value.navigateBack)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun duplicateNameAndSaveFailure_doNotNavigateAway() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val duplicateRepository = mock<KeyboardRepository>()
            whenever(duplicateRepository.doesNameExist(any(), isNull())).thenReturn(true)
            val duplicateViewModel = KeyboardEditorViewModel(duplicateRepository)
            duplicateViewModel.start(-1L)

            duplicateViewModel.saveLayout()
            advanceUntilIdle()

            assertTrue(duplicateViewModel.uiState.value.duplicateNameError)
            assertFalse(duplicateViewModel.uiState.value.navigateBack)
            verify(duplicateRepository, never()).saveLayout(any(), any(), any())

            val failingRepository = mock<KeyboardRepository>()
            whenever(failingRepository.doesNameExist(any(), isNull())).thenReturn(false)
            whenever(failingRepository.saveLayout(any(), any(), isNull()))
                .thenThrow(IllegalStateException("save failed"))
            val failingViewModel = KeyboardEditorViewModel(failingRepository)
            failingViewModel.start(-1L)
            failingViewModel.updateName("Unsaved failing draft")

            failingViewModel.saveLayout()
            advanceUntilIdle()

            assertFalse(failingViewModel.uiState.value.navigateBack)
            assertTrue(failingViewModel.hasUnsavedChanges())
        } finally {
            Dispatchers.resetMain()
        }
    }
}
