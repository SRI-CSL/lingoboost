package edu.northwestern.langlearn

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.media.AudioManager
import android.os.Handler

import org.jetbrains.anko.startActivity

import kotlinx.android.synthetic.main.activity_volume.*

inline fun AppCompatSeekBar.onProgressChangeVolume(crossinline body: (seekBar: SeekBar, progress: Float) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = if (fromUser) body(seekBar, progress / 100f) else { }
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
        is String -> putString(key, value as String)

        is Int -> putInt(key, value as Int)
        is Boolean -> putBoolean(key, value as Boolean)
        is Long -> putLong(key, value as Long)
        is Float -> putFloat(key, value as Float)
        else -> error("Only primitive types can be stored in SharedPreferences")
    }
}

class VolumeActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayerWhiteNoise: MediaPlayer? = null
    private var hasWhiteNoiseBeenAdjusted = false
    private var hasWhordsBeenAdjusted = false
    private lateinit var updateSysStreamProgresRunner: Runnable
    private var updateSysStreamProgressHandler: Handler? = Handler()
    private val isSleep by lazy { intent.getBooleanExtra("isSleep", false) }
    private val isTest by lazy { intent.getBooleanExtra("isTest", false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        checkSharedPreferences()
        setVolumeControlStream(AudioManager.STREAM_MUSIC)

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

        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val sysStreamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC) // 0 .. 15
        Log.d(TAG, "MAX: ${ am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }")
        val sysStreamVolumeProgress = Math.round((sysStreamVolume / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()) * 100f)

        Log.d(TAG, "SysStreamVolume: $sysStreamVolume")
        volume_next.isEnabled = false
        seek_bar_sys_stream_volume.progress = sysStreamVolumeProgress
        seek_bar_sys_stream_volume.isFocusable = false
        seek_bar_sys_stream_volume.onProgressChangeVolume { seekBar, vol ->
            seekBar.progress = sysStreamVolumeProgress
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_SHOW_UI)
        }
        updateSysStreamProgresRunner = object: Runnable {
            override fun run() {
                seek_bar_sys_stream_volume.progress = Math.round((am.getStreamVolume(AudioManager.STREAM_MUSIC) / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()) * 100f)
                updateSysStreamProgressHandler?.postDelayed(this, 200)
            }
        }
        createPlayer()

        if (isTest) showWordsVolume()

        seek_bar_white_noise.onProgressChangeVolume { seekBar, vol ->
            enableNextTouch(whiteNoiseAdjusted = true) { mediaPlayerWhiteNoise?.setVolume(vol, vol) }
        }
        seek_bar_words.onProgressChangeVolume { seekBar, vol ->
            enableNextTouch(wordsAdjusted = true) { changeVolumeAndPlay(vol) }
        }
        volume_next.setOnClickListener {
            Log.d(TAG, "next OnClickListener")
            next()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        destroyPlayer()

        updateSysStreamProgressHandler = null
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        updateSysStreamProgressHandler?.postDelayed(updateSysStreamProgresRunner, 200)
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        destroyPlayer()
        updateSysStreamProgressHandler?.removeCallbacks(updateSysStreamProgresRunner);
        super.onStop()
    }

    private fun next() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        prefs.edit {
            put(MainActivity.VOLUME_WORDS_PREF to seek_bar_words.progress)
            put(MainActivity.VOLUME_WHITE_NOISE_PREF to seek_bar_white_noise.progress)
        }

        if (isSleep) {
            startActivity<SleepMode>()
        } else { // isTest
            startActivity<TestActivity>()
        }
    }

    private fun enableNextTouch(whiteNoiseAdjusted: Boolean? = null, wordsAdjusted: Boolean? = null, func:() -> Unit) {
        hasWhiteNoiseBeenAdjusted = whiteNoiseAdjusted ?: hasWhiteNoiseBeenAdjusted
        hasWhordsBeenAdjusted = wordsAdjusted ?: hasWhordsBeenAdjusted

        when {
            isTest -> volume_next.isEnabled = hasWhordsBeenAdjusted
            isSleep -> volume_next.isEnabled = hasWhiteNoiseBeenAdjusted && hasWhordsBeenAdjusted
        }

        func.invoke()
    }

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
        bar_volumes.visibility = View.GONE
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
