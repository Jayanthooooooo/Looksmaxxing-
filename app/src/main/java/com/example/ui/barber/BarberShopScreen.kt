package com.example.ui.barber

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BarberShopItemEntity
import com.example.ui.components.LumoraTopBar
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans

@Composable
fun BarberShopScreen(viewModel: BarberShopViewModel, onNavigateBack: () -> Unit) {
    val items by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Barber Shop",
                onBackClick = onNavigateBack
            )
        },
        containerColor = LumoraColors.DarkBackground,
        modifier = Modifier.testTag("barber_shop_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GoldEyebrow("GROOMING & STYLING")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Grooming Style Checklist",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = "Explore haircut and beard options suited for your facial structure. Mark styles as tried to track your personal grooming routine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }

            items(items, key = { it.id }) { item ->
                BarberItemCard(
                    item = item,
                    onToggle = { viewModel.toggleTriedStatus(item.id, item.isEquipped) }
                )
            }
        }
    }
}

@Composable
fun BarberItemCard(item: BarberShopItemEntity, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(
            1.dp,
            if (item.isEquipped) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("barber_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (item.isEquipped) Color(0x28D9C59B) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isEquipped) Icons.Default.Check else Icons.Default.ContentCut,
                    contentDescription = null,
                    tint = if (item.isEquipped) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.type,
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumoraColors.ChampagneGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = item.name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = item.isEquipped,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = LumoraColors.ChampagneGold,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("toggle_${item.id}")
                )
                Text(
                    text = if (item.isEquipped) "Tried" else "Mark tried",
                    fontSize = 10.sp,
                    color = if (item.isEquipped) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}
