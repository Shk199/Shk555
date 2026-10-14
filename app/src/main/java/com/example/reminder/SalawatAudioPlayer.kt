package com.example.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.widget.Toast
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object SalawatAudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Checks if the phone is currently in silent or vibrate mode.
     */
    fun isSilentOrVibrate(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        val ringerMode = audioManager.ringerMode
        return ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }

    /**
     * Checks if the phone is in NORMAL ringer mode, meaning sound is allowed to play.
     */
    fun canPlayAudio(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        return audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    fun togglePlay(context: Context) {
        if (_isPlaying.value) {
            pause()
        } else {
            if (isSilentOrVibrate(context)) {
                Toast.makeText(context, "الهاتف في الوضع الصامت، لن يتم تشغيل الصوت", Toast.LENGTH_SHORT).show()
            } else {
                play(context)
            }
        }
    }

    fun play(context: Context) {
        _errorMessage.value = null
        if (!canPlayAudio(context)) {
            android.util.Log.d("SalawatAudioPlayer", "Skipping audio playback because phone is silent/vibrate")
            _isPlaying.value = false
            return
        }

        try {
            if (mediaPlayer == null) {
                initMediaPlayer(context)
            }

            mediaPlayer?.let { player ->
                player.start()
                _isPlaying.value = true
                _duration.value = player.duration.coerceAtLeast(1)
                startProgressTracker()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "تعذر تشغيل المقطع الصوتي: ${e.localizedMessage ?: "خطأ غير معروف"}"
            _isPlaying.value = false
            resetPlayer()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isPlaying.value = false
            progressJob?.cancel()
        }
    }

    fun stop() {
        resetPlayer()
    }

    private fun initMediaPlayer(context: Context) {
        resetPlayer()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        var player: MediaPlayer? = null
        try {
            val afd = context.resources.openRawResourceFd(R.raw.salawat)
            player = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
            }
            afd.close()
        } catch (e: Exception) {
            player = null
        }

        if (player == null) {
            try {
                val afd = context.assets.openFd("salawat.mp3")
                player = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                }
                afd.close()
            } catch (e: Exception) {
                throw IllegalStateException("ملف الصوت salawat.mp3 غير متاح", e)
            }
        }

        player.setOnCompletionListener {
            _isPlaying.value = false
            _progress.value = 0f
            _currentPosition.value = 0
            progressJob?.cancel()
        }

        player.setOnErrorListener { _, what, extra ->
            _errorMessage.value = "خطأ في تشغيل الصوت ($what, $extra)"
            resetPlayer()
            true
        }

        mediaPlayer = player
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                try {
                    val player = mediaPlayer
                    if (player != null && player.isPlaying) {
                        val pos = player.currentPosition
                        val dur = player.duration
                        _currentPosition.value = pos
                        _duration.value = dur
                        if (dur > 0) {
                            _progress.value = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                        }
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    break
                }
                delay(100)
            }
        }
    }

    private fun resetPlayer() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _isPlaying.value = false
        _progress.value = 0f
        _currentPosition.value = 0
    }

    /**
     * Plays the included salawat.mp3 audio file exclusively for reminders and testing.
     * Uses USAGE_NOTIFICATION to respect notification audio channels and system silent modes.
     * Checks ringerMode explicitly: if phone is in SILENT or VIBRATE mode, it immediately returns
     * and produces zero sound.
     */
    fun playSalawatReminderAudio(context: Context) {
        // 1. Strictly verify phone ringer mode: if SILENT or VIBRATE, stop immediately
        if (!canPlayAudio(context)) {
            android.util.Log.d("SalawatAudioPlayer", "Phone is in silent or vibrate mode, skipping salawat audio playback")
            return
        }

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            var player: MediaPlayer? = null
            try {
                val afd = context.resources.openRawResourceFd(R.raw.salawat)
                player = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                }
                afd.close()
            } catch (e: Exception) {
                player = null
            }

            if (player == null) {
                try {
                    val afd = context.assets.openFd("salawat.mp3")
                    player = MediaPlayer().apply {
                        setAudioAttributes(audioAttributes)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        prepare()
                    }
                    afd.close()
                } catch (e: Exception) {
                    player = null
                }
            }

            player?.apply {
                isLooping = false
                setOnCompletionListener { mp ->
                    try {
                        mp.stop()
                        mp.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                setOnErrorListener { mp, _, _ ->
                    try {
                        mp.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                    true
                }
                start()
            }
        } catch (e: Exception) {
            android.util.Log.e("SalawatAudioPlayer", "Error playing salawat reminder", e)
        }
    }

    /**
     * Backward-compatible alias for one-shot reminder audio.
     */
    fun playStandalone(context: Context) {
        playSalawatReminderAudio(context)
    }
}
