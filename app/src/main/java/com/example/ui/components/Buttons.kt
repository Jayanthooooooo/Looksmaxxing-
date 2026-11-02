package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.util.Haptics
import com.example.ui.util.LocalHapticsEnabled
import com.example.ui.util.pressScaleEffect

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = "primary_button"
) {
    val context = LocalContext.current
    val hapticSetting = LocalHapticsEnabled.current

    Button(
        onClick = {
            if (hapticSetting) {
                Haptics.tap(context)
            }
            onClick()
        },
        modifier = modifier
            .pressScaleEffect(enabled = enabled && !isLoading)
            .fillMaxWidth()
            .height(54.dp)
            .testTag(testTag),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF0F0F3),
            contentColor = Color(0xFF0B0B0F),
            disabledContainerColor = Color(0xFFF0F0F3).copy(alpha = 0.25f),
            disabledContentColor = Color(0xFF0B0B0F).copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF0B0B0F),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = "secondary_button"
) {
    val context = LocalContext.current
    val hapticSetting = LocalHapticsEnabled.current

    OutlinedButton(
        onClick = {
            if (hapticSetting) {
                Haptics.tap(context)
            }
            onClick()
        },
        modifier = modifier
            .pressScaleEffect(enabled = enabled && !isLoading)
            .fillMaxWidth()
            .height(54.dp)
            .testTag(testTag),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0x12FFFFFF),
            contentColor = Color(0xFFF4F4F6),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.25f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFFD9C59B),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun QuietTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "quiet_text_button"
) {
    val context = LocalContext.current
    val hapticSetting = LocalHapticsEnabled.current

    TextButton(
        onClick = {
            if (hapticSetting) {
                Haptics.tap(context)
            }
            onClick()
        },
        modifier = modifier
            .pressScaleEffect(enabled = enabled)
            .testTag(testTag),
        enabled = enabled
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color(0x99F4F4F6)
        )
    }
}
