package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.navigation.BarItem
import ru.koolmax.cycoffline.presentation.ui.ColoredIcon
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(fit: String, viewModel: WorkoutViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.getFitSession(fit)
    }

    val tabs = BarItem.fitTabs
    val pagerState = rememberPagerState(pageCount = {
        tabs.size
    })

    Column {
        Tabs(tabs = tabs, pagerState = pagerState)
        TabsContent(tabs = tabs, pagerState = pagerState, fit, viewModel)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tabs(tabs: List<BarItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    PrimaryTabRow(selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = {
                    //Icon(painter = painterResource(id = tab.icon), contentDescription = "")
                    ColoredIcon(
                        drawable = tab.icon,
                        color = LocalCustomColorsPalette.current.iconColorActive
                    )
                       },
                text = { Text(stringResource(tab.title)) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(tabs: List<BarItem>, pagerState: PagerState, fit: String, viewModel: WorkoutViewModel) {
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when (page) {
                0 -> {
                    WorkoutInfoScreen(viewModel)
                }
                1 -> {
                    WorkoutChartScreen(viewModel)
                }
                2 -> {
                    WorkoutStatisticsScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun TabsPreview() {
/*
    val tabs = listOf(
        TabRoute.Info,
        TabRoute.Charts
    )
    val pagerState = rememberPagerState(pageCount = {
        tabs.size
    })
    Tabs(tabs = tabs, pagerState = pagerState)
*/
}