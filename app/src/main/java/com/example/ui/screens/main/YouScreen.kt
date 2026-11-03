package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserProfile
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans

@Composable
fun YouScreen(
    userProfile: UserProfile,
    email: String,
    isPro: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivacyCentre: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit,
    onNavigateToJourney: () -> Unit = {},
    onNavigateToBarberShop: () -> Unit = {},
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = userProfile.displayName.ifBlank { "Lumora User" }
    val initial = displayName.trim().firstOrNull()?.uppercase() ?: "L"

    val genderText = when (userProfile.referenceRangePref.lowercase()) {
        "female_typical" -> "Female"
        "male_typical" -> "Male"
        "neutral" -> "Neutral"
        else -> "Neutral"
    }
    val birthYearText = userProfile.birthYear?.toString() ?: "—"
    val heightText = userProfile.heightCm?.let { "${it.toInt()} cm" } ?: "—"

    val (skinTypeTitle, skinTypeIcon) = when (userProfile.skinType.lowercase()) {
        "normal", "balanced" -> "Balanced" to Icons.Default.WbSunny
        "oily" -> "Oily / shine" to Icons.Default.WaterDrop
        "acne" -> "Acne-prone" to Icons.Default.Grain
        "dry" -> "Dry / dehydrated" to Icons.Default.Spa
        "sensitive" -> "Sensitive / redness" to Icons.Default.FavoriteBorder
        "marks" -> "Dark spots / scars" to Icons.Default.BrightnessLow
        else -> "Balanced" to Icons.Default.WbSunny
    }

    val firstConcern = userProfile.concerns.firstOrNull()
    val concernTitle = if (firstConcern != null) {
        firstConcern.replace("_", " ").replaceFirstChar { it.uppercase() }
    } else {
        "Daily hydration & barrier"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
            .testTag("you_screen")
    ) {
        // Eyebrow "PROFILE"
        GoldEyebrow(text = "PROFILE")
        Spacer(modifier = Modifier.height(20.dp))

        // Large circle avatar with the user's initial in serif
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0x28D9C59B))
                    .border(1.5.dp, LumoraColors.ChampagneGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontFamily = Fraunces,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = LumoraColors.ChampagneGold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                // Name in serif
                Text(
                    text = displayName,
                    fontFamily = Fraunces,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFF4F4F6)
                )

                if (email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    // Email below in 60% opacity muted text
                    Text(
                        text = email,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = Color(0x99F4F4F6)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Two chips: FREE and RANK: NEWBIE
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Tier chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isPro) Color(0x28D9C59B) else Color(0x18FFFFFF),
                border = BorderStroke(1.dp, if (isPro) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.12f))
            ) {
                Text(
                    text = if (isPro) "PRO" else "FREE",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = if (isPro) LumoraColors.ChampagneGold else Color(0xFFF4F4F6),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Rank chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x24D9C59B),
                border = BorderStroke(1.dp, LumoraColors.ChampagneGold)
            ) {
                Text(
                    text = "RANK: NEWBIE",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = LumoraColors.ChampagneGold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "ABOUT YOU" card with 3 columns: GENDER, BIRTH YEAR, HEIGHT (serif values)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                GoldEyebrow(text = "ABOUT YOU")
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "GENDER",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = Color(0x99F4F4F6)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = genderText,
                            fontFamily = Fraunces,
                            fontSize = 18.sp,
                            color = Color(0xFFF4F4F6)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BIRTH YEAR",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = Color(0x99F4F4F6)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = birthYearText,
                            fontFamily = Fraunces,
                            fontSize = 18.sp,
                            color = Color(0xFFF4F4F6)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "HEIGHT",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = Color(0x99F4F4F6)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = heightText,
                            fontFamily = Fraunces,
                            fontSize = 18.sp,
                            color = Color(0xFFF4F4F6)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "SKIN" card with 2 rows: skin type and skin concern, each with an icon
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                GoldEyebrow(text = "SKIN")
                Spacer(modifier = Modifier.height(14.dp))

                // Row 1: Skin type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22D9C59B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = skinTypeIcon,
                            contentDescription = null,
                            tint = LumoraColors.ChampagneGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Skin type",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0x99F4F4F6)
                        )
                        Text(
                            text = skinTypeTitle,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFFF4F4F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Skin concern
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22D9C59B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = LumoraColors.ChampagneGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Skin concern",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0x99F4F4F6)
                        )
                        Text(
                            text = concernTitle,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFFF4F4F6)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Below: Skill Tree, Barber Shop, Settings, Privacy centre, Delete account, Sign out rows
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Skill Tree row
            ActionMenuRow(
                title = "Skill Tree",
                icon = Icons.Default.AccountTree,
                onClick = onNavigateToJourney,
                testTag = "menu_skill_tree"
            )

            // Barber Shop row
            ActionMenuRow(
                title = "Barber Shop",
                icon = Icons.Default.ContentCut,
                onClick = onNavigateToBarberShop,
                testTag = "menu_barber_shop"
            )

            // Settings row
            ActionMenuRow(
                title = "Settings",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                testTag = "menu_settings"
            )

            // Privacy centre row
            ActionMenuRow(
                title = "Privacy centre",
                icon = Icons.Default.Lock,
                onClick = onNavigateToPrivacyCentre,
                testTag = "menu_privacy_centre"
            )

            // Delete account row
            ActionMenuRow(
                title = "Delete account",
                icon = Icons.Default.DeleteForever,
                textColor = Color(0xFFF87171),
                iconTint = Color(0xFFF87171),
                onClick = onNavigateToDeleteAccount,
                testTag = "menu_delete_account"
            )

            // Sign out row
            ActionMenuRow(
                title = "Sign out",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                textColor = Color(0x99F4F4F6),
                iconTint = Color(0x99F4F4F6),
                onClick = onSignOut,
                testTag = "menu_sign_out"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ActionMenuRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    textColor: Color = Color(0xFFF4F4F6),
    iconTint: Color = LumoraColors.ChampagneGold
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0x66F4F4F6),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
