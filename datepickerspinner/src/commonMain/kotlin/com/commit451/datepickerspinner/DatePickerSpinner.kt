package com.commit451.datepickerspinner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Number of rows visible in each wheel at once. Must be odd so one row sits at the center. */
private const val VisibleItemCount = 3

/** Rows of empty padding above/below the values so the first/last value can reach the center. */
private const val EdgeItemCount = VisibleItemCount / 2

/** Height of a single wheel row. */
private val ItemHeight = 48.dp

private val MonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/**
 * A spinner-style date picker — three independently scrollable wheels for month, day and year,
 * mirroring the look of the legacy Android [android.widget.DatePicker] spinner mode.
 *
 * Each wheel snaps to the nearest value; the centered value (framed by two divider lines) is the
 * current selection. Tapping a dimmed neighbouring value scrolls it into the center.
 *
 * @param modifier the [Modifier] applied to the picker.
 * @param initialDate the date shown when the picker first appears. Defaults to [today].
 * @param yearRange the inclusive range of selectable years.
 * @param onDateChange called whenever the selected date changes.
 */
@Composable
fun DatePickerSpinner(
    modifier: Modifier = Modifier,
    initialDate: CalendarDate = today(),
    yearRange: IntRange = 1900..2100,
    onDateChange: (CalendarDate) -> Unit = {},
) {
    var year by remember { mutableStateOf(initialDate.year.coerceIn(yearRange)) }
    var month by remember { mutableStateOf(initialDate.month.coerceIn(1, 12)) }
    var day by remember {
        mutableStateOf(
            initialDate.dayOfMonth.coerceIn(1, daysInMonth(initialDate.year, initialDate.month)),
        )
    }

    val daysThisMonth = daysInMonth(year, month)

    LaunchedEffect(year, month, day) {
        onDateChange(CalendarDate(year, month, day))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WheelSpinner(
            count = MonthNames.size,
            selectedIndex = month - 1,
            onSelectedIndexChange = { index ->
                month = index + 1
                // A shorter month may invalidate the current day.
                day = day.coerceAtMost(daysInMonth(year, month))
            },
            label = { MonthNames[it] },
            modifier = Modifier.weight(1f),
        )
        WheelSpinner(
            count = daysThisMonth,
            selectedIndex = (day - 1).coerceIn(0, daysThisMonth - 1),
            onSelectedIndexChange = { index -> day = index + 1 },
            label = { (it + 1).toString() },
            modifier = Modifier.weight(1f),
        )
        WheelSpinner(
            count = yearRange.last - yearRange.first + 1,
            selectedIndex = year - yearRange.first,
            onSelectedIndexChange = { index ->
                year = yearRange.first + index
                // Leap years change the length of February.
                day = day.coerceAtMost(daysInMonth(year, month))
            },
            label = { (yearRange.first + it).toString() },
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * A single scrollable wheel of [count] values, snapping the nearest value to the center.
 *
 * @param selectedIndex the value index to center; also used to react to external changes.
 * @param onSelectedIndexChange called when scrolling settles on a new value.
 * @param label maps a value index to its display text.
 */
@Composable
private fun WheelSpinner(
    count: Int,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    label: (index: Int) -> String,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val scope = rememberCoroutineScope()

    // The list index whose center is nearest the viewport center. Padding rows mean the centered
    // value index is this minus EdgeItemCount.
    val centeredListIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) {
                selectedIndex + EdgeItemCount
            } else {
                val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                visible.minByOrNull { item ->
                    abs((item.offset + item.size / 2f) - viewportCenter)
                }!!.index
            }
        }
    }

    // Report a new selection once the wheel settles on it.
    LaunchedEffect(listState, count) {
        snapshotFlow { listState.isScrollInProgress }
            .drop(1)
            .filter { scrolling -> !scrolling }
            .collect {
                val value = (centeredListIndex - EdgeItemCount).coerceIn(0, count - 1)
                if (value != selectedIndex) onSelectedIndexChange(value)
            }
    }

    // Re-align the wheel when the selection changes from the outside (e.g. day clamping).
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != selectedIndex) {
            listState.scrollToItem(selectedIndex)
        }
    }

    val lineColor = MaterialTheme.colorScheme.outline
    val density = LocalDensity.current
    val itemHeightPx = with(density) { ItemHeight.toPx() }
    val strokePx = with(density) { 1.dp.toPx() }
    val insetPx = with(density) { 8.dp.toPx() }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(ItemHeight * VisibleItemCount)
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
        item { Spacer(Modifier.height(ItemHeight)) }
        items(count, key = { it }) { index ->
            val isSelected = index + EdgeItemCount == centeredListIndex
            Box(
                modifier = Modifier
                    .height(ItemHeight)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch { listState.animateScrollToItem(index) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(index),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = if (isSelected) 20.sp else 18.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    },
                )
            }
        }
        item { Spacer(Modifier.height(ItemHeight)) }
    }
}
