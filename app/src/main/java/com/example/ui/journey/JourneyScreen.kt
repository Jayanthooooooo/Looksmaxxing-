package com.example.ui.journey

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SkillNodeEntity
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline

@Composable
fun JourneyScreen(viewModel: JourneyViewModel, onNavigateBack: () -> Unit) {
    val nodes by viewModel.skillNodes.collectAsState()
    val unlockedCount = nodes.count { it.status == "DONE" }
    
    Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp).verticalScroll(rememberScrollState())) {
        GoldEyebrow("SKILL TREE")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SerifHeadline("Choose Your Path", italicWord = "Path", fontSize = 24.sp)
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0x28D9C59B)) {
                Text("${nodes.size - unlockedCount} points left", modifier = Modifier.padding(8.dp), color = LumoraColors.ChampagneGold, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(progress = { unlockedCount / 15f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = LumoraColors.ChampagneGold)
        Text("Unlocked $unlockedCount / 15 skills", fontSize = 12.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        nodes.forEach { node ->
            SkillNodeItem(node, onClick = { viewModel.unlockSkill(node.id) })
        }
    }
}

@Composable
fun SkillNodeItem(node: SkillNodeEntity, onClick: () -> Unit) {
    val color = when(node.status) {
        "DONE" -> Color.Green
        "IN_PROGRESS" -> LumoraColors.ChampagneGold
        else -> Color.Gray
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onClick)) {
        Box(modifier = Modifier.size(40.dp).border(2.dp, color, CircleShape), contentAlignment = Alignment.Center) {
            if (node.status == "DONE") Text("✓", color = color)
            else if (node.status == "IN_PROGRESS") Box(modifier = Modifier.size(16.dp).background(color, CircleShape))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(node.name, fontWeight = FontWeight.Bold)
            Text(node.description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
