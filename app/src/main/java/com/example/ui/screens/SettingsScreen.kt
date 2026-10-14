package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubble.SmartTasbeehManager
import com.example.reminder.ReminderPermissionHelper
import com.example.ui.components.ExactAlarmPermissionDialog
import com.example.ui.components.NotificationDeniedDialog
import com.example.data.model.DailyStat
import com.example.data.model.DhikrItem
import com.example.reminder.SalawatAudioPlayer
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

/**
 * الوجهات الفرعية لصفحة الإعدادات
 */
enum class SettingsDestination {
    MAIN,
    STATS,
    SALAWAT_REMINDER,
    MORNING_EVENING_REMINDER,
    COUNTING_OPTIONS,
    ABOUT
}

@Composable
fun SettingsScreen(
    // 1. Stats data
    todayTotal: Int,
    lifetimeTotal: Int,
    recentStats: List<DailyStat>,
    azkarList: List<DhikrItem>,
    // 2. Salawat settings
    isSalawatEnabled: Boolean,
    salawatInterval: Int,
    onToggleSalawat: (Boolean) -> Unit,
    onSelectSalawatInterval: (Int) -> Unit,
    onTestSalawatReminder: () -> Unit,
    // 3. Morning / Evening settings
    isMorningEveningEnabled: Boolean,
    meRepeatInterval: Int,
    morningHour: Int,
    morningMinute: Int,
    eveningHour: Int,
    eveningMinute: Int,
    onToggleMorningEveningReminder: (Boolean) -> Unit,
    onSelectMorningEveningRepeat: (Int) -> Unit,
    onTestMorningEveningReminder: (Boolean) -> Unit,
    // 4. Counting Options
    isShakeToCountEnabled: Boolean,
    isVolumeKeysToCountEnabled: Boolean,
    onToggleShakeToCount: (Boolean) -> Unit,
    onToggleVolumeKeysToCount: (Boolean) -> Unit,
    // Navigation
    currentDestination: SettingsDestination = SettingsDestination.MAIN,
    onDestinationChange: (SettingsDestination) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (currentDestination) {
        SettingsDestination.MAIN -> {
            MainSettingsList(
                todayTotal = todayTotal,
                isSalawatEnabled = isSalawatEnabled,
                salawatInterval = salawatInterval,
                isMorningEveningEnabled = isMorningEveningEnabled,
                morningHour = morningHour,
                morningMinute = morningMinute,
                eveningHour = eveningHour,
                eveningMinute = eveningMinute,
                isShakeToCountEnabled = isShakeToCountEnabled,
                isVolumeKeysToCountEnabled = isVolumeKeysToCountEnabled,
                onNavigate = onDestinationChange,
                modifier = modifier
            )
        }
        SettingsDestination.STATS -> {
            StatsScreen(
                todayTotal = todayTotal,
                lifetimeTotal = lifetimeTotal,
                recentStats = recentStats,
                azkarList = azkarList,
                onBack = { onDestinationChange(SettingsDestination.MAIN) },
                modifier = modifier
            )
        }
        SettingsDestination.SALAWAT_REMINDER -> {
            SalawatReminderSubScreen(
                isEnabled = isSalawatEnabled,
                interval = salawatInterval,
                onToggle = onToggleSalawat,
                onSelectInterval = onSelectSalawatInterval,
                onTestAudio = onTestSalawatReminder,
                onBack = { onDestinationChange(SettingsDestination.MAIN) },
                modifier = modifier
            )
        }
        SettingsDestination.MORNING_EVENING_REMINDER -> {
            MorningEveningReminderSubScreen(
                isEnabled = isMorningEveningEnabled,
                repeatInterval = meRepeatInterval,
                morningHour = morningHour,
                morningMinute = morningMinute,
                eveningHour = eveningHour,
                eveningMinute = eveningMinute,
                onToggle = onToggleMorningEveningReminder,
                onSelectRepeat = onSelectMorningEveningRepeat,
                onTestReminder = onTestMorningEveningReminder,
                onBack = { onDestinationChange(SettingsDestination.MAIN) },
                modifier = modifier
            )
        }
        SettingsDestination.COUNTING_OPTIONS -> {
            CountingOptionsSubScreen(
                isShakeEnabled = isShakeToCountEnabled,
                isVolumeKeysEnabled = isVolumeKeysToCountEnabled,
                onToggleShake = onToggleShakeToCount,
                onToggleVolumeKeys = onToggleVolumeKeysToCount,
                onBack = { onDestinationChange(SettingsDestination.MAIN) },
                modifier = modifier
            )
        }
        SettingsDestination.ABOUT -> {
            AboutSubScreen(
                onBack = { onDestinationChange(SettingsDestination.MAIN) },
                modifier = modifier
            )
        }
    }
}

