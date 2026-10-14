package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.bubble.SmartTasbeehManager
import com.example.data.model.DhikrItem
import com.example.ui.components.TopTasbeehHeader
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundVariant
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkRedReset
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.MutedPurpleLight
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.PrimaryPurpleVibrant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UthmanicFontFamily
import com.example.util.ShakeDetector

@Composable
fun TasbeehScreen(
    azkarList: List<DhikrItem>,
    selectedDhikr: DhikrItem?,
    counter: Int,
    goal: Int, // 33, 100, 0
    todayTotal: Int,
    showGoalDialog: Boolean,
    isShakeToCountEnabled: Boolean,
    isSmartTasbeehEnabled: Boolean = false,
    onTasbeehClick: () -> Unit,
    onSelectDhikr: (DhikrItem) -> Unit,
    onSelectGoal: (Int) -> Unit,
    onResetCounter: () -> Unit,
    onDismissGoalDialog: () -> Unit,
    onShakeDetected: () -> Unit,
    onToggleSmartTasbeeh: (Boolean) -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Register ShakeDetector when in TasbeehScreen and feature is enabled
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(isShakeToCountEnabled, lifecycleOwner) {
        android.util.Log.d("TasbeehScreen", "ShakeDetector effect evaluated: isShakeToCountEnabled=$isShakeToCountEnabled")
        var detector: ShakeDetector? = null

        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (isShakeToCountEnabled && detector == null) {
                    android.util.Log.d("TasbeehScreen", "Lifecycle ON_RESUME: starting ShakeDetector")
                    detector = ShakeDetector(context) {
                        android.util.Log.d("TasbeehScreen", "Shake triggered! Calling onShakeDetected()")
                        onShakeDetected()
                    }.apply { start() }
                }
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                android.util.Log.d("TasbeehScreen", "Lifecycle ON_PAUSE: stopping ShakeDetector")
                detector?.stop()
                detector = null
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        if (isShakeToCountEnabled && lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            android.util.Log.d("TasbeehScreen", "Immediately starting ShakeDetector")
            detector = ShakeDetector(context) {
                android.util.Log.d("TasbeehScreen", "Shake triggered! Calling onShakeDetected()")
                onShakeDetected()
            }.apply { start() }
        }

        onDispose {
            android.util.Log.d("TasbeehScreen", "Disposing ShakeDetector effect")
            lifecycleOwner.lifecycle.removeObserver(observer)
            detector?.stop()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring scale animation on central button press
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "TasbeehButtonScale"
    )

    // Calculate progress fraction
    val progressFraction = if (goal > 0) {
        (counter.toFloat() / goal).coerceIn(0f, 1f)
    } else 0f

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Header (الأعلى): يعرض الشعار وإجمالي تسبيحات اليوم
        TopTasbeehHeader(
            todayCount = todayTotal,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Body (المنتصف): يركز بالكامل على التسبيح النقي والمرتب
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========================================================
            // 🫧 ميزة التسبيح الذكي (الفقاعة العائمة) في أعلى الشاشة
            // ========================================================
            val hasOverlayPermission = SmartTasbeehManager.canDrawOverlays(context)

            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_smart_tasbeeh_control")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSmartTasbeehEnabled) PrimaryPurple else MutedPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "التسبيح الذكي",
                                    tint = if (isSmartTasbeehEnabled) Color.White else PrimaryPurpleLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "التسبيح الذكي (الفقاعة العائمة)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isSmartTasbeehEnabled) "نشط • فقاعة تظهر فوق كل التطبيقات" else "تسبيح عائم خارج التطبيق",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSmartTasbeehEnabled) Color(0xFF4CAF50) else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isSmartTasbeehEnabled,
                            onCheckedChange = { checked ->
                                if (checked && !SmartTasbeehManager.canDrawOverlays(context)) {
                                    showOverlayPermissionDialog = true
                                } else {
                                    onToggleSmartTasbeeh(checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CardBackgroundVariant
                            ),
                            modifier = Modifier.testTag("smart_tasbeeh_toggle")
                        )
                    }

                    if (isSmartTasbeehEnabled && !hasOverlayPermission) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentGold.copy(alpha = 0.12f))
                                .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "يتطلب إذن الظهور فوق التطبيقات",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            OutlinedButton(
                                onClick = onRequestOverlayPermission,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurpleLight),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("منح الإذن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // قائمة اختيار الذكر: شريط منسدل أنيق
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dhikr_dropdown_trigger")
            ) {
                Surface(
                    onClick = { dropdownExpanded = true },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CardBorder),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الذكر الحالي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryPurpleLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedDhikr?.arabicText ?: stringResource(R.string.select_dhikr),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = UthmanicFontFamily,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "قائمة الأذكار",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(CardBackgroundVariant)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    azkarList.forEach { dhikr ->
                        DropdownMenuItem(
                            text = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = dhikr.arabicText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = UthmanicFontFamily,
                                            color = if (dhikr.id == selectedDhikr?.id) PrimaryPurpleLight else TextPrimary,
                                            fontWeight = if (dhikr.id == selectedDhikr?.id) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 17.sp
                                        ),
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "المستهدف: ${if (dhikr.targetCount > 0) "${dhikr.targetCount} مرة" else "مفتوح"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary
                                        )
                                    )
                                }
                            },
                            onClick = {
                                onSelectDhikr(dhikr)
                                dropdownExpanded = false
                            },
                            trailingIcon = if (dhikr.id == selectedDhikr?.id) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "المحدد حالياً",
                                        tint = PrimaryPurple
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            // النص الكامل للذكر المختار بخط عثماني جميل
            selectedDhikr?.let { dhikr ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = dhikr.arabicText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = UthmanicFontFamily,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 34.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ========================================================
            // ⚪ دائرة عداد التسبيح الكبيرة (الرقم الحالي + اضغط للتسبيح)
            // ========================================================
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .scale(buttonScale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTasbeehClick
                    )
                    .testTag("tasbeeh_central_button")
            ) {
                // Outer circular progress ring
                if (goal > 0) {
                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.size(236.dp),
                        color = PrimaryPurple,
                        trackColor = MutedPurple,
                        strokeWidth = 6.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(236.dp)
                            .border(3.dp, MutedPurple, CircleShape)
                    )
                }

                // Central interactive sphere
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(210.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = PrimaryPurple,
                            ambientColor = PrimaryPurpleVibrant
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MutedPurpleLight,
                                    MutedPurple,
                                    CardBackground
                                )
                            )
                        )
                        .border(3.dp, PrimaryPurple, CircleShape)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Bold counter number
                        Text(
                            text = "$counter",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 54.sp,
                                color = TextPrimary
                            ),
                            modifier = Modifier.testTag("counter_display")
                        )

                        Text(
                            text = if (goal > 0) "الهدف: $goal" else "الهدف: مفتوح",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = AccentGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryPurple.copy(alpha = 0.2f))
                                .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "اضغط للتسبيح",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // ========================================================
            // 🎯 مربع اختيار الهدف مباشرة تحت دائرة عداد التسبيح
            // ========================================================
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_goal_selector")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الهدف المختار:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(33 to "33", 100 to "100", 0 to "مفتوح").forEach { (targetVal, label) ->
                            val isSelected = goal == targetVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) PrimaryPurple else MutedPurple
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryPurpleLight else CardBorder,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onSelectGoal(targetVal) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("goal_selector_$targetVal")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ========================================================
            // 🔄 زر إعادة الضبط (تصفير العداد)
            // ========================================================
            Button(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkRedReset,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_counter_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة ضبط العداد",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_reset),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // رسالة تأكيد إعادة الضبط (Confirmation Dialog)
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.reset_confirm_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.reset_confirm_message),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetCounter()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRedReset)
                ) {
                    Text(text = stringResource(R.string.confirm_yes))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetConfirm = false }
                ) {
                    Text(text = stringResource(R.string.confirm_cancel), color = TextPrimary)
                }
            },
            containerColor = CardBackground,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        )
    }

    // Dialog upon reaching goal
    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = onDismissGoalDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.target_reached_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            },
            text = {
                Text(
                    text = "${stringResource(R.string.target_reached_message)}\n(العدد المنجز: $counter)",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismissGoalDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("متابعة الذكر", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onResetCounter()
                        onDismissGoalDialog()
                    }
                ) {
                    Text("إعادة العداد", color = PrimaryPurpleLight)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        )
    }

    // نافذة إذن الظهور فوق التطبيقات
    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = PrimaryPurpleLight,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "إذن الظهور فوق التطبيقات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Text(
                    text = "تتطلب ميزة التسبيح الذكي إذن الظهور فوق التطبيقات لعرض فقاعة التسبيح أثناء تصفح هاتفك.\n\nيرجى منح الإذن من الإعدادات.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayPermissionDialog = false
                        onRequestOverlayPermission()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("الانتقال للإعدادات", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showOverlayPermissionDialog = false }
                ) {
                    Text(text = stringResource(R.string.confirm_cancel), color = TextPrimary)
                }
            },
            containerColor = CardBackground,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        )
    }
}
