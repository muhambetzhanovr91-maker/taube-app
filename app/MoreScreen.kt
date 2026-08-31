package kz.taube.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MenuItemData(
    val id: Int,
    val icon: String,
    val title: String
)

@Composable
fun MoreScreen() {
    val menuItems = listOf(
        MenuItemData(1, "📅", "Ислам күнтізбесі"),
        MenuItemData(2, "🌙", "Ораза уақыты"),
        MenuItemData(3, "📖", "Құран оқу жоспары"),
        MenuItemData(4, "🕌", "Намазды үйрену"),
        MenuItemData(5, "📜", "Хадис күнделігі"),
        MenuItemData(6, "🤲", "Дұға кітапшасы"),
        MenuItemData(7, "🔖", "Бетбелгілер"),
        MenuItemData(8, "⚙️", "Параметрлер")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Жоғарғы тақырып
        Text(
            text = "Қосымша",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Меню элементтерінің тізімі
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(menuItems) { item ->
                MoreRowCard(item)
            }
        }
    }
}

@Composable
fun MoreRowCard(item: MenuItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Тиісті бөлімді ашу */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
