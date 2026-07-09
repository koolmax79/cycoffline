package ru.koolmax.cycoffline.presentation.ui.navigation

import android.R
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, title: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    //AnimatedVisibility(
    //    visible = barState,
    //    enter = slideInVertically(initialOffsetY = { -it }),
    //    exit = slideOutVertically(targetOffsetY = { -it }),
    //    content = {
//            //title = { Text(text = title) },
    //        val currentRoute = navBackStackEntry?.destination?.route
            TopAppBar(title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                    }
                })
    //    }
    //)
}