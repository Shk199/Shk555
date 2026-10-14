package com.example.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object AudioSoundHelper {

    /**
     * Plays the Salawat recitation audio (or fallback chime) exclusively for
     * the "الصلاة على النبي ﷺ" reminder section.
     */
    fun playSalawatSound(context: Context) {
        if (!SalawatAudioPlayer.canPlayAudio(context)) {
            return
        }
        try {
            SalawatAudioPlayer.playStandalone(context)
        } catch (e: Exception) {
            playSynthesizedChime()
        }
    }

    /**
     * Backward-compatible alias for Salawat sound.
     */
    fun playSalawatChime(context: Context) {
        playSalawatSound(context)
    }

    /**
     * Fallback chime for Salawat if media player fails.
     */
    fun playSynthesizedChime() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 2.4
                val numSamples = (durationSeconds * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                val freq1 = 329.63
                val freq2 = 415.30
                val freq3 = 493.88
                val freq4 = 659.25

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val decay = Math.exp(-2.2 * time)

                    val wave1 = sin(2.0 * Math.PI * freq1 * time) * 0.35
                    val wave2 = sin(2.0 * Math.PI * freq2 * time) * 0.25
                    val wave3 = sin(2.0 * Math.PI * freq3 * time) * 0.20
                    val wave4 = sin(2.0 * Math.PI * freq4 * time) * 0.20

                    val combined = (wave1 + wave2 + wave3 + wave4) * decay
                    samples[i] = (combined * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
