package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumoraColors

@Composable
fun StreamingMessageBubble(
    role: String,
    text: String,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    isError: Boolean = false,
    onCopy: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null
) {
    val isUser = role == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgBrush = if (isUser) LumoraColors.PrimaryGradient else SolidColor(LumoraColors.GlassBackground)
    val borderBrush = if (isUser) SolidColor(Color.Transparent) else LumoraColors.GlassBorder
    val textColor = if (isUser) Color.White else Color.White.copy(alpha = 0.9f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(bgBrush)
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .testTag(if (isUser) "user_message_bubble" else "assistant_message_bubble")
        ) {
            Text(
                text = text + if (isStreaming) " ▋" else "",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                lineHeight = 24.sp
            )
        }

        if (isError && onRetry != null) {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 2.dp).testTag("chat_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry message",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        } else if (!isUser && text.isNotBlank() && !isStreaming) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (onReport != null) {
                    IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
