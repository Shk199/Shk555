package com.example.bubble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.TasbeehDatabase
import com.example.data.model.DhikrItem
import com.example.data.repository.TasbeehRepository
import com.example.util.VibrationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.hypot

class TasbihBubbleService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var windowManager: WindowManager
    private var floatingView: FrameLayout? = null
    private lateinit var layoutParams: WindowManager.LayoutParams

    private lateinit var repository: TasbeehRepository
    private var azkarList: List<DhikrItem> = emptyList()
    private var currentDhikr: DhikrItem? = null
    private var currentCount: Int = 0

    // UI References inside the floating view
    private var tvDhikrTitle: TextView? = null
    private var tvCounter: TextView? = null
    private var btnTasbeehCircle: LinearLayout? = null

    companion object {
        const val ACTION_STOP = "com.example.bubble.ACTION_STOP"
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "tasbeeh_bubble_channel_v1"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val dao = TasbeehDatabase.getInstance(applicationContext).tasbeehDao()
        repository = TasbeehRepository(dao)

        startForegroundNotification()
        initFloatingBubble()
        observeAzkarData()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            SmartTasbeehManager.setEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "التسبيح الذكي (الفقاعة العائمة)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار تشغيل ميزة التسبيح الذكي العائمة فوق التطبيقات الأخرى"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            201,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TasbihBubbleService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            202,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("التسبيح الذكي مفعّل ✨")
            .setContentText("اضغط على الفقاعة للتسبيح من أي تطبيق")
            .setOngoing(true)
            .setContentIntent(pendingOpenApp)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف التسبيح الذكي", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun initFloatingBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val (savedX, savedY) = SmartTasbeehManager.getBubblePosition(this)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        val root = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
        }

        val density = resources.displayMetrics.density
        fun dp(dp: Float): Int = (dp * density).toInt()

        // 1. Main Capsule Container
        val capsuleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4f), dp(4f), dp(8f), dp(4f))

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(32f).toFloat()
                setColor(Color.parseColor("#151028")) // Deep dark purple
                setStroke(dp(1.8f), Color.parseColor("#7C3AED")) // Accent vibrant purple
            }
            background = bg
            elevation = dp(8f).toFloat()
        }

        // 2. Action Circle ("سبّح")
        val circleBtn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val circleSize = dp(48f)
            layoutParams = LinearLayout.LayoutParams(circleSize, circleSize).apply {
                marginEnd = dp(8f)
            }

            val circleBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#221940"))
                setStroke(dp(1.5f), Color.parseColor("#A78BFA"))
            }
            background = circleBg
        }
        btnTasbeehCircle = circleBtn

        val tvTasbeehAction = TextView(this).apply {
            text = "سبِّح"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        circleBtn.addView(tvTasbeehAction)

        // 3. Info Section (Dhikr Name & Live Counter)
        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2f), dp(2f), dp(4f), dp(2f))
        }

        val titleTv = TextView(this).apply {
            text = "سُبْحَانَ اللَّهِ"
            setTextColor(Color.parseColor("#C4B5FD"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            maxWidth = dp(120f)
        }
        tvDhikrTitle = titleTv

        val countTv = TextView(this).apply {
            text = "0"
            setTextColor(Color.parseColor("#FBBF24")) // Warm gold
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = Typeface.DEFAULT_BOLD
        }
        tvCounter = countTv

        infoLayout.addView(titleTv)
        infoLayout.addView(countTv)

        // Allow tapping info section to switch to next Dhikr
        infoLayout.setOnClickListener {
            cycleToNextDhikr()
        }

        capsuleContainer.addView(circleBtn)
        capsuleContainer.addView(infoLayout)

        // Wrap capsule with margin for close button
        val capsuleParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(8f), dp(8f), 0)
        }
        root.addView(capsuleContainer, capsuleParams)

        // 4. Close Mini Button ("✕")
        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val closeSize = dp(20f)
            layoutParams = FrameLayout.LayoutParams(closeSize, closeSize).apply {
                gravity = Gravity.TOP or Gravity.END
            }

            val closeBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#E11D48")) // Clean red
                setStroke(dp(1f), Color.WHITE)
            }
            background = closeBg
            elevation = dp(10f).toFloat()

            setOnClickListener {
                SmartTasbeehManager.setEnabled(this@TasbihBubbleService, false)
                stopSelf()
            }
        }
        root.addView(closeBtn)

        // 5. Touch and Drag Logic
        setupTouchListener(root)

        floatingView = root
        try {
            windowManager.addView(root, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTouchListener(root: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (!isDragging && hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        layoutParams.x = initialX + dx
                        layoutParams.y = initialY + dy
                        try {
                            windowManager.updateViewLayout(floatingView, layoutParams)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        SmartTasbeehManager.saveBubblePosition(this, layoutParams.x, layoutParams.y)
                    } else {
                        // User tapped the bubble -> Tasbeeh count
                        performTasbeehClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun performTasbeehClick() {
        val dhikr = currentDhikr ?: return

        // 1. Light tactile vibration
        VibrationHelper.tapHaptic(this)

        // 2. Increment local count & update UI immediately
        currentCount++
        tvCounter?.text = currentCount.toString()

        // 3. Quick tap spring scale animation
        btnTasbeehCircle?.animate()
            ?.scaleX(0.88f)
            ?.scaleY(0.88f)
            ?.setDuration(50)
            ?.withEndAction {
                btnTasbeehCircle?.animate()
                    ?.scaleX(1.0f)
                    ?.scaleY(1.0f)
                    ?.setDuration(70)
                    ?.start()
            }
            ?.start()

        // 4. Check goal achievement vibration
        if (dhikr.targetCount > 0 && currentCount == dhikr.targetCount) {
            VibrationHelper.goalReachedVibration(this)
        }

        // 5. Persist into Room Database & DailyStat
        serviceScope.launch(Dispatchers.IO) {
            try {
                repository.incrementTasbeeh(dhikr.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cycleToNextDhikr() {
        if (azkarList.isEmpty()) return
        val currentIndex = azkarList.indexOfFirst { it.id == currentDhikr?.id }
        val nextIndex = if (currentIndex >= 0 && currentIndex < azkarList.size - 1) {
            currentIndex + 1
        } else {
            0
        }
        val nextDhikr = azkarList[nextIndex]
        currentDhikr = nextDhikr
        currentCount = nextDhikr.todayCount
        SmartTasbeehManager.setCurrentDhikrId(this, nextDhikr.id)
        updateDhikrDisplay()
        VibrationHelper.tapHaptic(this)
    }

    private fun observeAzkarData() {
        serviceScope.launch {
            repository.allAzkar.collect { list ->
                if (list.isNotEmpty()) {
                    azkarList = list
                    val savedId = SmartTasbeehManager.getCurrentDhikrId(this@TasbihBubbleService)
                    val matched = list.find { it.id == savedId } ?: list.first()
                    currentDhikr = matched
                    currentCount = matched.todayCount
                    updateDhikrDisplay()
                }
            }
        }
    }

    private fun updateDhikrDisplay() {
        val dhikr = currentDhikr ?: return
        val shortName = getShortDhikrName(dhikr.arabicText)
        tvDhikrTitle?.text = shortName
        tvCounter?.text = currentCount.toString()
    }

    private fun getShortDhikrName(fullText: String): String {
        return when {
            fullText.contains("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ") -> "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"
            fullText.contains("لا إلهَ إلاَّ اللَّهُ") -> "لا إله إلا الله"
            fullText.contains("أَسْتَغْفِرُ اللَّهَ") -> "أَسْتَغْفِرُ اللَّهَ"
            fullText.contains("اللَّهُمَّ صَلِّ وَسَلِّمْ") -> "الصلاة على النبي"
            fullText.contains("لا حَوْلَ وَلا قُوَّةَ") -> "الحَوْقَلَة"
            fullText.contains("حَسْبُنَا اللَّهُ") -> "حَسْبُنَا اللَّهُ"
            fullText.length > 25 -> fullText.take(24) + "…"
            else -> fullText
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        floatingView?.let { view ->
            try {
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
        SmartTasbeehManager.setEnabled(this, false)
    }
}
