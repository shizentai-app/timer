package app.shizentai.timer.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import app.shizentai.timer.engine.Phase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Haptics @Inject constructor(@ApplicationContext context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun heavy() {
        vibrator?.takeIf { it.hasVibrator() }
            ?.vibrate(VibrationEffect.createOneShot(60L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun medium() {
        vibrator?.takeIf { it.hasVibrator() }
            ?.vibrate(VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun forPhaseChange(phase: Phase) {
        when (phase) {
            Phase.WORK, Phase.REST, Phase.FINISHED -> heavy()
            Phase.PREPARE, Phase.WARNING -> medium()
            Phase.IDLE -> Unit
        }
    }
}
