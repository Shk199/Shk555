package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MorningEveningCategory
import com.example.data.model.MorningEveningItemState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundVariant
import com.example.ui.theme.CardBorder
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UthmanicFontFamily

@Composable
fun MorningEveningScreen(
    morningAzkar: List<MorningEveningItemState>,
    eveningAzkar: List<MorningEveningItemState>,
    isReminderEnabled: Boolean,
    repeatInterval: Int, // 0: once, 30: 30m, 60: 1h
    morningHour: Int,
    morningMinute: Int,
    eveningHour: Int,
    eveningMinute: Int,
    onIncrementDhikr: (Int, MorningEveningCategory) -> Unit,
    onResetDhikr: (Int, MorningEveningCategory) -> Unit,
    onResetCategory: (MorningEveningCategory) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSelectRepeat: (Int) -> Unit,
    onTestReminder: (Boolean) -> Unit = {},
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(MorningEveningCategory.MORNING) }
    var showReminderSettings by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleReminder(true)
        }
    }

    fun handleToggle(newValue: Boolean) {
        if (newValue && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        onToggleReminder(newValue)
    }

    val activeList = if (selectedCategory == MorningEveningCategory.MORNING) morningAzkar else eveningAzkar
    val completedCount = activeList.count { it.isCompleted }
    val totalCount = activeList.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("morning_evening_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Tabs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CardBackgroundVariant, CardBackground)
                        )
                    )
                    .border(1.dp, CardBorder, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MutedPurple)
                                .border(1.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbTwilight,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "أذكار الصباح والمساء",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "مأثورة من حصن المسلم بالفضل والتخريج",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    if (onNavigateToSettings != null) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MutedPurple)
                                .border(1.dp, CardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الإعدادات",
                                tint = PrimaryPurpleLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Modern Tab Row
                TabRow(
                    selectedTabIndex = if (selectedCategory == MorningEveningCategory.MORNING) 0 else 1,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryPurple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(
                                tabPositions[if (selectedCategory == MorningEveningCategory.MORNING) 0 else 1]
                            ),
                            color = AccentGold,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedCategory == MorningEveningCategory.MORNING,
                        onClick = { selectedCategory = MorningEveningCategory.MORNING },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (selectedCategory == MorningEveningCategory.MORNING) AccentGold else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "أذكار الصباح",
                                    fontWeight = if (selectedCategory == MorningEveningCategory.MORNING) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategory == MorningEveningCategory.MORNING) AccentGold else TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_morning")
                    )

                    Tab(
                        selected = selectedCategory == MorningEveningCategory.EVENING,
                        onClick = { selectedCategory = MorningEveningCategory.EVENING },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = null,
                                    tint = if (selectedCategory == MorningEveningCategory.EVENING) AccentGold else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "أذكار المساء",
                                    fontWeight = if (selectedCategory == MorningEveningCategory.EVENING) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategory == MorningEveningCategory.EVENING) AccentGold else TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_evening")
                    )
                }
            }
        }

        // 2. Progress and Achievement Card ("أنجزت X من Y")
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (completedCount == totalCount && totalCount > 0) Icons.Default.DoneAll else Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = if (completedCount == totalCount && totalCount > 0) Color(0xFF4CAF50) else AccentGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أنجزت $completedCount من $totalCount ذكراً",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (completedCount == totalCount && totalCount > 0) Color(0xFF4CAF50) else PrimaryPurple,
                        trackColor = MutedPurple
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == MorningEveningCategory.MORNING)
                                "التوقيت المستحب: بعد صلاة الفجر إلى الضحى"
                            else
                                "التوقيت المستحب: بعد صلاة العصر إلى الغروب",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )

                        if (completedCount > 0) {
                            Text(
                                text = "إعادة تعيين اليوم",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryPurpleLight,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable { onResetCategory(selectedCategory) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Smart Reminder Card with Toggle, Timing Info & Frequency
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MutedPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = PrimaryPurpleLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تذكير بأذكار الصباح والمساء",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = if (isReminderEnabled) "التنبيه مفعل في الأوقات المباركة" else "التنبيه متوقف",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isReminderEnabled) Color(0xFF4CAF50) else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { handleToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = MutedPurple
                            ),
                            modifier = Modifier.testTag("me_toggle_switch")
                        )
                    }

                    // Expandable details when enabled
                    AnimatedVisibility(visible = isReminderEnabled) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            // Smart Schedule info
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MutedPurple.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "⏰ المواقيت الذكية للتذكير:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• الصباح: ${String.format("%02d:%02d", morningHour, morningMinute)} ص (بين الفجر والضحى)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp)
                                    )
                                    Text(
                                        text = "• المساء: ${String.format("%02d:%02d", eveningHour, eveningMinute)} م (بين العصر والمغرب)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "خيارات التكرار أثناء الفترة:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val repeatOptions = listOf(
                                0 to "مرة واحدة فقط",
                                60 to "كل ساعة",
                                30 to "كل 30 دقيقة"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeatOptions.forEach { (interval, label) ->
                                    val isSelected = repeatInterval == interval
                                    val buttonBg by animateColorAsState(
                                        targetValue = if (isSelected) PrimaryPurple else CardBackgroundVariant,
                                        label = "btnBg"
                                    )
                                    val textColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else TextSecondary,
                                        label = "txtColor"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(buttonBg)
                                            .border(
                                                1.dp,
                                                if (isSelected) PrimaryPurpleLight else CardBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onSelectRepeat(interval) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = textColor,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. List of Authentic Dhikr Items from Hisn al-Muslim
        items(
            items = activeList,
            key = { it.dhikr.id }
        ) { item ->
            MorningEveningDhikrCard(
                itemState = item,
                category = selectedCategory,
                onIncrement = { onIncrementDhikr(item.dhikr.id, selectedCategory) },
                onReset = { onResetDhikr(item.dhikr.id, selectedCategory) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MorningEveningDhikrCard(
    itemState: MorningEveningItemState,
    category: MorningEveningCategory,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dhikr = itemState.dhikr
    val isCompleted = itemState.isCompleted

    val cardBorderColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFF4CAF50) else CardBorder,
        label = "borderColor"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(if (isCompleted) 1.5.dp else 1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 2.dp else 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("me_card_${dhikr.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Card Top Row: Title & Target Badge & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.2f) else MutedPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dhikr.orderIndex}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) Color(0xFF4CAF50) else AccentGold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dhikr.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Target Badge (شارة عدد المرات - تتكيف أفقياً ولا تُجبر على الالتفاف العمودي)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBackgroundVariant)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (dhikr.targetCount == 1) "مرة واحدة" else "${dhikr.targetCount} مرات",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            softWrap = false,
                            maxLines = 1
                        )
                    }

                    if (itemState.currentCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة تعيين",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Full Arabic Text in Uthmanic Font
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackgroundVariant.copy(alpha = 0.7f))
                    .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = dhikr.arabicText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = UthmanicFontFamily,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Virtue (الفضل الشرعي)
            if (dhikr.virtue.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "الفضل: ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = dhikr.virtue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Takhrij (التخريج)
            if (dhikr.takhrij.isNotBlank()) {
                Text(
                    text = "التخريج: ${dhikr.takhrij}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bottom Action: "قرأت هذا الذكر" with Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today's Progress Badge
                Text(
                    text = if (isCompleted) "تم الإنجاز بالكامل ✓" else "أنجزت: ${itemState.currentCount} / ${dhikr.targetCount}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF4CAF50) else PrimaryPurpleLight,
                        fontSize = 13.sp
                    )
                )

                // Read button
                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) Color(0xFF388E3C) else PrimaryPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_read_${dhikr.id}")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompleted) "أعد القراءة (+1)" else "قرأت هذا الذكر",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
