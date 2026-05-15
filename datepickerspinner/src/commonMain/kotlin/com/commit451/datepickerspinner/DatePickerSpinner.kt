package com.commit451.datepickerspinner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs

/** A selectable field of a [DatePickerSpinner]. */
enum class DateField { Year, Month, Day }

/** Number of rows visible in each wheel at once. Must be odd so one row sits at the center. */
private const val VisibleItemCount = 3

/** Width of a single wheel column when the picker is laid out at its compact size. */
private val WheelWidth = 70.dp

/** Height added on top of the text height to give each row comfortable padding. */
private val RowExtraHeight = 24.dp

/**
 * A spinner-style date picker — three independently scrollable, wrapping wheels for month, day
 * and year, mirroring the look of the legacy [android.widget.DatePicker] spinner mode.
 *
 * Each wheel snaps to the nearest value; the centered value (framed by two divider lines) is the
 * current selection. Tapping a dimmed neighboring value scrolls it into the center.
 *
 * The picker is compact by default. When its width is constrained — for example with
 * `Modifier.fillMaxWidth()` — the three columns stretch to evenly share the available width.
 *
 * @param state the [DatePickerSpinnerState] holding the current selection. Create one with
 * [rememberDatePickerSpinnerState].
 * @param modifier the [Modifier] applied to the picker.
 * @param dateFormatter formats the wheel values and controls the wheel order. Create one with
 * [DatePickerSpinnerDefaults.dateFormatter], or implement [DatePickerSpinnerFormatter] directly.
 * @param colors the [DatePickerSpinnerColors] used to render the picker.
 */
@Composable
fun DatePickerSpinner(
    state: DatePickerSpinnerState,
    modifier: Modifier = Modifier,
    dateFormatter: DatePickerSpinnerFormatter = remember { DatePickerSpinnerDefaults.dateFormatter() },
    colors: DatePickerSpinnerColors = DatePickerSpinnerDefaults.colors(),
) {
    // Text styling follows the theme, matching Material 3's DatePicker. Restyle by overriding
    // the bodyLarge typography role in MaterialTheme.
    val textStyle = MaterialTheme.typography.bodyLarge

    // Row height follows the text style, so larger styles are not clipped.
    val density = LocalDensity.current
    val itemHeight = remember(textStyle, density) {
        val lineHeight = when {
            textStyle.lineHeight.isSp -> textStyle.lineHeight
            textStyle.fontSize.isSp -> textStyle.fontSize * 1.4f
            else -> 24.sp
        }
        with(density) { lineHeight.toDp() } + RowExtraHeight
    }

    BoxWithConstraints(modifier = modifier) {
        // With no width constraint the columns keep their compact, native-like fixed width.
        // When the caller fixes the width (e.g. fillMaxWidth) they stretch to share it instead.
        val stretchToWidth = constraints.hasFixedWidth

        Row(
            modifier = if (stretchToWidth) Modifier.fillMaxWidth() else Modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val wheelModifier =
                if (stretchToWidth) Modifier.weight(1f) else Modifier.width(WheelWidth)

            for (field in dateFormatter.fieldOrder) {
                when (field) {
                    DateField.Month -> WheelSpinner(
                        count = 12,
                        selectedIndex = state.selectedMonth - 1,
                        onSelectedIndexChange = { state.selectMonth(it + 1) },
                        label = { dateFormatter.formatMonth(it + 1) },
                        textStyle = textStyle,
                        colors = colors,
                        itemHeight = itemHeight,
                        modifier = wheelModifier,
                    )

                    DateField.Day -> WheelSpinner(
                        count = daysInMonth(state.selectedYear, state.selectedMonth),
                        selectedIndex = state.selectedDay - 1,
                        onSelectedIndexChange = { state.selectDay(it + 1) },
                        label = { dateFormatter.formatDay(it + 1) },
                        textStyle = textStyle,
                        colors = colors,
                        itemHeight = itemHeight,
                        modifier = wheelModifier,
                    )

                    DateField.Year -> WheelSpinner(
                        count = state.yearRange.last - state.yearRange.first + 1,
                        selectedIndex = state.selectedYear - state.yearRange.first,
                        onSelectedIndexChange = { state.selectYear(state.yearRange.first + it) },
                        label = { dateFormatter.formatYear(state.yearRange.first + it) },
                        textStyle = textStyle,
                        colors = colors,
                        itemHeight = itemHeight,
                        modifier = wheelModifier,
                    )
                }
            }
        }
    }
}

