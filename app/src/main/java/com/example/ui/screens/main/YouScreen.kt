package com.example.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.MenuNavigationCard
import com.example.ui.components.PrivacyBadge
import com.example.ui.theme.LumoraColors

@Composable
fun YouScreen(
    displayName: String,
    email: String,
    isPro: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivacyCentre: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("you_screen")
    ) {
        Text(
            text = "Your Profile",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LumoraColors.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = if (displayName.isNotBlank()) displayName else "Lumora User",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPro) LumoraColors.PrimaryGradient else androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.1f)))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPro) "PRO MEMBER" else "FREE TIER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isPro) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Management & Security",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MenuNavigationCard(
                title = "Privacy Centre",
                subtitle = "Photo retention, biometric lock, data export",
                icon = Icons.Default.Lock,
                onClick = onNavigateToPrivacyCentre,
                testTag = "menu_privacy_centre"
            )

            MenuNavigationCard(
                title = "Subscription & Plans",
                subtitle = if (isPro) "Active Lumora Pro plan" else "View Pro features and pricing",
                icon = Icons.Default.CardMembership,
                onClick = onNavigateToSubscription,
                testTag = "menu_subscription"
            )

            MenuNavigationCard(
                title = "Settings",
                subtitle = "Appearance, notifications, legal, sign out",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                testTag = "menu_settings"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        PrivacyBadge()
        Spacer(modifier = Modifier.height(40.dp))
    }
}
