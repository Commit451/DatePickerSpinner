package com.commit451.datepickerspinner

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DatePickerSpinnerInteractionTest {

    @Test
    fun immediateSaveIsBlockedUntilTheTappedDateCommits() = runComposeUiTest {
        val initialDate = LocalDate(1990, 6, 15)
        val state = DatePickerSpinnerState(initialDate)
        var savedDate: LocalDate? = null
        setContent {
            MaterialTheme {
                Column {
                    DatePickerSpinner(state)
                    Button(
                        enabled = !state.isSelectionInProgress,
                        onClick = {
                            if (!state.isSelectionInProgress) savedDate = state.selectedDate
                        },
                    ) {
                        Text("Save")
                    }
                }
            }
        }
        waitForIdle()
        runOnIdle { assertFalse(state.isSelectionInProgress) }
        mainClock.autoAdvance = false

        tapNextValue(wheelIndex = 1)
        onNodeWithText("Save").performClick()

        runOnIdle {
            assertTrue(state.isSelectionInProgress)
            assertEquals(initialDate, state.selectedDate)
            assertNull(savedDate)
        }

        mainClock.advanceTimeByFrame()
        onNodeWithText("Save").assertIsNotEnabled()
        mainClock.autoAdvance = true
        waitForIdle()
        onNodeWithText("Save").assertIsEnabled().performClick()

        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertEquals(LocalDate(1990, 6, 16), savedDate)
        }
    }

    @Test
    fun selectionStaysPendingUntilEveryWheelSettles() = runComposeUiTest {
        val state = DatePickerSpinnerState(LocalDate(1990, 6, 15))
        showPicker(state)

        tapNextValue(wheelIndex = 1)
        mainClock.advanceTimeBy(64)
        tapNextValue(wheelIndex = 0)
        mainClock.advanceTimeUntil { state.selectedDate.day == 16 }

        runOnIdle { assertTrue(state.isSelectionInProgress) }

        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertEquals(LocalDate(1990, 7, 16), state.selectedDate)
        }
    }

    @Test
    fun touchDragAndFlingStayPendingUntilTheSelectionCommits() = runComposeUiTest {
        val initialDate = LocalDate(1990, 6, 15)
        val state = DatePickerSpinnerState(initialDate)
        showPicker(state)

        wheel(1).performTouchInput {
            down(center)
            moveBy(Offset(0f, -height / 3f))
        }
        runOnIdle { assertTrue(state.isSelectionInProgress) }

        wheel(1).performTouchInput {
            moveBy(Offset(0f, -height / 4f))
            up()
        }
        runOnIdle { assertTrue(state.isSelectionInProgress) }

        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertNotEquals(initialDate, state.selectedDate)
        }
    }

    @Test
    fun mouseDragStaysPendingBetweenScrollUpdatesAndThroughTheFling() = runComposeUiTest {
        val initialDate = LocalDate(1990, 6, 15)
        val state = DatePickerSpinnerState(initialDate)
        showPicker(state)

        wheel(1).performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(0f, -height / 3f))
        }
        mainClock.advanceTimeBy(64)
        runOnIdle { assertTrue(state.isSelectionInProgress) }

        wheel(1).performMouseInput {
            moveBy(Offset(0f, -height / 4f))
            release()
        }
        runOnIdle { assertTrue(state.isSelectionInProgress) }

        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertNotEquals(initialDate, state.selectedDate)
        }
    }

    @Test
    fun mouseWheelScrollingCommitsBeforeClearingPendingSelection() = runComposeUiTest {
        val initialDate = LocalDate(1990, 6, 15)
        val state = DatePickerSpinnerState(initialDate)
        showPicker(state)

        wheel(1).performMouseInput {
            moveTo(center)
            scroll(height / 3f)
        }
        runOnIdle { assertTrue(state.isSelectionInProgress) }
        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertNotEquals(initialDate, state.selectedDate)
        }
    }

    @Test
    fun interruptingAnAnimationDoesNotLeaveTheSelectionPending() = runComposeUiTest {
        val state = DatePickerSpinnerState(LocalDate(1990, 6, 15))
        showPicker(state)

        tapNextValue(wheelIndex = 1)
        runOnIdle { assertTrue(state.isSelectionInProgress) }
        mainClock.advanceTimeBy(64)
        wheel(1).performTouchInput {
            down(center)
            up()
        }
        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle { assertFalse(state.isSelectionInProgress) }
        onNodeWithText(state.selectedDate.day.toString()).assertIsDisplayed()
    }

    @Test
    fun replacingTheHoistedStateTransfersSelectionTrackingToTheNewOwner() = runComposeUiTest {
        val initialDate = LocalDate(1990, 6, 15)
        val initialState = DatePickerSpinnerState(initialDate)
        val replacementState = DatePickerSpinnerState(LocalDate(1990, 6, 20))
        var state by mutableStateOf(initialState)
        setContent {
            MaterialTheme { DatePickerSpinner(state) }
        }
        waitForIdle()
        runOnIdle { state = replacementState }
        waitForIdle()
        mainClock.autoAdvance = false

        tapNextValue(wheelIndex = 1)
        runOnIdle {
            assertFalse(initialState.isSelectionInProgress)
            assertTrue(replacementState.isSelectionInProgress)
        }
        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle {
            assertEquals(initialDate, initialState.selectedDate)
            assertFalse(replacementState.isSelectionInProgress)
            assertEquals(LocalDate(1990, 6, 21), replacementState.selectedDate)
        }
    }

    @Test
    fun removingAndReattachingThePickerClearsPendingAnimations() = runComposeUiTest {
        val initialDate = LocalDate(1990, 6, 15)
        val state = DatePickerSpinnerState(initialDate)
        var showPicker by mutableStateOf(true)
        setContent {
            MaterialTheme {
                if (showPicker) DatePickerSpinner(state)
            }
        }
        waitForIdle()
        mainClock.autoAdvance = false
        tapNextValue(wheelIndex = 1)
        runOnIdle {
            assertTrue(state.isSelectionInProgress)
            showPicker = false
        }

        mainClock.advanceTimeByFrame()
        waitForIdle()
        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertEquals(initialDate, state.selectedDate)
            showPicker = true
        }
        mainClock.autoAdvance = true
        waitForIdle()

        runOnIdle {
            assertFalse(state.isSelectionInProgress)
            assertEquals(initialDate, state.selectedDate)
        }
    }

    private fun ComposeUiTest.showPicker(state: DatePickerSpinnerState) {
        setContent {
            MaterialTheme { DatePickerSpinner(state) }
        }
        waitForIdle()
        mainClock.autoAdvance = false
    }

    private fun ComposeUiTest.wheel(index: Int) = onAllNodes(hasScrollAction())[index]

    private fun ComposeUiTest.tapNextValue(wheelIndex: Int) {
        wheel(wheelIndex).performTouchInput { click(Offset(centerX, height * 5f / 6f)) }
    }
}