/**
 * A single scrollable, wrapping wheel of [count] values, snapping the nearest value to the center.
 *
 * @param selectedIndex the value index to center; also used to react to external changes.
 * @param onSelectedIndexChange called when scrolling settles on a new value.
 * @param label maps a value index to its display text.
 * @param textStyle the text style applied to every value.
 * @param colors the colors used to render values and the divider lines.
 * @param itemHeight the height of a single row.
 */
@Composable
private fun WheelSpinner(
    count: Int,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    label: (index: Int) -> String,
    textStyle: TextStyle,
    colors: DatePickerSpinnerColors,
    itemHeight: Dp,
    modifier: Modifier = Modifier,
) {
    // The list is virtually infinite; the value at a list index is index % count, so the wheel
    // wraps around like the native NumberPicker.
    val initialListIndex = remember {
        val anchor = Int.MAX_VALUE / 2
        anchor - anchor.mod(count) + (selectedIndex - 1).mod(count)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialListIndex)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val scope = rememberCoroutineScope()

    // List index nearest the viewport center. When snapped this is firstVisibleItemIndex + 1.
    val centeredListIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) {
                listState.firstVisibleItemIndex + 1
            } else {
                val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                visible.minByOrNull { item ->
                    abs((item.offset + item.size / 2f) - viewportCenter)
                }!!.index
            }
        }
    }

    // Report the centered value once the wheel settles. selectMonth/Day/Year are idempotent, so
    // reporting an unchanged value is harmless.
    LaunchedEffect(listState, count) {
        snapshotFlow { listState.isScrollInProgress }
            .drop(1)
            .filter { scrolling -> !scrolling }
            .collect { onSelectedIndexChange(centeredListIndex.mod(count)) }
    }

    // Re-align the wheel when the selection — or the number of values — changes from outside.
    LaunchedEffect(selectedIndex, count) {
        if (listState.isScrollInProgress) return@LaunchedEffect
        val centered = centeredListIndex
        val delta = selectedIndex - centered.mod(count)
        if (delta != 0) {
            listState.scrollToItem((centered + delta - 1).coerceAtLeast(0))
        }
    }

    val lineColor = colors.dividerColor
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val strokePx = with(density) { 1.5.dp.toPx() }
    val insetPx = with(density) { 8.dp.toPx() }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * VisibleItemCount)
            // Frame the center row with a divider line above and below it.
            .drawWithContent {
                drawContent()
                val topLineY = itemHeightPx
                val bottomLineY = itemHeightPx * 2f
                drawLine(
                    color = lineColor,
                    start = Offset(insetPx, topLineY),
                    end = Offset(size.width - insetPx, topLineY),
                    strokeWidth = strokePx,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(insetPx, bottomLineY),
                    end = Offset(size.width - insetPx, bottomLineY),
                    strokeWidth = strokePx,
                )
            },
    ) {
        items(Int.MAX_VALUE, key = { it }) { listIndex ->
            val isSelected = listIndex == centeredListIndex
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            listState.animateScrollToItem((listIndex - 1).coerceAtLeast(0))
                        }
                    }
                    // Only the centered value is exposed to accessibility services; the dimmed
                    // neighbours are cleared so a screen reader announces a single value.
                    .then(if (isSelected) Modifier else Modifier.clearAndSetSemantics {}),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(listIndex.mod(count)),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = textStyle,
                    color = if (isSelected) colors.selectedTextColor else colors.unselectedTextColor,
                )
            }
        }
    }
}