/**
 * القائمة الرئيسية للإعدادات: تعرض الأقسام كعناصر قائمة قابلة للنقر مع أيقونة وسهم انتقال
 */
@Composable
private fun MainSettingsList(
    todayTotal: Int,
    isSalawatEnabled: Boolean,
    salawatInterval: Int,
    isMorningEveningEnabled: Boolean,
    morningHour: Int,
    morningMinute: Int,
    eveningHour: Int,
    eveningMinute: Int,
    isShakeToCountEnabled: Boolean,
    isVolumeKeysToCountEnabled: Boolean,
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val morningFormatted = String.format("%02d:%02d", morningHour, morningMinute)
    val eveningFormatted = String.format("%02d:%02d", eveningHour, eveningMinute)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .testTag("settings_main_list")
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardBackgroundVariant, CardBackground)
                    )
                )
                .border(1.dp, CardBorder, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MutedPurple)
                        .border(1.dp, PrimaryPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PrimaryPurpleLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "الإعدادات",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "تخصيص التنبيهات، خيارات التسبيح، وسجل الإنجاز",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Menu Items List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. الإحصائيات
            SettingsMenuItemRow(
                title = "الإحصائيات وسجل الإنجاز",
                subtitle = "تسبيحات اليوم ($todayTotal)، الأسبوع، والرسوم البيانية",
                icon = Icons.Default.BarChart,
                iconTint = AccentGold,
                onClick = { onNavigate(SettingsDestination.STATS) },
                testTag = "settings_menu_stats"
            )

            // 2. تذكير الصلاة على النبي
            SettingsMenuItemRow(
                title = "تذكير الصلاة على النبي",
                subtitle = if (isSalawatEnabled) "مُفعّل (كل $salawatInterval دقيقة)" else "التنبيه الدوري متوقف",
                icon = Icons.Default.NotificationsActive,
                iconTint = if (isSalawatEnabled) PrimaryPurpleLight else TextSecondary,
                badgeText = if (isSalawatEnabled) "مُفعّل" else null,
                isBadgeActive = isSalawatEnabled,
                onClick = { onNavigate(SettingsDestination.SALAWAT_REMINDER) },
                testTag = "settings_menu_salawat"
            )

            // 3. تذكير أذكار الصباح والمساء
            SettingsMenuItemRow(
                title = "تذكير أذكار الصباح والمساء",
                subtitle = if (isMorningEveningEnabled) "مُفعّل (الصباح $morningFormatted • المساء $eveningFormatted)" else "التذكير متوقف",
                icon = Icons.Default.WbTwilight,
                iconTint = if (isMorningEveningEnabled) PrimaryPurpleLight else TextSecondary,
                badgeText = if (isMorningEveningEnabled) "مُفعّل" else null,
                isBadgeActive = isMorningEveningEnabled,
                onClick = { onNavigate(SettingsDestination.MORNING_EVENING_REMINDER) },
                testTag = "settings_menu_morning_evening"
            )

            // 4. خيارات العد
            val countingActiveCount = (if (isShakeToCountEnabled) 1 else 0) + (if (isVolumeKeysToCountEnabled) 1 else 0)
            SettingsMenuItemRow(
                title = "خيارات العد الإضافية",
                subtitle = "العد بهز الموبايل وأزرار الصوت الجانبية",
                icon = Icons.Default.Tune,
                iconTint = if (countingActiveCount > 0) PrimaryPurpleLight else TextSecondary,
                badgeText = if (countingActiveCount > 0) "$countingActiveCount مُفعّل" else null,
                isBadgeActive = countingActiveCount > 0,
                onClick = { onNavigate(SettingsDestination.COUNTING_OPTIONS) },
                testTag = "settings_menu_counting_options"
            )

            // 5. عام / حول التطبيق
            SettingsMenuItemRow(
                title = "حول التطبيق",
                subtitle = "الإصدار 1.2.0 • يعمل بدون إنترنت • معلومات المسبحة",
                icon = Icons.Default.Info,
                iconTint = TextSecondary,
                onClick = { onNavigate(SettingsDestination.ABOUT) },
                testTag = "settings_menu_about"
            )
        }

        // ========================================================
        // 🤲 رسالة الدعاء في نهاية صفحة الإعدادات
        // ========================================================
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "أسألكم الدعاء لي ولوالدي 🤲",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = PrimaryPurpleLight,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

