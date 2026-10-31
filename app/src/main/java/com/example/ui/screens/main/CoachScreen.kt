package com.example.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.StreamingMessageBubble
import com.example.ui.theme.LumoraColors

data class ChatMessage(
    val id: String,
    val role: String, // "user", "assistant"
    val text: String,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onReportMessage: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onConsentSettingsClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryMessage: (() -> Unit)? = null,

    isCloudAiConsentGranted: Boolean = true,
    isGenerating: Boolean = false,
    messagesUsedToday: Int = 0,
    dailyLimit: Int = 5,
    isPro: Boolean = false
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (!isCloudAiConsentGranted) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmptyState(
                title = "AI Coach is disabled",
                message = "The coach requires Cloud AI analysis consent to process your questions. You can enable it anytime in the Consent Centre or Privacy settings.",
                actionLabel = "Open Consent Settings",
                onAction = onConsentSettingsClick
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("coach_screen")
    ) {
        // Coach top bar with history button and limit meter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lumora AI Coach",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isPro) "PRO MEMBER" else "$messagesUsedToday / $dailyLimit messages used today",
                    style = MaterialTheme.typography.labelSmall,
                    color = LumoraColors.SecondaryCyan,
                    letterSpacing = 1.sp
                )
            }
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .testTag("chat_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Chat history",
                    tint = Color.White
                )
            }
        }

        LinearProgressIndicator(
            progress = { (messagesUsedToday.toFloat() / dailyLimit.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = if (messagesUsedToday >= dailyLimit) LumoraColors.ErrorCoral else LumoraColors.SecondaryCyan,
            trackColor = Color.White.copy(alpha = 0.05f)
        )

        // Suggestion chips when empty
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ask about routine consistency, lighting, or measurement context",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                val suggestions = listOf(
                    "How do I build a simple gentle routine?",
                    "What affects my symmetry reading?",
                    "How should I interpret typical ranges?",
                    "Can habits change bone structure?"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        FilterChip(
                            selected = false,
                            onClick = { onSendMessage(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    StreamingMessageBubble(
                        role = msg.role,
                        text = msg.text,
                        isStreaming = msg.isStreaming,
                        isError = msg.isError,
                        onReport = { onReportMessage(msg.id) },
                        onRetry = onRetryMessage
                    )
                }

            }
        }

        // Limit reached banner
        if (messagesUsedToday >= dailyLimit && !isPro) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily free coach limit reached ($dailyLimit messages)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upgrade to Pro for up to 100 messages/day and weekly AI progress reports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "View Pro plans",
                        onClick = onUpgradeClick,
                        modifier = Modifier.height(44.dp)
                    )
                }
            }
        } else {
            // Message input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask your coach...", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f).testTag("coach_message_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumoraColors.SecondaryCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = LumoraColors.SecondaryCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(12.dp))

                if (isGenerating) {
                    IconButton(
                        onClick = onStopGeneration,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LumoraColors.ErrorCoral.copy(alpha = 0.2f))
                            .testTag("coach_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop generation",
                            tint = LumoraColors.ErrorCoral
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val msg = inputText.trim()
                            if (msg.isNotBlank()) {
                                onSendMessage(msg)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) LumoraColors.PrimaryGradient else SolidColor(Color.White.copy(alpha = 0.05f)))
                            .testTag("coach_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0D1A)
@Composable
fun CoachScreenPreview() {
    val mockMessages = listOf(
        ChatMessage("1", "user", "How can I improve my skin symmetry?"),
        ChatMessage("2", "assistant", "Consistency in your evening routine is key! I recommend using a gentle cleanser followed by a hydrating serum.")
    )
    com.example.ui.theme.LumoraTheme {
        CoachScreen(
            messages = mockMessages,
            onSendMessage = {},
            onStopGeneration = {},
            onReportMessage = {},
            onHistoryClick = {},
            onConsentSettingsClick = {},
            onUpgradeClick = {}
        )
    }
}
