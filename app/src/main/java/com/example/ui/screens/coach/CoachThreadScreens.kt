package com.example.ui.screens.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ChatThreadEntity
import com.example.ui.components.AppBottomSheet
import com.example.ui.components.EmptyState
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuietTextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryScreen(
    threads: List<ChatThreadEntity>,
    onSelectThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onClearAllThreads: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Chat History",
                onBackClick = onBackClick,
                actions = {
                    if (threads.isNotEmpty()) {
                        QuietTextButton(text = "Clear all", onClick = onClearAllThreads)
                    }
                }
            )
        },
        modifier = modifier.testTag("chat_history_screen")
    ) { padding ->
        if (threads.isEmpty()) {
            EmptyState(
                title = "No conversations yet",
                message = "Your conversation threads with the AI coach will appear here.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(threads, key = { it.id }) { thread ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectThread(thread.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = thread.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.US).format(Date(thread.updatedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteThread(thread.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete thread",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportReplySheet(
    onDismiss: () -> Unit,
    onSubmitReport: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReason by remember { mutableStateOf("Inaccurate") }
    var note by remember { mutableStateOf("") }
    val reasons = listOf("Unsafe", "Inaccurate", "Rude/shaming", "Medical overreach", "Other")

    AppBottomSheet(onDismiss = onDismiss) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = "Report AI Response",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lumora enforces strict guidelines: no diagnosis, no body shaming, and clear distinction between bone structure and changeable features.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reasons.forEach { reason ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(reason) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Additional notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Submit Report",
                onClick = {
                    onSubmitReport(selectedReason, note.trim())
                    onDismiss()
                },
                testTag = "submit_report_button"
            )
        }
    }
}
