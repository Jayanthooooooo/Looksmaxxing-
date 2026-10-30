package com.example.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class MainTab(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Default.Today),
    PROGRESS("Progress", Icons.Default.ShowChart),
    COACH("Coach", Icons.Default.Chat),
    YOU("You", Icons.Default.Person)
}

@Composable
fun MainScaffold(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onCenterScanClick: () -> Unit,
    isExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize().testTag("main_scaffold_expanded")) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    FloatingActionButton(
                        onClick = onCenterScanClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(vertical = 12.dp).testTag("rail_scan_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Scan")
                    }
                }
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = currentTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth().testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.TODAY,
                            onClick = { onTabSelected(MainTab.TODAY) },
                            icon = { Icon(MainTab.TODAY.icon, contentDescription = "Today") },
                            label = { Text("Today") },
                            modifier = Modifier.testTag("nav_tab_today")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.PROGRESS,
                            onClick = { onTabSelected(MainTab.PROGRESS) },
                            icon = { Icon(MainTab.PROGRESS.icon, contentDescription = "Progress") },
                            label = { Text("Progress") },
                            modifier = Modifier.testTag("nav_tab_progress")
                        )

                        // Spacer item for center FAB
                        NavigationBarItem(
                            selected = false,
                            onClick = {},
                            enabled = false,
                            icon = { Box(modifier = Modifier.size(24.dp)) },
                            label = { Text("") }
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.COACH,
                            onClick = { onTabSelected(MainTab.COACH) },
                            icon = { Icon(MainTab.COACH.icon, contentDescription = "Coach") },
                            label = { Text("Coach") },
                            modifier = Modifier.testTag("nav_tab_coach")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.YOU,
                            onClick = { onTabSelected(MainTab.YOU) },
                            icon = { Icon(MainTab.YOU.icon, contentDescription = "You") },
                            label = { Text("You") },
                            modifier = Modifier.testTag("nav_tab_you")
                        )
                    }

                    // Centre Floating Action Button
                    FloatingActionButton(
                        onClick = onCenterScanClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-20).dp)
                            .size(56.dp)
                            .testTag("center_scan_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Scan",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            modifier = Modifier.testTag("main_scaffold_compact")
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                content()
            }
        }
    }
}
