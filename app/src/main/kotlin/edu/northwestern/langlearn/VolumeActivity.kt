package edu.northwestern.langlearn

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PointF
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar

import kotlinx.android.synthetic.main.activity_volume.*

import org.jetbrains.anko.contentView

import com.agilie.volumecontrol.animation.controller.ControllerImpl
import com.agilie.volumecontrol.getPointOnBorderLineOfCircle
import com.agilie.volumecontrol.view.VolumeControlView
import com.agilie.volumecontrol.view.VolumeControlView.Companion.CONTROLLER_SPACE

inline fun VolumeControlView.onTouchChangeVolume(bar: AppCompatSeekBar, crossinline body: (vol: Float) -> Unit) {
    this.controller?.onTouchControllerListener = (object : ControllerImpl.OnTouchControllerListener {
        override fun onControllerDown(angle: Int, percent: Int) { }
        override fun onControllerMove(angle: Int, percent: Int) { }
        override fun onAngleChange(angle: Int, percent: Int) {
            bar.setProgress(percent)
            body(percent / 100f)
        }
    })
}

inline fun AppCompatSeekBar.onProgressChangeVolume(v: VolumeControlView, crossinline body: (v: VolumeControlView, progress: Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = if (fromUser) body(v, progress) else { }
        override fun onStartTrackingTouch(seekBar: SeekBar) { }
        override fun onStopTrackingTouch(seekBar: SeekBar) { }
    })
}

inline fun SharedPreferences.edit(body: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()

    editor.body()
    editor.apply() // not kotlin apply, prefs apply!
}

fun SharedPreferences.Editor.put(pair: Pair<String, Any>) {
    val key = pair.first
    val value = pair.second

    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Boolean -> putBoolean(key, value)
        is Long -> putLong(key, value)
        is Float -> putFloat(key, value)
        else -> error("Only primitive types can be stored in SharedPreferences")
    }
}

class VolumeActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        mediaPlayer = MediaPlayer.create(this, R.raw.kvinnan)
        checkSharedPreferences()
        words_volume.onTouchChangeVolume(seek_bar_words) { vol -> playAudioRaw(vol) }
        white_noise_volume.onTouchChangeVolume(seek_bar_white_noise) { playAudioRaw(it) }
        seek_bar_words.onProgressChangeVolume(words_volume) { v, p -> setVolumeControlViewProgress(v, p) }
        seek_bar_white_noise.onProgressChangeVolume(white_noise_volume) { v, p -> setVolumeControlViewProgress(v, p) }
        volume_next.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "next OnClickListener")

            if (words_volume.visibility == View.VISIBLE) {
                words_volume.visibility = View.GONE
                seek_bar_words.visibility = View.GONE
                text_view_word_playback.visibility = View.GONE
                white_noise_volume.visibility = View.VISIBLE
                seek_bar_white_noise.visibility = View.VISIBLE
                text_view_white_noise.visibility = View.VISIBLE
                destroyPlayer()
                mediaPlayer = MediaPlayer.create(this, R.raw.bnoise3)
                mediaPlayer?.seekTo(45000)
                mediaPlayer?.setLooping(true)
                mediaPlayer?.start()
            } else {
                words_volume.visibility = View.VISIBLE
                seek_bar_words.visibility = View.VISIBLE
                text_view_word_playback.visibility = View.VISIBLE
                white_noise_volume.visibility = View.GONE
                seek_bar_white_noise.visibility = View.GONE
                text_view_white_noise.visibility = View.GONE

                val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
                val tIntent: Intent = Intent(this, TestActivity::class.java)

                prefs.edit {
                    put(MainActivity.VOLUME_WORDS_PREF to seek_bar_words.progress)
                    put(MainActivity.VOLUME_WHITE_NOISE_PREF to seek_bar_white_noise.progress)
                }
                startActivity(tIntent)
            }
        })
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        destroyPlayer()
        super.onDestroy()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        destroyPlayer()
        super.onStop()
    }

    private fun setVolumeControlViewProgress(v: VolumeControlView, progress: Int) {
        val w: Int = v.width
        val h: Int = v.height
        val controllerCenter = PointF().apply {
            x = w / 2f
            y = h / 2f
        }
        val controllerRadius = if (w > h) h / CONTROLLER_SPACE else w / CONTROLLER_SPACE
        val restoreTouchPoint = getPointOnBorderLineOfCircle(controllerCenter, controllerRadius, (progress *  360) / 100)
        val motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, restoreTouchPoint.x, restoreTouchPoint.y, 0)

        v.onTouch(contentView!!, motionEvent)
    }

    private fun playAudioRaw(volume: Float) {
        Log.d(TAG, "playAudioRaw")
        mediaPlayer?.setVolume(volume, volume)

        if (!(mediaPlayer?.isPlaying() ?: true)) {
            mediaPlayer?.start()
        }
    }

    private fun destroyPlayer() {
        if (mediaPlayer?.isPlaying() ?: false) {
            mediaPlayer?.stop()
        }

        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun checkSharedPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        var wordsPercent: Int = 0
        var whiteNoisePercent: Int = 0

        prefs.apply {
            wordsPercent = getInt(MainActivity.VOLUME_WORDS_PREF, MainActivity.WORDS_VOLUME_PREF_DEFAULT)
            whiteNoisePercent = getInt(MainActivity.VOLUME_WHITE_NOISE_PREF, MainActivity.WHITE_NOISE_VOLUME_PREF_DEFAULT)
            edit {
                put(MainActivity.VOLUME_WORDS_PREF to wordsPercent)
                put(MainActivity.VOLUME_WHITE_NOISE_PREF to whiteNoisePercent)
            }
        }

        words_volume.setStartPercent(wordsPercent)
        seek_bar_words.progress = wordsPercent
        white_noise_volume.setStartPercent(whiteNoisePercent)
        seek_bar_white_noise.progress = whiteNoisePercent
    }
}
