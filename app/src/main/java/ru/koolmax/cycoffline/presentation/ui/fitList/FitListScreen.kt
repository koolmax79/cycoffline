package ru.koolmax.cycoffline.presentation.ui.fitList

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.presentation.MeasureUtil.Companion.getYearMonth
import ru.koolmax.cycoffline.presentation.ui.navigation.Route
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalIconSize
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FitListScreen(navController: NavController, viewModel: FitViewModel = hiltViewModel()) {
    val sessions by viewModel.fitSessionList.collectAsState()
    //val openDialog = remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { viewModel.addFitToLib(it) }
    }
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    openDocumentLauncher.launch("application/octet-stream")
                    }) {
                    Icon(Icons.Filled.Add, "Add fit file")
                }
            },
        ) { _ ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                sessions.groupBy {
                    it.startTime?.let {
                        LocalDateTime.of(it.year, it.month.value, 1, 0, 0, 0)
                    }
                }.forEach { date, fitList ->
                    stickyHeader {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(LocalSpacing.current.space100)
                        ) {
                            Text(getYearMonth(date), style = MaterialTheme.typography.titleLarge,
                                fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
                        }
                    }
                    items(items = fitList, key = { session -> session.fileName }) { session ->
                        FitCard(
                            session,
                            onOpen = {
                                navController.navigate(
                                    Route.Workout.route.replace(
                                        "{fit}",
                                        session.fileName
                                    )
                                )
                            },
                            onDelete = {
                                viewModel.delFitSession(session.fileName)
                            })
                    }
                }
            }
        }
        //    if(openDialog.value)
        //AlertDialogYesNo(dialogText = "Удалить тренировку",
        //    onYes = { viewModel.delFitSession(fileForDel.value) },
        //    onClose = { openDialog.value = false })
}

@Composable
fun  FitCard(itm: FitSessionItem, onOpen: (FitSessionItem) -> Unit, onDelete: (FitSessionItem) -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(modifier = Modifier.fillMaxSize(),
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = state.dismissDirection
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                Box(modifier = Modifier.fillMaxSize().padding(end = LocalSpacing.current.space100).background(color = MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.CenterEnd) {
                    //Row(
                    //    verticalAlignment = Alignment.CenterVertically,
                    //    horizontalArrangement = Arrangement.End
                    //) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(LocalIconSize.current.size100)
                        )
                    //}
                }
            }
        },
        content = {
        ElevatedCard (modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space25)
            .combinedClickable(onClick = { onOpen(itm) }),
            colors = if (itm.isDisplayed)  CardDefaults.elevatedCardColors() else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface )) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.start),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = MeasureUtil.getDateTime(itm.startTime),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.distance),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        val distance = MeasureUtil.getDistance(itm.totalDistance)
                        Text(
                            text = distance.first,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = LocalSpacing.current.space25)
                        )
                        Text(
                            text = distance.second,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.movingTime),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = MeasureUtil.getDuration(itm.totalMovingTime),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.elapsedTime),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = MeasureUtil.getDuration(itm.totalElapsedTime),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        },
        onDismiss = {
            onDelete(itm)
        }
    )
}

@Preview
@Composable
fun Preview() {
/*
    val itm = FitSessionItem(
        fileName = "1111",
        displayed = 0,
        avgHeartRate = 80,
        avgSpeed = 25.0,
        maxAltitude = 0.6,
        maxHeartRate = 180,
        maxNegGrade = -3.0,
        maxPosGrade = 3.0,
        maxSpeed = 40.0,
        minHeartRate = 100,
        startTime = LocalDateTime.now(),
        totalAscent = 100,
        totalDescent = 20,
        totalDistance = 60000,
        totalElapsedTime = 40000,
        totalMovingTime = 23423
    )
    FitCard(itm, {}, {})
*/
}