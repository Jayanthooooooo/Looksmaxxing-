package com.example.ui.barber

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BarberShopItemEntity
import com.example.ui.theme.SerifHeadline

@Composable
fun BarberShopScreen(viewModel: BarberShopViewModel, onNavigateBack: () -> Unit) {
    val items by viewModel.items.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SerifHeadline("Barber shop", fontSize = 24.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Hairstyles", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(300.dp)) {
            items(items.filter { it.type == "HAIRSTYLE" }) { item ->
                BarberItemCard(item, onEquip = { viewModel.equipItem(item.id) })
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Beards", style = MaterialTheme.typography.titleMedium)
        // Beard row...
    }
}

@Composable
fun BarberItemCard(item: BarberShopItemEntity, onEquip: () -> Unit) {
    Card(modifier = Modifier.padding(4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Canvas(modifier = Modifier.size(60.dp)) {
                drawCircle(color = Color.LightGray, style = Stroke(width = 2.dp.toPx()))
            }
            Text(item.name, fontSize = 12.sp)
            Button(onClick = onEquip, enabled = !item.isEquipped) {
                Text(if (item.isEquipped) "Equipped" else "Equip")
            }
        }
    }
}