/**
 * صف عنصر القائمة في صفحة الإعدادات: أيقونة + نصوص + سهم انتقال ◀
 */
@Composable
private fun SettingsMenuItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isBadgeActive: Boolean = false,
    testTag: String = ""
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MutedPurple)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Texts Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBadgeActive) PrimaryPurple.copy(alpha = 0.25f) else CardBackgroundVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isBadgeActive) PrimaryPurpleLight else CardBorder
                            )
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isBadgeActive) PrimaryPurpleLight else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing Chevron / Arrow ◀
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MutedPurple.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "فتح القسم",
                    tint = PrimaryPurpleLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * هيدر موحد للصفحات الفرعية يحتوي على زر رجوع وعنوان وأيقونة
 */
@Composable
private fun SubScreenHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = PrimaryPurpleLight,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CardBackgroundVariant, CardBackground)
                )
            )
            .border(1.dp, CardBorder, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // زر الرجوع
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MutedPurple)
                    .border(1.dp, CardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "رجوع إلى الإعدادات",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MutedPurple)
                    .border(1.dp, PrimaryPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

// =========================================================================
// 🕊️ صفحة فرعية: تذكير الصلاة على النبي
// =========================================================================
@Composable
private fun SalawatReminderSubScreen(
    isEnabled: Boolean,
    interval: Int,
    onToggle: (Boolean) -> Unit,
    onSelectInterval: (Int) -> Unit,
    onTestAudio: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }

    fun checkAuxiliaryPermissions() {
        if (!ReminderPermissionHelper.canScheduleExactAlarms(context)) {
            showExactAlarmDialog = true
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggle(true)
            checkAuxiliaryPermissions()
        } else {
            showNotificationDeniedDialog = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        SubScreenHeader(
            title = "تذكير الصلاة على النبي",
            subtitle = "ضبط التنبيه الصوتي والإشعارات الدورية",
            icon = Icons.Default.NotificationsActive,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Toggle Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isEnabled) PrimaryPurple else MutedPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isEnabled) Color.White else PrimaryPurpleLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تفعيل التذكير الدوري",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (isEnabled) "ينبهك بصوت الصلاة على النبي وإشعار دوري" else "التنبيه الدوري متوقف حالياً",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isEnabled) Color(0xFF4CAF50) else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !ReminderPermissionHelper.hasNotificationPermission(context)
                                    ) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onToggle(true)
                                        checkAuxiliaryPermissions()
                                    }
                                } else {
                                    onToggle(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CardBackgroundVariant
                            )
                        )
                    }

                    AnimatedVisibility(visible = isEnabled) {
                        Column(modifier = Modifier.padding(top = 18.dp)) {
                            Text(
                                text = "تكرار التنبيه كل:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val row1Intervals = listOf(
                                5 to "5 دقائق",
                                10 to "10 دقائق",
                                15 to "15 دقيقة"
                            )
                            val row2Intervals = listOf(
                                30 to "30 دقيقة",
                                60 to "كل ساعة"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row1Intervals.forEach { (minutes, label) ->
                                        val isSelected = interval == minutes
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) PrimaryPurple else MutedPurple)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) PrimaryPurpleLight else CardBorder,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onSelectInterval(minutes) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) Color.White else TextSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row2Intervals.forEach { (minutes, label) ->
                                        val isSelected = interval == minutes
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) PrimaryPurple else MutedPurple)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) PrimaryPurpleLight else CardBorder,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onSelectInterval(minutes) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) Color.White else TextSecondary,
                                                    fontWeight = FontWeight.Bold
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

            // طبيعة التنبيه
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "طبيعة التنبيه:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    )
                    Text(
                        text = "• تنبيه صوتي: مقطع صوتي نقي ومميز ينبهك للصلاة على النبي ﷺ.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                    Text(
                        text = "• إشعار مرئي: إشعار في شريط التنبيهات يذكرك بالصلاة على الحبيب ﷺ.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // زر تجربة التنبيه الصوتي
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تجربة التنبيه الصوتي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط للاستماع إلى الصوت الذي سيتم تشغيله عند حلول وقت التذكير",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (SalawatAudioPlayer.isSilentOrVibrate(context)) {
                                Toast.makeText(context, "الهاتف في الوضع الصامت، لن يتم تشغيل الصوت", Toast.LENGTH_SHORT).show()
                            } else {
                                onTestAudio()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تشغيل صوت التجربة الآن",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showExactAlarmDialog) {
        ExactAlarmPermissionDialog(
            onDismiss = { showExactAlarmDialog = false },
            context = context
        )
    }

    if (showNotificationDeniedDialog) {
        NotificationDeniedDialog(
            onDismiss = { showNotificationDeniedDialog = false },
            context = context
        )
    }
}

