package edu.northwestern.langlearn

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.view.View
import android.widget.SeekBar

import kotlinx.android.synthetic.main.activity_volume.*

inline fun AppCompatSeekBar.onProgressChangeVolume(crossinline body: (progress: Float) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = if (fromUser) body(progress / 100f) else { }
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
    private var hasWhiteNoiseBeenAdjusted = false
    private var hasWhordsBeenAdjusted = false
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
            "Sleep Volume"
        else
            "Test Volume"
        volume_next.text = if (isSleep)
            "Ready for Sleep"
        else
            "Ready for Test"

        volume_next.isEnabled = false
        createPlayer()

        if (isTest) showWordsVolume()

        seek_bar_white_noise.onProgressChangeVolume {
            mediaPlayerWhiteNoise?.setVolume(it, it)
            hasWhiteNoiseBeenAdjusted = true
            enableNextTouch()
        }
        seek_bar_words.onProgressChangeVolume { vol ->
            changeVolumeAndPlay(vol)
            hasWhordsBeenAdjusted = true
            enableNextTouch()
        }
        volume_next.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "next OnClickListener")
            next()
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

    //override fun onBackPressed() {
    //    isSleepPartOne {
    //        when {
    //            isTest -> super.onBackPressed()
    //            !it -> showWhiteNoiseVolume()
    //            else -> super.onBackPressed()
    //        }
    //    }
    //}

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

    private fun enableNextTouch() {
        when {
            isTest -> volume_next.isEnabled = hasWhordsBeenAdjusted
            isSleep -> volume_next.isEnabled = hasWhiteNoiseBeenAdjusted && hasWhordsBeenAdjusted
        }
    }

    //private fun isSleepPartOne(func: ((isSleepP1: Boolean) -> Unit)? = null): Boolean {
    //    val pOne = if (isTest) false else white_noise_volume.visibility == View.VISIBLE
    //
    //    func?.invoke(pOne)
    //    return pOne
    //}

    private fun createPlayer() {
        if (isSleep) {
            mediaPlayerWhiteNoise = MediaPlayer.create(this, R.raw.bnoise3)
            mediaPlayerWhiteNoise?.seekTo(45000)
            mediaPlayerWhiteNoise?.setLooping(true)
            mediaPlayerWhiteNoise?.setVolume(seek_bar_white_noise.progress / 100f, seek_bar_white_noise.progress / 100f)
            mediaPlayerWhiteNoise?.start()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.kvinnan)
        changeVolumeAndPlay(seek_bar_words.progress / 100f)
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
        bar_heading.visibility = View.GONE
        text_view_heading_white_noise.visibility = View.GONE
        lay_white_noise.visibility = View.GONE
        text_view_white_noise.visibility = View.GONE
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

        seek_bar_words.progress = wordsPercent
        seek_bar_white_noise.progress = whiteNoisePercent
    }
}
