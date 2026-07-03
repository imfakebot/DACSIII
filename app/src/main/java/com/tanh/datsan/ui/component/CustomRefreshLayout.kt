package com.tanh.datsan.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun CustomRefreshLayout(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    PullToRefreshBox(
       isRefreshing = isRefreshing,
        onRefresh ={
            coroutineScope.launch {
                isRefreshing = true
                onRefresh()
                delay(800)
                isRefreshing = false
            }
       },
        modifier = modifier.fillMaxSize()
    ) {
        content()
    }
}