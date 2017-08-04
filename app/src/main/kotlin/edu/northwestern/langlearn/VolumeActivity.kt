package edu.northwestern.langlearn

import android.content.Intent
import android.graphics.PointF
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar

import java.io.IOException

import kotlinx.android.synthetic.main.activity_volume.*

import org.jetbrains.anko.contentView

import com.agilie.volumecontrol.animation.controller.ControllerImpl
import com.agilie.volumecontrol.getPointOnBorderLineOfCircle
import com.agilie.volumecontrol.view.VolumeControlView
import com.agilie.volumecontrol.view.VolumeControlView.Companion.CONTROLLER_SPACE

class VolumeActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)
        mediaPlayer = MediaPlayer.create(this, R.raw.kvinnan)

        words_volume.setStartPercent(50)
        seek_bar_words.progress = 50
        white_noise_volume.setStartPercent(25)
        seek_bar_white_noise.progress = 25

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)



        words_volume.controller?.onTouchControllerListener = (object : ControllerImpl.OnTouchControllerListener {
            override fun onControllerDown(angle: Int, percent: Int) { }
            override fun onControllerMove(angle: Int, percent: Int) { }
            override fun onAngleChange(angle: Int, percent: Int) {
                seek_bar_words.setProgress(percent)
                playAudioRaw(percent / 100f)
            }
        })
        white_noise_volume.controller?.onTouchControllerListener = (object : ControllerImpl.OnTouchControllerListener {
            override fun onControllerDown(angle: Int, percent: Int) { }
            override fun onControllerMove(angle: Int, percent: Int) { }
            override fun onAngleChange(angle: Int, percent: Int) {
                seek_bar_white_noise.setProgress(percent)
                playAudioRaw(percent / 100f)
            }
        })
        seek_bar_words.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = if (fromUser) setVolumeControlViewProgress(words_volume, progress) else { }
            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })
        seek_bar_white_noise.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = if (fromUser) setVolumeControlViewProgress(white_noise_volume, progress) else { }
            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })
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

                val tIntent: Intent = Intent(this, TestActivity::class.java)

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
}
