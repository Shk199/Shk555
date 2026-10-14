package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bubble.SmartTasbeehManager
import com.example.data.local.TasbeehDatabase
import com.example.data.model.DailyStat
import com.example.data.model.DhikrItem
import com.example.data.model.MorningEveningCategory
import com.example.data.model.MorningEveningItemState
import com.example.data.repository.MorningEveningRepository
import com.example.data.repository.TasbeehRepository
import com.example.reminder.SalawatAudioPlayer
import com.example.reminder.MorningEveningNotificationHelper
import com.example.reminder.MorningEveningScheduler
import com.example.reminder.SalawatNotificationHelper
import com.example.reminder.SalawatScheduler
import com.example.util.VibrationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasbeehViewModel(
    application: Application,
    private val repository: TasbeehRepository,
    private val morningEveningRepository: MorningEveningRepository
) : AndroidViewModel(application) {

    val allAzkar: StateFlow<List<DhikrItem>> = repository.allAzkar
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val todayStat: StateFlow<DailyStat?> = repository.getTodayStat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayTotal: StateFlow<Int> = todayStat.map { it?.totalCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentStats: StateFlow<List<DailyStat>> = repository.getRecentStats(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lifetimeTotal: StateFlow<Int> = repository.lifetimeTotal.map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedDhikr = MutableStateFlow<DhikrItem?>(null)
    val selectedDhikr: StateFlow<DhikrItem?> = _selectedDhikr.asStateFlow()

    private val _currentCounter = MutableStateFlow(0)
    val currentCounter: StateFlow<Int> = _currentCounter.asStateFlow()

    // Goals: 33, 100, or 0 (open/مفتوح)
    private val _selectedGoal = MutableStateFlow(33)
    val selectedGoal: StateFlow<Int> = _selectedGoal.asStateFlow()

    private val _goalReachedDialog = MutableStateFlow(false)
    val goalReachedDialog: StateFlow<Boolean> = _goalReachedDialog.asStateFlow()

    // Salawat Reminder states
    private val _isSalawatEnabled = MutableStateFlow(false)
    val isSalawatEnabled: StateFlow<Boolean> = _isSalawatEnabled.asStateFlow()

    private val _salawatInterval = MutableStateFlow(30)
    val salawatInterval: StateFlow<Int> = _salawatInterval.asStateFlow()

    // Morning & Evening Azkar states
    val morningAzkar: StateFlow<List<MorningEveningItemState>> = morningEveningRepository.morningAzkar
    val eveningAzkar: StateFlow<List<MorningEveningItemState>> = morningEveningRepository.eveningAzkar

    private val _isMorningEveningEnabled = MutableStateFlow(false)
    val isMorningEveningEnabled: StateFlow<Boolean> = _isMorningEveningEnabled.asStateFlow()

    private val _meRepeatInterval = MutableStateFlow(0)
    val meRepeatInterval: StateFlow<Int> = _meRepeatInterval.asStateFlow()

    private val _morningHour = MutableStateFlow(6)
    val morningHour: StateFlow<Int> = _morningHour.asStateFlow()

    private val _morningMinute = MutableStateFlow(30)
    val morningMinute: StateFlow<Int> = _morningMinute.asStateFlow()

    private val _eveningHour = MutableStateFlow(17)
    val eveningHour: StateFlow<Int> = _eveningHour.asStateFlow()

    private val _eveningMinute = MutableStateFlow(0)
    val eveningMinute: StateFlow<Int> = _eveningMinute.asStateFlow()

    // Smart Tasbeeh (Floating Bubble) states
    private val _isSmartTasbeehEnabled = MutableStateFlow(false)
    val isSmartTasbeehEnabled: StateFlow<Boolean> = _isSmartTasbeehEnabled.asStateFlow()

    private val _isSmartTasbeehBootEnabled = MutableStateFlow(false)
    val isSmartTasbeehBootEnabled: StateFlow<Boolean> = _isSmartTasbeehBootEnabled.asStateFlow()

    // Shake & Volume Keys states
    private val _isShakeToCountEnabled = MutableStateFlow(false)
    val isShakeToCountEnabled: StateFlow<Boolean> = _isShakeToCountEnabled.asStateFlow()

    private val _isVolumeKeysToCountEnabled = MutableStateFlow(false)
    val isVolumeKeysToCountEnabled: StateFlow<Boolean> = _isVolumeKeysToCountEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureAzkarPopulated()
            // Observe azkar and pick the first one by default if not selected
            allAzkar.collect { list ->
                if (list.isNotEmpty() && _selectedDhikr.value == null) {
                    val first = list.first()
                    _selectedDhikr.value = first
                    _currentCounter.value = first.todayCount
                    _selectedGoal.value = if (first.targetCount > 0) first.targetCount else 33
                }
            }
        }

        // Load Salawat prefs
        val context = getApplication<Application>()
        _isSalawatEnabled.value = SalawatScheduler.isReminderEnabled(context)
        _salawatInterval.value = SalawatScheduler.getIntervalMinutes(context)

        // Load Morning & Evening Reminder prefs
        _isMorningEveningEnabled.value = MorningEveningScheduler.isEnabled(context)
        _morningHour.value = MorningEveningScheduler.getMorningHour(context)
        _morningMinute.value = MorningEveningScheduler.getMorningMinute(context)
        _eveningHour.value = MorningEveningScheduler.getEveningHour(context)
        _eveningMinute.value = MorningEveningScheduler.getEveningMinute(context)
        _meRepeatInterval.value = MorningEveningScheduler.getRepeatInterval(context)

        // Load Smart Tasbeeh prefs
        _isSmartTasbeehEnabled.value = SmartTasbeehManager.isEnabled(context) && SmartTasbeehManager.canDrawOverlays(context)
        _isSmartTasbeehBootEnabled.value = SmartTasbeehManager.isAutoStartOnBootEnabled(context)

        // Load Shake & Volume keys prefs
        _isShakeToCountEnabled.value = com.example.util.TasbeehSettingsManager.isShakeToCountEnabled(context)
        _isVolumeKeysToCountEnabled.value = com.example.util.TasbeehSettingsManager.isVolumeKeysToCountEnabled(context)
    }

    fun selectDhikr(dhikr: DhikrItem) {
        _selectedDhikr.value = dhikr
        _currentCounter.value = dhikr.todayCount
        if (dhikr.targetCount > 0) {
            _selectedGoal.value = dhikr.targetCount
        }
        SmartTasbeehManager.setCurrentDhikrId(getApplication(), dhikr.id)
    }

    fun refreshSmartTasbeehState(context: Context) {
        _isSmartTasbeehEnabled.value = SmartTasbeehManager.isEnabled(context) && SmartTasbeehManager.canDrawOverlays(context)
        _isSmartTasbeehBootEnabled.value = SmartTasbeehManager.isAutoStartOnBootEnabled(context)
    }

    fun setSmartTasbeehEnabled(enabled: Boolean, context: Context) {
        _isSmartTasbeehEnabled.value = enabled
        SmartTasbeehManager.setEnabled(context, enabled)
    }

    fun setSmartTasbeehBootEnabled(enabled: Boolean, context: Context) {
        _isSmartTasbeehBootEnabled.value = enabled
        SmartTasbeehManager.setAutoStartOnBootEnabled(context, enabled)
    }

    fun setShakeToCountEnabled(enabled: Boolean, context: Context) {
        _isShakeToCountEnabled.value = enabled
        com.example.util.TasbeehSettingsManager.setShakeToCountEnabled(context, enabled)
    }

    fun setVolumeKeysToCountEnabled(enabled: Boolean, context: Context) {
        _isVolumeKeysToCountEnabled.value = enabled
        com.example.util.TasbeehSettingsManager.setVolumeKeysToCountEnabled(context, enabled)
    }

    fun onShakeTasbeeh() {
        val context = getApplication<Application>()
        val dhikr = _selectedDhikr.value ?: return

        // 1. Short distinct vibration feedback for shake gesture
        VibrationHelper.shakeHaptic(context)

        // 2. Increment local counter
        val newCount = _currentCounter.value + 1
        _currentCounter.value = newCount

        // 3. Check goal completion
        val goal = _selectedGoal.value
        if (goal > 0 && newCount == goal) {
            VibrationHelper.goalReachedVibration(context)
            _goalReachedDialog.value = true
        }

        // 4. Update repository in background
        viewModelScope.launch {
            repository.incrementTasbeeh(dhikr.id)
        }
    }

    fun selectGoal(goal: Int) {
        _selectedGoal.value = goal
    }

    fun onTasbeehClick() {
        val context = getApplication<Application>()
        val dhikr = _selectedDhikr.value ?: return

        // 1. Light haptic feedback on every tap
        VibrationHelper.tapHaptic(context)

        // 2. Increment local counter
        val newCount = _currentCounter.value + 1
        _currentCounter.value = newCount

        // 3. Check goal completion
        val goal = _selectedGoal.value
        if (goal > 0 && newCount == goal) {
            // Extended vibration when reaching the goal
            VibrationHelper.goalReachedVibration(context)
            _goalReachedDialog.value = true
        }

        // 4. Update repository in background
        viewModelScope.launch {
            repository.incrementTasbeeh(dhikr.id)
        }
    }

    fun dismissGoalDialog() {
        _goalReachedDialog.value = false
    }

    fun resetCounter() {
        val dhikr = _selectedDhikr.value ?: return
        _currentCounter.value = 0
        viewModelScope.launch {
            repository.resetDhikr(dhikr.id)
        }
    }

    fun toggleSalawat(enabled: Boolean) {
        val context = getApplication<Application>()
        _isSalawatEnabled.value = enabled
        SalawatScheduler.setReminderEnabled(context, enabled, _salawatInterval.value)
    }

    fun setSalawatInterval(minutes: Int) {
        val context = getApplication<Application>()
        _salawatInterval.value = minutes
        SalawatScheduler.saveIntervalMinutes(context, minutes)
        if (_isSalawatEnabled.value) {
            SalawatScheduler.setReminderEnabled(context, true, minutes)
        }
    }

    fun testSalawatReminder() {
        val context = getApplication<Application>()
        SalawatAudioPlayer.playSalawatReminderAudio(context)
        SalawatNotificationHelper.showSalawatNotification(context)
    }

    // Morning & Evening Actions
    fun incrementMorningEvening(id: Int, category: MorningEveningCategory) {
        val context = getApplication<Application>()
        VibrationHelper.tapHaptic(context)
        morningEveningRepository.incrementDhikr(id, category)
    }

    fun resetMorningEveningDhikr(id: Int, category: MorningEveningCategory) {
        morningEveningRepository.resetDhikr(id, category)
    }

    fun resetMorningEveningCategory(category: MorningEveningCategory) {
        morningEveningRepository.resetCategory(category)
    }

    fun toggleMorningEveningReminder(enabled: Boolean) {
        val context = getApplication<Application>()
        _isMorningEveningEnabled.value = enabled
        MorningEveningScheduler.updateSettings(
            context = context,
            enabled = enabled,
            morningHour = _morningHour.value,
            morningMinute = _morningMinute.value,
            eveningHour = _eveningHour.value,
            eveningMinute = _eveningMinute.value,
            repeatInterval = _meRepeatInterval.value
        )
    }

    fun setMorningEveningRepeat(interval: Int) {
        val context = getApplication<Application>()
        _meRepeatInterval.value = interval
        if (_isMorningEveningEnabled.value) {
            MorningEveningScheduler.updateSettings(
                context = context,
                enabled = true,
                repeatInterval = interval
            )
        }
    }

    fun setMorningTime(hour: Int, minute: Int) {
        val context = getApplication<Application>()
        _morningHour.value = hour
        _morningMinute.value = minute
        if (_isMorningEveningEnabled.value) {
            MorningEveningScheduler.updateSettings(
                context = context,
                enabled = true,
                morningHour = hour,
                morningMinute = minute
            )
        }
    }

    fun setEveningTime(hour: Int, minute: Int) {
        val context = getApplication<Application>()
        _eveningHour.value = hour
        _eveningMinute.value = minute
        if (_isMorningEveningEnabled.value) {
            MorningEveningScheduler.updateSettings(
                context = context,
                enabled = true,
                eveningHour = hour,
                eveningMinute = minute
            )
        }
    }

    fun testMorningEveningReminder(isMorning: Boolean) {
        val context = getApplication<Application>()
        // Silent notification only: no ringtone, no TTS, no chime
        if (isMorning) {
            MorningEveningNotificationHelper.showMorningNotification(context)
        } else {
            MorningEveningNotificationHelper.showEveningNotification(context)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = TasbeehDatabase.getInstance(application)
                    val repo = TasbeehRepository(db.tasbeehDao())
                    val meRepo = MorningEveningRepository(application)
                    return TasbeehViewModel(application, repo, meRepo) as T
                }
            }
    }
}
