package com.example.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans

enum class MainTab(val label: String, val icon: ImageVector) {
    TODAY("Home", Icons.Default.Home),
    PROGRESS("Progress", Icons.AutoMirrored.Filled.ShowChart),
    COACH("Coach", Icons.Default.Chat),
    YOU("Profile", Icons.Default.Person)
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
                containerColor = LumoraColors.DarkSurface,
                header = {
                    FloatingActionButton(
                        onClick = onCenterScanClick,
                        shape = CircleShape,
                        containerColor = Color(0xFFF0F0F3),
                        contentColor = Color(0xFF0B0B0F),
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
                // Outer container for floating bottom bar and floating gold AI button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Floating rounded bottom bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(16.dp, RoundedCornerShape(32.dp))
                            .testTag("main_bottom_nav"),
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF141419).copy(alpha = 0.94f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Home tab
                            NavTabButton(
                                label = "Home",
                                icon = Icons.Default.Home,
                                isSelected = currentTab == MainTab.TODAY,
                                onClick = { onTabSelected(MainTab.TODAY) },
                                testTag = "nav_tab_today"
                            )

                            // Progress tab
                            NavTabButton(
                                label = "Progress",
                                icon = Icons.AutoMirrored.Filled.ShowChart,
                                isSelected = currentTab == MainTab.PROGRESS,
                                onClick = { onTabSelected(MainTab.PROGRESS) },
                                testTag = "nav_tab_progress"
                            )

                            // Profile tab
                            NavTabButton(
                                label = "Profile",
                                icon = Icons.Default.Person,
                                isSelected = currentTab == MainTab.YOU,
                                onClick = { onTabSelected(MainTab.YOU) },
                                testTag = "nav_tab_you"
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Separate light pill button "Scan" on the right
                            Surface(
                                onClick = onCenterScanClick,
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF0F0F3), // light pill button
                                contentColor = Color(0xFF0B0B0F),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .testTag("center_scan_fab")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Scan",
                                        tint = Color(0xFF0B0B0F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Scan",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0B0B0F)
                                    )
                                }
                            }
                        }
                    }

                    // Small round gold "AI" button floating above the bar on the right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 22.dp)
                            .offset(y = (-20).dp)
                            .size(44.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFD9C59B)) // champagne gold
                            .border(
                                width = if (currentTab == MainTab.COACH) 2.dp else 1.dp,
                                color = if (currentTab == MainTab.COACH) Color.White else Color(0xFFF2E6CE),
                                shape = CircleShape
                            )
                            .clickable { onTabSelected(MainTab.COACH) }
                            .testTag("nav_tab_coach"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AI",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0B0B0F)
                        )
                    }
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.testTag("main_scaffold_compact")
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun NavTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFD9C59B) else Color(0x99F4F4F6),
        label = "tab_color"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = activeColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = activeColor
        )
    }
}
