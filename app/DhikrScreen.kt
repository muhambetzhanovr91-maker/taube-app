package kz.taube.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DhikrItem(
    val id: Int,
    val title: String,
    val description: String,
    val targetCount: Int,
    var currentCount: Int = 0
)

@Composable
fun DhikrScreen() {
    var selectedCategory by remember { mutableIntStateOf(0) }

    val dhikrList = remember {
        mutableStateListOf(
            DhikrItem(1, "Аят әл-Курси", "Бақара сүресі, 255-аят", 1),
            DhikrItem(2, "3 Құл (Ихлас, Фалақ, Нас)", "3 реттен оқу", 3),
            DhikrItem(3, "Тәубе истиғфар", "Астағфируллаһ әл-Азим", 100),
            DhikrItem(4, "Субханаллаһ", "33 рет", 33),
            DhikrItem(5, "Әлхамдулиллаһ", "33 рет", 33),
            DhikrItem(6, "Аллаһу Акбар", "33 рет", 33),
            DhikrItem(7, "Ла илаһа иллаллаһ", "100 рет", 100)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Жоғарғы тақырып
        Text(
            text = "Зікір",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Таңғы зікір баннері (Hero Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(
                        text = "Таңғы зікір",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Күн сайын оқылатын таңғы дұғалар",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Бөлім түймелері (Быстрый выбор)
        val categories = listOf("Таңғы", "Кешкі", "Тасбих", "Дұғалар", "99 есім")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEachIndexed { index, cat ->
                FilterChip(
                    selected = selectedCategory == index,
                    onClick = { selectedCategory = index },
                    label = { Text(cat, fontSize = 11.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0D5C3A),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF64748B)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Таңғы зікірлер",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Зікірлер тізімі
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(dhikrList) { item ->
                DhikrRowCard(
                    item = item,
                    onIncrement = {
                        val index = dhikrList.indexOf(item)
                        if (index != -1 && item.currentCount < item.targetCount) {
                            dhikrList[index] = item.copy(currentCount = item.currentCount + 1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DhikrRowCard(
    item: DhikrItem,
    onIncrement: () -> Unit
) {
    val isCompleted = item.currentCount >= item.targetCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIncrement() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFE6F4ED) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF0D5C3A) else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Санау түймесі (Counter Badge)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isCompleted) Color(0xFF0D5C3A) else Color(0xFFF1F5F9),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else "${item.currentCount}/${item.targetCount}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color.White else Color(0xFF0F172A)
                )
            }
        }
    }
}
