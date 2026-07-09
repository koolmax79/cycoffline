package ru.koolmax.cycoffline.presentation.ui.lib

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun rememberPickerState() = remember { PickerState() }

class PickerState {
    var selectedIndex by mutableIntStateOf(0)
}

fun interface PickerValueFormatter {
    fun format(
        value: Any,
    ): String

    companion object {
        val Default: PickerValueFormatter
            get() {
                return SimpleFormat()
            }

        private class SimpleFormat(): PickerValueFormatter {
            override fun format(value: Any) = value.toString()

        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPicker(
    modifier: Modifier = Modifier,
    items: List<Int>,
    selectedState: MutableState<Int>,
    textStyle: TextStyle = LocalTextStyle.current,
    selectedTextStyle:TextStyle = TextStyle(fontSize = textStyle.fontSize * 1.5),
    formatter: PickerValueFormatter = PickerValueFormatter.Default,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentPadding = (maxWidth - 80.dp) / 2
        val offSet = maxWidth / 5
        val itemSpacing = offSet - 50.dp
        val pagerState = rememberPagerState(initialPage = selectedState.value, pageCount = { items.size })

        LaunchedEffect(pagerState.currentPage) {
            if(items.size > pagerState.currentPage)
                selectedState.value = items[pagerState.currentPage]
        }

        val scope = rememberCoroutineScope()

        val mutableInteractionSource = remember {
            MutableInteractionSource()
        }

        HorizontalPager(
            modifier = modifier,
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(0)
            ),
            contentPadding = PaddingValues(horizontal = contentPadding),
            pageSpacing = itemSpacing,
        ) { page ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction).absoluteValue
                        // Set the item alpha based on the distance from the center
                        val percentFromCenter = 1.0f - (pageOffset / (5f / 2f))
                        val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)

                        alpha = opacity
                        clip = true
                    }
                    .clickable(
                        interactionSource = mutableInteractionSource,
                        indication = null,
                        enabled = true,
                    ) {
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }) {
                var style = textStyle
                var fontWeight = FontWeight.Normal
                if (page == pagerState.currentPage) {
                    style = selectedTextStyle
                    fontWeight = FontWeight.Bold
                }
                Text(
                    text = formatter.format(items[page]),
                    overflow = TextOverflow.Ellipsis,
                    style = style,
                    fontWeight = fontWeight,
                    modifier = Modifier
                        .size(80.dp)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPicker(
    modifier: Modifier = Modifier,
    min: Int,
    max: Int,
    selectedState: MutableState<Int>,
    defaultValue: Int? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    selectedTextStyle: TextStyle = TextStyle(fontSize = textStyle.fontSize * 1.5),
    defaultTextStyle: TextStyle = TextStyle(textDecoration = TextDecoration.Underline),
    defaultAndSelectedTextStyle: TextStyle = TextStyle(textDecoration = TextDecoration.Underline, fontSize = textStyle.fontSize * 1.5),
    formatter: PickerValueFormatter = PickerValueFormatter.Default,
) {
    require(min < max)
    val count = max - min + 1
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentPadding = (maxWidth - 80.dp) / 2
        val offSet = maxWidth / 5
        val itemSpacing = offSet - 50.dp
        val pagerState = rememberPagerState(initialPage = selectedState.value - min, pageCount = { count })

        LaunchedEffect(pagerState.currentPage) {
            if(count > pagerState.currentPage)
                selectedState.value = pagerState.currentPage + min
        }

        val scope = rememberCoroutineScope()

        val mutableInteractionSource = remember {
            MutableInteractionSource()
        }

        HorizontalPager(
            modifier = modifier,
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(0)
            ),
            contentPadding = PaddingValues(horizontal = contentPadding),
            pageSpacing = itemSpacing,
        ) { page ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction).absoluteValue
                        // Set the item alpha based on the distance from the center
                        val percentFromCenter = 1.0f - (pageOffset / (5f / 2f))
                        val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)

                        alpha = opacity
                        clip = true
                    }
                    .clickable(
                        interactionSource = mutableInteractionSource,
                        indication = null,
                        enabled = true,
                    ) {
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }) {
                var style: TextStyle
                var fontWeight: FontWeight

                when {
                    (defaultValue!=null) && (page + min == defaultValue) && (page == pagerState.currentPage) -> {
                        style = defaultAndSelectedTextStyle
                        fontWeight = FontWeight.Bold
                    }
                    (defaultValue!=null) && (page + min == defaultValue) -> {
                        style = defaultTextStyle
                        fontWeight = FontWeight.Normal
                    }
                    (page == pagerState.currentPage) -> {
                        style = selectedTextStyle
                        fontWeight = FontWeight.Bold
                    }
                    else -> {
                        style = textStyle
                        fontWeight = FontWeight.Normal
                    }
                }

                Text(
                    text = formatter.format(page + min ),
                    overflow = TextOverflow.Ellipsis,
                    style = style,
                    fontWeight = fontWeight,
                    modifier = Modifier
                        .size(80.dp)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}