// =========================================================================
// 🌅 صفحة فرعية: تذكير أذكار الصباح والمساء
// =========================================================================
@Composable
private fun MorningEveningReminderSubScreen(
    isEnabled: Boolean,
    repeatInterval: Int,
    morningHour: Int,
    morningMinute: Int,
    eveningHour: Int,
    eveningMinute: Int,
    onToggle: (Boolean) -> Unit,
    onSelectRepeat: (Int) -> Unit,
    onTestReminder: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val morningFormatted = String.format("%02d:%02d", morningHour, morningMinute)
    val eveningFormatted = String.format("%02d:%02d", eveningHour, eveningMinute)

    val context = LocalContext.current
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }

    fun checkAuxiliaryPermissions() {
        if (!ReminderPermissionHelper.canScheduleExactAlarms(context)) {
            showExactAlarmDialog = true
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggle(true)
            checkAuxiliaryPermissions()
        } else {
            showNotificationDeniedDialog = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        SubScreenHeader(
            title = "تذكير أذكار الصباح والمساء",
            subtitle = "ضبط مواقيت التنبيه وتكرار الإشعارات",
            icon = Icons.Default.WbTwilight,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isEnabled) PrimaryPurple else MutedPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbTwilight,
                                contentDescription = null,
                                tint = if (isEnabled) Color.White else PrimaryPurpleLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تفعيل تذكير الأذكار",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = if (isEnabled) "تنبيهك في أوقات الصباح والمساء بانتظام" else "التذكير متوقف حالياً",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isEnabled) Color(0xFF4CAF50) else TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !ReminderPermissionHelper.hasNotificationPermission(context)
                                ) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onToggle(true)
                                    checkAuxiliaryPermissions()
                                }
                            } else {
                                onToggle(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryPurple,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = CardBackgroundVariant
                        )
                    )
                }
            }

            // Morning & Evening Times Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "المواقيت المحددة للأذكار:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // الصباح
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardBackgroundVariant)
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🌅 أذكار الصباح",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = morningFormatted,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "يبدأ مع شروق الفجر",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryPurpleLight,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // المساء
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardBackgroundVariant)
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🌇 أذكار المساء",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = eveningFormatted,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "يبدأ بعد صلاة العصر",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryPurpleLight,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // تكرار التذكير أثناء الفترة
            AnimatedVisibility(visible = isEnabled) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تكرار التذكير أثناء فترة الأذكار:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val repeatOptions = listOf(
                            0 to "مرة واحدة",
                            60 to "كل ساعة",
                            30 to "كل 30 دقيقة"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeatOptions.forEach { (minutes, label) ->
                                val isSelected = repeatInterval == minutes
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PrimaryPurple else MutedPurple)
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimaryPurpleLight else CardBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectRepeat(minutes) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // أزرار تجربة التذكير الفوري
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تجربة إشعارات الأذكار",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط لتجربة ظهور الإشعار في شريط الحالة على الفور",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTestReminder(true) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "تجربة إشعار الصباح",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Button(
                            onClick = { onTestReminder(false) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "تجربة إشعار المساء",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showExactAlarmDialog) {
        ExactAlarmPermissionDialog(
            onDismiss = { showExactAlarmDialog = false },
            context = context
        )
    }

    if (showNotificationDeniedDialog) {
        NotificationDeniedDialog(
            onDismiss = { showNotificationDeniedDialog = false },
            context = context
        )
    }
}



// =========================================================================
// 📳 صفحة فرعية: خيارات العد الإضافية (الهز وأزرار الصوت)
// =========================================================================
@Composable
private fun CountingOptionsSubScreen(
    isShakeEnabled: Boolean,
    isVolumeKeysEnabled: Boolean,
    onToggleShake: (Boolean) -> Unit,
    onToggleVolumeKeys: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        SubScreenHeader(
            title = "خيارات العد الإضافية",
            subtitle = "طرق مبتكرة للتسبيح دون لمس الشاشة",
            icon = Icons.Default.Tune,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // العد بالهز
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isShakeEnabled) PrimaryPurple else MutedPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isShakeEnabled) Color.White else PrimaryPurpleLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "العد بهز الموبايل",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (isShakeEnabled) "مُفعّل • هز الهاتف بخفة للتسبيح" else "معطل حالياً",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isShakeEnabled) Color(0xFF4CAF50) else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isShakeEnabled,
                            onCheckedChange = { onToggleShake(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CardBackgroundVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "عند تفعيل هذا الخيار وتواجدك في صفحة المسبحة، يمكنك هز الهاتف بحركة خفيفة لزيادة العداد دون الحاجة للنظر إلى الشاشة أو لمسها، مع اهتزاز لمسي يؤكد احتساب التسبيحة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // العد بأزرار الصوت
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isVolumeKeysEnabled) PrimaryPurple else MutedPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (isVolumeKeysEnabled) Color.White else PrimaryPurpleLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "العد بأزرار الصوت",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (isVolumeKeysEnabled) "مُفعّل • اضغط أزرار الصوت للتسبيح" else "معطل حالياً",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isVolumeKeysEnabled) Color(0xFF4CAF50) else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isVolumeKeysEnabled,
                            onCheckedChange = { onToggleVolumeKeys(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CardBackgroundVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "عند تفعيل هذا الخيار وتواجدك في صفحة المسبحة، يزداد العداد عند الضغط على زري رفع أو خفض الصوت الجانبيين في الهاتف، مما يتيح لك التسبيح بينما الهاتف في يدك أو جيبك.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// =========================================================================
// ℹ️ صفحة فرعية: حول التطبيق
// =========================================================================
@Composable
private fun AboutSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        SubScreenHeader(
            title = "حول التطبيق",
            subtitle = "المسبحة الإلكترونية والأذكار النبوية",
            icon = Icons.Default.Info,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(PrimaryPurpleLight, PrimaryPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📿",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "المسبحة الإلكترونية الذكية",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "الإصدار 1.2.0 • صدقة جارية",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "تطبيق إسلامي شامل للذكر والتسبيح، صُمم بعناية ليعينك على ذكر الله في كل أوقاتك بكل يسر وسهولة.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            // ميزات التطبيق
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أهم المميزات:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    )
                    Text(
                        text = "• يعمل دون الحاجة للاتصال بالإنترنت تماماً وبلا إعلانات.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Text(
                        text = "• مسبحة متقدمة تدعم تحديد الأهداف والعد بالهز وبأزرار الصوت.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Text(
                        text = "• أذكار الصباح والمساء وأذكار بعد الصلاة مع عدادات ذكية لكل ذكر.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Text(
                        text = "• تنبيه صوتي دوري ومرئي للصلاة على النبي ﷺ.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Text(
                        text = "• فقاعة التسبيح الذكي العائمة للتسبيح أثناء تصفح التطبيقات الأخرى.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Text(
                        text = "• إحصائيات دقيقة وسجل إنجاز يومي وأسبوعي ورسوم بيانية.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                }
            }

            // دعاء ختامي
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "﴿ وَالذَّاكِرِينَ اللَّهَ كَثِيرًا وَالذَّاكِرَاتِ أَعَدَّ اللَّهُ لَهُم مَّغْفِرَةً وَأَجْرًا عَظِيمًا ﴾",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = UthmanicFontFamily,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
