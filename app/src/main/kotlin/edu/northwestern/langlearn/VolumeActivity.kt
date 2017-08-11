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
    private var mediaPlayerWhiteNoise: MediaPlayer? = null
    private val isSleep by lazy { intent.getBooleanExtra("isSleep", false) }
    private val isTest by lazy { intent.getBooleanExtra("isTest", false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        checkSharedPreferences()

        Log.d(TAG, "isSleep: $isSleep")
        Log.d(TAG, "isTest: $isTest")
        text_view_word_playback.text = if (isSleep)
            "Set the word volume so that you can clearly understand the word through the white noise, but at a volume that is comfortable for sleeping."
        else
            "Set the word volume loud enough so that you can clearly understand the word."
        text_view_set_volume.text = if (isSleep)
            "Set Sleep Volume"
        else
            "Set Test Volume"

        createPlayer()

        if (isTest) showWordsVolume()

        words_volume.onTouchChangeVolume(seek_bar_words) { vol -> changeVolumeAndPlay(vol) }
        white_noise_volume.onTouchChangeVolume(seek_bar_white_noise) { mediaPlayerWhiteNoise?.setVolume(it, it) }
        seek_bar_words.onProgressChangeVolume(words_volume) { v, p -> setVolumeControlViewProgress(v, p) }
        seek_bar_white_noise.onProgressChangeVolume(white_noise_volume) { v, p -> setVolumeControlViewProgress(v, p) }
        volume_next.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "next OnClickListener")

            isSleepPartOne {
                if (it) showWordsVolume()
                else next()
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

    override fun onBackPressed() {
        isSleepPartOne {
            if (!it && isSleep) showWhiteNoiseVolume()
            else super.onBackPressed()
        }
    }

    private fun next() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val toIntent: Intent = if (isSleep) {
            Intent(this, SleepMode::class.java)
        } else { // isTest
            Intent(this, TestActivity::class.java)
        }

        prefs.edit {
            put(MainActivity.VOLUME_WORDS_PREF to seek_bar_words.progress)
            put(MainActivity.VOLUME_WHITE_NOISE_PREF to seek_bar_white_noise.progress)
        }
        startActivity(toIntent)
    }

    private fun isSleepPartOne(func: ((isSleepP1: Boolean) -> Unit)? = null): Boolean {
        val pOne = if (isTest) false else white_noise_volume.visibility == View.VISIBLE

        func?.invoke(pOne)
        return pOne
    }

    private fun createPlayer() {
        if (isSleep) {
            mediaPlayerWhiteNoise = MediaPlayer.create(this, R.raw.bnoise3)
            mediaPlayerWhiteNoise?.seekTo(45000)
            mediaPlayerWhiteNoise?.setLooping(true)
            mediaPlayerWhiteNoise?.setVolume(seek_bar_white_noise.progress.toFloat(), seek_bar_white_noise.progress.toFloat())
            mediaPlayerWhiteNoise?.start()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.kvinnan)
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

    private fun changeVolumeAndPlay(volume: Float) {
        Log.d(TAG, "changeVolumeAndPlay")
        mediaPlayer?.setVolume(volume, volume)

        if (!(mediaPlayer?.isPlaying() ?: true)) {
            mediaPlayer?.start()
        }
    }

    private fun destroyPlayer() {
        if (mediaPlayer?.isPlaying() ?: false) {
            mediaPlayer?.stop()
        }

        if (mediaPlayerWhiteNoise?.isPlaying() ?: false) {
            mediaPlayerWhiteNoise?.stop()
        }

        mediaPlayer?.release()
        mediaPlayer = null
        mediaPlayerWhiteNoise?.release()
        mediaPlayerWhiteNoise = null;
    }

    private fun showWordsVolume() {
        white_noise_volume.visibility = View.GONE
        seek_bar_white_noise.visibility = View.GONE
        text_view_white_noise.visibility = View.GONE
        words_volume.visibility = View.VISIBLE
        seek_bar_words.visibility = View.VISIBLE
        text_view_word_playback.visibility = View.VISIBLE
    }

    private fun showWhiteNoiseVolume() {
        white_noise_volume.visibility = View.VISIBLE
        seek_bar_white_noise.visibility = View.VISIBLE
        text_view_white_noise.visibility = View.VISIBLE
        words_volume.visibility = View.GONE
        seek_bar_words.visibility = View.GONE
        text_view_word_playback.visibility = View.GONE
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
