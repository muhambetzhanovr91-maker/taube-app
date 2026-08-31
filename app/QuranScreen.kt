package kz.taube.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Surah(
    val id: Int,
    val nameKhazakh: String,
    val nameArabic: String,
    val origin: String,
    val versesCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val surahList = remember {
        listOf(
            Surah(1, "Әл-Фатиха", "الفاتحة", "Меккеде түскен", 7),
            Surah(2, "Әл-Бақара", "البقرة", "Мединада түскен", 286),
            Surah(3, "Әлі Имран", "آل عمران", "Мединада түскен", 200),
            Surah(4, "Ан-Ниса", "النساء", "Мединада түскен", 176),
            Surah(5, "Әл-Маида", "المائدة", "Мединада түскен", 120),
            Surah(6, "Әл-Анғам", "الأنعام", "Меккеде түскен", 165),
            Surah(7, "Әл-Ағраф", "الأعراف", "Меккеде түскен", 206),
            Surah(8, "Әл-Анфал", "الأنفال", "Мединада түскен", 75),
            Surah(9, "Ат-Тәубе", "التوبة", "Мединада түскен", 129),
            Surah(10, "Юнус", "يونس", "Меккеде түскен", 109)
        )
    }

    val filteredSurahs = surahList.filter {
        it.nameKhazakh.contains(searchQuery, ignoreCase = true)
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
            text = "Құран",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Іздеу жолы
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Сүре немесе аят іздеу...", fontSize = 14.sp, color = Color(0xFF94A3B8)) },
            leadingIcon = { Text("🔍", fontSize = 16.sp) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF0D5C3A),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                containerColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Соңғы оқылған жер (Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "СОҢҒЫ ОҚЫЛҒАН",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Әл-Бақара сүресі",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Аят 255 (Аят әл-Курси)",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Button(
                        onClick = { /* Аятты ашу */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C3A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Жалғастыру", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Тобтар (Сүрелер / Паралар / Сақталған)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Сүрелер", "Паралар", "Сақталған").forEachIndexed { index, title ->
                TextButton(
                    onClick = { selectedTab = index }
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) Color(0xFF0D5C3A) else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Сүрелер тізімі
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(filteredSurahs) { surah ->
                SurahRowItem(surah)
            }
        }
    }
}

@Composable
fun SurahRowItem(surah: Surah) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Сүреге өту */ },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Реттік нөмірі (Шеңбер ішінде)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE6F4ED), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.id.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D5C3A)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = surah.nameKhazakh,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${surah.origin} • ${surah.versesCount} аят",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Арабша атауы
            Text(
                text = surah.nameArabic,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D5C3A)
            )
        }
    }
}
