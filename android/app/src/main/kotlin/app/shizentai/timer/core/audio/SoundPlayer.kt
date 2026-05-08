package app.shizentai.timer.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.shizentai.timer.R
import app.shizentai.timer.engine.AudioCue
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the four bundled WAV cues into a SoundPool and plays them on demand.
 * SoundPool is right for short, low-latency cues; MediaPlayer would over-allocate
 * for these <1s clips and adds state-machine churn we don't need.
 */
@Singleton
class SoundPlayer @Inject constructor(@ApplicationContext context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids: Map<AudioCue, Int> = mapOf(
        AudioCue.WORK_START to pool.load(context, R.raw.gong_twice, 1),
        AudioCue.WORK_WARNING to pool.load(context, R.raw.alert, 1),
        AudioCue.REST_START to pool.load(context, R.raw.gong, 1),
        AudioCue.REST_ENDING to pool.load(context, R.raw.rest_end, 1),
        AudioCue.FINISHED to pool.load(context, R.raw.gong, 1),
    )

    fun play(cue: AudioCue) {
        val id = ids[cue] ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
