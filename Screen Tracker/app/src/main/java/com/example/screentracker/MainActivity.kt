package com.example.screentracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.screentracker.ui.theme.ScreenTrackerTheme

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.content.Intent
import android.os.Process
import android.provider.Settings

import android.app.usage.UsageStatsManager // Needed for usage stats
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar                  // Needed for time calculation


import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTrackerTheme {
                TrackPermission()
            }
        }
    }
}

//To Track Permission
@Composable
fun TrackPermission() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsagePermission by remember {
        mutableStateOf(checkUsagePermission(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsagePermission = checkUsagePermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (hasUsagePermission) {
        // CHANGED: Show UsageDataScreen instead of WelcomeScreen
        UsageDataScreen(context)
    } else {
        PermissionRequiredScreen {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
        }
    }
}

fun checkUsagePermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun getAppName(context: Context, packageName: String): String {
    return try {
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        // return the package name if nothing is found
        packageName
    }
}

//-----------------------------------------Composable UI-----------------------------------------------------------

@Composable
fun PermissionRequiredScreen(onGrantPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Permission Required",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "To analyze your Screen time, this app needs permission to access your usage data.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onGrantPermission) {
                Text(text = "Grant Permission")
            }
        }
    }
}

//@Composable
//fun WelcomeScreen() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = "Welcome to ScreenTracker",
//            style = MaterialTheme.typography.headlineLarge,
//        )
//    }
//}


@Composable
fun UsageDataScreen(context: Context) {
    // Load usage data
    val usageData = remember { getTodayUsage(context) }

    // Convert map to sorted list (most used apps first)
    val sortedList = usageData.toList()
        .sortedByDescending { it.second }
        .filter { it.second > 0 } // Remove apps with 0 usage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Today's App Usage",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            UsageBarGraph(sortedList)

            Spacer(modifier = Modifier.height(32.dp))
        }

        items(sortedList) { (packageName, timeInMillis) ->
            // Convert milliseconds to minutes
            val seconds = timeInMillis / 1000

            // Only show apps used for at least 1 minute
            var minutes = seconds / 60
            if (minutes > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        // App Name (Left Side)
                        text = getAppName(LocalContext.current, packageName),
                        //.substringAfterLast(".").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        // Time (Right Side)
                        text = "$minutes minute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider()
            }
            else if (seconds > 0){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        // App Name (Left Side)
                        text = getAppName(LocalContext.current, packageName),
                        //.substringAfterLast(".").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        // Time (Right Side)
                        text = "Less than a minute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider()
            }
            else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        // App Name (Left Side)
                        text = getAppName(LocalContext.current, packageName),
                        //.substringAfterLast(".").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        // Time (Right Side)
                        text = "0 minute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

/* ---------------- USAGE DATA LOGIC ---------------- */

fun getTodayUsage(context: Context): Map<String, Long> {

    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val calendar = Calendar.getInstance()

    // End time = now
    val endTime = calendar.timeInMillis

    // Start time = today 12:00 AM
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    val startTime = calendar.timeInMillis

    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
    val event = UsageEvents.Event()

    val appUsageMap = mutableMapOf<String, Long>()
    val lastForegroundTime = mutableMapOf<String, Long>()

    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)

        val packageName = event.packageName ?: continue

        when (event.eventType) {

            UsageEvents.Event.ACTIVITY_RESUMED -> {
                lastForegroundTime[packageName] = event.timeStamp
            }

            UsageEvents.Event.ACTIVITY_PAUSED -> {
                val start = lastForegroundTime[packageName]
                if (start != null) {
                    val duration = event.timeStamp - start
                    appUsageMap[packageName] =
                        (appUsageMap[packageName] ?: 0L) + duration
                }
            }
        }
    }

    return appUsageMap
}

@Composable
fun UsageBarGraph(usageData: List<Pair<String, Long>>) {

    // 1. Prepare Data: Filter top 5 apps > 1 minute
    // We create a "Model" that Vico understands
    val chartEntryModel = remember(usageData) {
        val filteredData = usageData
            .filter { it.second > 60 * 1000 }
            .take(5)

        // Convert to Vico "FloatEntry" (x = index, y = minutes)
        val entries = filteredData.mapIndexed { index, (packageName, millis) ->
            val minutes = millis / 1000f / 60f
            FloatEntry(x = index.toFloat(), y = minutes)
        }

        entryModelOf(entries)
    }

    // 2. Draw the Chart
    if (chartEntryModel.entries.isNotEmpty()) {
        Column(
            modifier = Modifier,
//                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Chart(
                // CHANGE COLOR HERE
                chart = columnChart(
                    columns = listOf(
                        lineComponent(
                            color = MaterialTheme.colorScheme.primary, //Bar Colour
                            thickness = 24.dp, // Controls bar width
                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(topLeft = 8.dp, topRight = 8.dp)
                        )
                    )
                ),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Take 90% of width (looks more centered than 100%)
                    .height(200.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    ScreenTrackerTheme {
        UsageDataScreen(LocalContext.current)
    }
}


