package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.example.reminder.ReminderPermissionHelper
import com.example.ui.components.NotificationDeniedDialog
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bubble.SmartTasbeehManager
import com.example.ui.TasbeehViewModel
import com.example.ui.components.AppTab
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.AzkarScreen
import com.example.ui.screens.MorningEveningScreen
import com.example.ui.screens.SalawatScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettingsDestination
import com.example.ui.screens.TasbeehScreen
import com.example.ui.theme.MyApplicationTheme

import android.view.KeyEvent
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    private val viewModel: TasbeehViewModel by viewModels {
        TasbeehViewModel.provideFactory(application)
    }

    var onVolumeKeyPressed: (() -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    TasbeehApp(
                        viewModel = viewModel,
                        setVolumeKeyHandler = { handler ->
                            android.util.Log.d("TasbeehApp", "setVolumeKeyHandler called, handler is null? ${handler == null}")
                            onVolumeKeyPressed = handler
                        }
                    )
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            android.util.Log.d("TasbeehApp", "dispatchKeyEvent: action=${event.action}, keyCode=$keyCode, handlerExists=${onVolumeKeyPressed != null}")
            // Only trigger once per press (on ACTION_DOWN)
            if (event.action == KeyEvent.ACTION_DOWN) {
                val handled = onVolumeKeyPressed?.invoke() ?: false
                android.util.Log.d("TasbeehApp", "dispatchKeyEvent: volume press handled = $handled")
                if (handled) {
                    return true
                }
            } else if (event.action == KeyEvent.ACTION_UP) {
                // If handler is active, consume the UP event as well so system volume slider doesn't pop up
                if (onVolumeKeyPressed != null) {
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        android.util.Log.d("TasbeehApp", "onKeyDown called with keyCode=$keyCode, handler=${onVolumeKeyPressed != null}")
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val handled = onVolumeKeyPressed?.invoke() ?: false
            android.util.Log.d("TasbeehApp", "onKeyDown volume handled: $handled")
            if (handled) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun TasbeehApp(
    viewModel: TasbeehViewModel,
    setVolumeKeyHandler: (((() -> Boolean)?) -> Unit)? = null
) {
    val context = LocalContext.current.applicationContext as Application

    val azkarList by viewModel.allAzkar.collectAsStateWithLifecycle()
    val selectedDhikr by viewModel.selectedDhikr.collectAsStateWithLifecycle()
    val counter by viewModel.currentCounter.collectAsStateWithLifecycle()
    val goal by viewModel.selectedGoal.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val showGoalDialog by viewModel.goalReachedDialog.collectAsStateWithLifecycle()
    val isSalawatEnabled by viewModel.isSalawatEnabled.collectAsStateWithLifecycle()
    val salawatInterval by viewModel.salawatInterval.collectAsStateWithLifecycle()
    val recentStats by viewModel.recentStats.collectAsStateWithLifecycle()
    val lifetimeTotal by viewModel.lifetimeTotal.collectAsStateWithLifecycle()

    val morningAzkar by viewModel.morningAzkar.collectAsStateWithLifecycle()
    val eveningAzkar by viewModel.eveningAzkar.collectAsStateWithLifecycle()
    val isMorningEveningEnabled by viewModel.isMorningEveningEnabled.collectAsStateWithLifecycle()
    val meRepeatInterval by viewModel.meRepeatInterval.collectAsStateWithLifecycle()
    val morningHour by viewModel.morningHour.collectAsStateWithLifecycle()
    val morningMinute by viewModel.morningMinute.collectAsStateWithLifecycle()
    val eveningHour by viewModel.eveningHour.collectAsStateWithLifecycle()
    val eveningMinute by viewModel.eveningMinute.collectAsStateWithLifecycle()

    val isSmartTasbeehEnabled by viewModel.isSmartTasbeehEnabled.collectAsStateWithLifecycle()
    val isSmartTasbeehBootEnabled by viewModel.isSmartTasbeehBootEnabled.collectAsStateWithLifecycle()
    val isShakeToCountEnabled by viewModel.isShakeToCountEnabled.collectAsStateWithLifecycle()
    val isVolumeKeysToCountEnabled by viewModel.isVolumeKeysToCountEnabled.collectAsStateWithLifecycle()
    val currentActivityContext = LocalContext.current
    var showNotificationDeniedDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showNotificationDeniedDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!ReminderPermissionHelper.hasNotificationPermission(currentActivityContext)) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                SmartTasbeehManager.checkAndResumePendingEnable(context)
                viewModel.refreshSmartTasbeehState(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var currentTab by remember { mutableStateOf(AppTab.TASBEEH) }
    var settingsDestination by remember { mutableStateOf(SettingsDestination.MAIN) }
    var showSplashScreen by remember { mutableStateOf(true) }

    // Intercept volume keys only when on the Tasbeeh tab and feature is enabled
    DisposableEffect(currentTab, isVolumeKeysToCountEnabled, showSplashScreen) {
        val shouldIntercept = !showSplashScreen && currentTab == AppTab.TASBEEH && isVolumeKeysToCountEnabled
        android.util.Log.d("TasbeehApp", "Volume key effect updated: currentTab=$currentTab, isVolumeKeysToCountEnabled=$isVolumeKeysToCountEnabled, shouldIntercept=$shouldIntercept")
        if (shouldIntercept) {
            setVolumeKeyHandler?.invoke {
                android.util.Log.d("TasbeehApp", "Volume key pressed while on Tasbeeh tab -> invoking onTasbeehClick()")
                viewModel.onTasbeehClick()
                true
            }
        } else {
            setVolumeKeyHandler?.invoke(null)
        }
        onDispose {
            setVolumeKeyHandler?.invoke(null)
        }
    }

    Crossfade(
        targetState = showSplashScreen,
        animationSpec = tween(durationMillis = 600),
        label = "splashCrossfade"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(
                onTimeout = { showSplashScreen = false }
            )
        } else {
            Scaffold(
                containerColor = com.example.ui.theme.AppBackground,
                contentWindowInsets = WindowInsets.statusBars,
                bottomBar = {
                    BottomNavBar(
                        selectedTab = currentTab,
                        onTabSelected = {
                            currentTab = it
                            if (it == AppTab.SETTINGS) {
                                settingsDestination = SettingsDestination.MAIN
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        AppTab.TASBEEH -> {
                            TasbeehScreen(
                                azkarList = azkarList,
                                selectedDhikr = selectedDhikr,
                                counter = counter,
                                goal = goal,
                                todayTotal = todayTotal,
                                showGoalDialog = showGoalDialog,
                                isShakeToCountEnabled = isShakeToCountEnabled,
                                isSmartTasbeehEnabled = isSmartTasbeehEnabled,
                                onTasbeehClick = { viewModel.onTasbeehClick() },
                                onSelectDhikr = { viewModel.selectDhikr(it) },
                                onSelectGoal = { viewModel.selectGoal(it) },
                                onResetCounter = { viewModel.resetCounter() },
                                onDismissGoalDialog = { viewModel.dismissGoalDialog() },
                                onShakeDetected = { viewModel.onShakeTasbeeh() },
                                onToggleSmartTasbeeh = { viewModel.setSmartTasbeehEnabled(it, context) },
                                onRequestOverlayPermission = { SmartTasbeehManager.openOverlaySettings(context) }
                            )
                        }
                        AppTab.AZKAR -> {
                            AzkarScreen(
                                azkarList = azkarList,
                                onSelectForTasbeeh = { dhikr ->
                                    viewModel.selectDhikr(dhikr)
                                    currentTab = AppTab.TASBEEH
                                }
                            )
                        }
                        AppTab.MORNING_EVENING -> {
                            MorningEveningScreen(
                                morningAzkar = morningAzkar,
                                eveningAzkar = eveningAzkar,
                                isReminderEnabled = isMorningEveningEnabled,
                                repeatInterval = meRepeatInterval,
                                morningHour = morningHour,
                                morningMinute = morningMinute,
                                eveningHour = eveningHour,
                                eveningMinute = eveningMinute,
                                onIncrementDhikr = { id, cat -> viewModel.incrementMorningEvening(id, cat) },
                                onResetDhikr = { id, cat -> viewModel.resetMorningEveningDhikr(id, cat) },
                                onResetCategory = { cat -> viewModel.resetMorningEveningCategory(cat) },
                                onToggleReminder = { viewModel.toggleMorningEveningReminder(it) },
                                onSelectRepeat = { viewModel.setMorningEveningRepeat(it) },
                                onTestReminder = { isMorning -> viewModel.testMorningEveningReminder(isMorning) },
                                onNavigateToSettings = {
                                    settingsDestination = SettingsDestination.MORNING_EVENING_REMINDER
                                    currentTab = AppTab.SETTINGS
                                }
                            )
                        }
                        AppTab.SALAWAT -> {
                            SalawatScreen(
                                isEnabled = isSalawatEnabled,
                                selectedInterval = salawatInterval,
                                onToggleSalawat = { viewModel.toggleSalawat(it) },
                                onSelectInterval = { viewModel.setSalawatInterval(it) },
                                onTestReminder = { viewModel.testSalawatReminder() },
                                onNavigateToSettings = {
                                    settingsDestination = SettingsDestination.SALAWAT_REMINDER
                                    currentTab = AppTab.SETTINGS
                                }
                            )
                        }
                        AppTab.SETTINGS -> {
                            SettingsScreen(
                                todayTotal = todayTotal,
                                lifetimeTotal = lifetimeTotal,
                                recentStats = recentStats,
                                azkarList = azkarList,
                                isSalawatEnabled = isSalawatEnabled,
                                salawatInterval = salawatInterval,
                                onToggleSalawat = { viewModel.toggleSalawat(it) },
                                onSelectSalawatInterval = { viewModel.setSalawatInterval(it) },
                                onTestSalawatReminder = { viewModel.testSalawatReminder() },
                                isMorningEveningEnabled = isMorningEveningEnabled,
                                meRepeatInterval = meRepeatInterval,
                                morningHour = morningHour,
                                morningMinute = morningMinute,
                                eveningHour = eveningHour,
                                eveningMinute = eveningMinute,
                                onToggleMorningEveningReminder = { viewModel.toggleMorningEveningReminder(it) },
                                onSelectMorningEveningRepeat = { viewModel.setMorningEveningRepeat(it) },
                                onTestMorningEveningReminder = { isMorning -> viewModel.testMorningEveningReminder(isMorning) },
                                isShakeToCountEnabled = isShakeToCountEnabled,
                                isVolumeKeysToCountEnabled = isVolumeKeysToCountEnabled,
                                onToggleShakeToCount = { viewModel.setShakeToCountEnabled(it, context) },
                                onToggleVolumeKeysToCount = { viewModel.setVolumeKeysToCountEnabled(it, context) },
                                currentDestination = settingsDestination,
                                onDestinationChange = { settingsDestination = it }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNotificationDeniedDialog) {
        NotificationDeniedDialog(
            onDismiss = { showNotificationDeniedDialog = false },
            context = currentActivityContext
        )
    }
}

