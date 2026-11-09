package com.zenlauncher.minimalist.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.zenlauncher.minimalist.R

class ZenSoundEngine(private val context: Context) {

    private val soundPool: SoundPool
    private val vibrator: Vibrator?

    private var soundTapId: Int = 0
    private var soundOpenId: Int = 0
    private var soundDrawerId: Int = 0
    private var soundBackId: Int = 0
    private var soundTickId: Int = 0

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundTapId = soundPool.load(context, R.raw.zen_tap, 1)
        soundOpenId = soundPool.load(context, R.raw.zen_open, 1)
        soundDrawerId = soundPool.load(context, R.raw.zen_drawer, 1)
        soundBackId = soundPool.load(context, R.raw.zen_back, 1)
        soundTickId = soundPool.load(context, R.raw.zen_tick, 1)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun triggerHaptic(effectType: Int = -1) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = if (effectType >= 0) {
                VibrationEffect.createPredefined(effectType)
            } else {
                VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    fun playTap() {
        if (isSoundEnabled && soundTapId != 0) {
            soundPool.play(soundTapId, 0.7f, 0.7f, 1, 0, 1.0f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            triggerHaptic(VibrationEffect.EFFECT_CLICK)
        } else {
            triggerHaptic()
        }
    }

    fun playOpen() {
        if (isSoundEnabled && soundOpenId != 0) {
            soundPool.play(soundOpenId, 0.85f, 0.85f, 1, 0, 1.0f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            triggerHaptic(VibrationEffect.EFFECT_HEAVY_CLICK)
        } else {
            triggerHaptic()
        }
    }

    fun playDrawer() {
        if (isSoundEnabled && soundDrawerId != 0) {
            soundPool.play(soundDrawerId, 0.65f, 0.65f, 1, 0, 1.0f)
        }
        triggerHaptic()
    }

    fun playBack() {
        if (isSoundEnabled && soundBackId != 0) {
            soundPool.play(soundBackId, 0.6f, 0.6f, 1, 0, 1.0f)
        }
        triggerHaptic()
    }

    fun playTick() {
        if (isSoundEnabled && soundTickId != 0) {
            soundPool.play(soundTickId, 0.45f, 0.45f, 1, 0, 1.0f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            triggerHaptic(VibrationEffect.EFFECT_TICK)
        } else {
            triggerHaptic()
        }
    }

    fun release() {
        soundPool.release()
    }
}
