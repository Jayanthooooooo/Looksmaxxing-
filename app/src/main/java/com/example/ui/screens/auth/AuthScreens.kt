package com.example.ui.screens.auth

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onNavigateNext != null) {
        LaunchedEffect(Unit) {
            delay(1500)
            onNavigateNext()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Lumora Logo",
                tint = LumoraColors.ChampagneGold,
                modifier = Modifier.size(68.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            SerifHeadline(
                text = "Lumora",
                italicWord = "Lumora",
                fontSize = 36.sp,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            GoldEyebrow(text = "FACE & SKIN JOURNAL")
            Spacer(modifier = Modifier.height(36.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LumoraColors.ChampagneGold,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    firebaseConfigured: Boolean = true,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier.testTag("welcome_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                if (!firebaseConfigured) {
                    Surface(
                        color = Color(0x33F87171),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("firebase_unconfigured_banner")
                    ) {
                        Text(
                            text = "Sign-in unavailable: Firebase not configured",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Lumora Emblem",
                        tint = LumoraColors.ChampagneGold,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                GoldEyebrow(text = "LUMORA JOURNAL")
                Spacer(modifier = Modifier.height(12.dp))

                // Headline with ONE italic word per title: "Welcome back" with "back" italic
                SerifHeadline(
                    text = "Welcome back",
                    italicWord = "back",
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "See how your face and skin photograph over time privately and without judgment.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0x99F4F4F6), // 60% opacity muted text
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Pinned at the bottom: Primary button (Google), Secondary (Email), Create account, Terms · Privacy
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary button: light grey/white, dark text, 16dp radius
                PrimaryButton(
                    text = "Continue with Google",
                    onClick = onGetStarted,
                    testTag = "welcome_get_started"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary button: dark with a thin border
                SecondaryButton(
                    text = "Continue with email",
                    onClick = onSignIn,
                    testTag = "welcome_sign_in"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // "New here? Create your account"
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New here? ",
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = Color(0x99F4F4F6)
                    )
                    Text(
                        text = "Create your account",
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LumoraColors.ChampagneGold,
                        modifier = Modifier.clickable { onGetStarted() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Terms · Privacy
                Text(
                    text = "Terms · Privacy",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color(0x99F4F4F6).copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
