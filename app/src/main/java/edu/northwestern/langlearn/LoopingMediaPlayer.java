package edu.northwestern.langlearn;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttm on 10/17/17.
 * Sourced from https://stackoverflow.com/a/38487142
 */

public class LoopingMediaPlayer {
    private static final String TAG = LoopingMediaPlayer.class.getSimpleName();

    private static final float MAX_VOLUME = 1.0f;
    private static final float MIN_VOLUME = 0.0f;

    private Context mContext = null;
    private int mResId = 0;
    private int mCounter = 1;
    private float playerVolume = 1.0f;

    private MediaPlayer mCurrentPlayer = null;
    private MediaPlayer mNextPlayer = null;

    public static LoopingMediaPlayer create(Context context, int resId) {
        return new LoopingMediaPlayer(context, resId);
    }

    private LoopingMediaPlayer(Context context, int resId) {
        mContext = context;
        mResId = resId;

        mCurrentPlayer = MediaPlayer.create(mContext, mResId);
        mCurrentPlayer.setVolume(playerVolume, playerVolume);
        mCurrentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mCurrentPlayer.start();
            }
        });
        createNextMediaPlayer();
    }

    private void createNextMediaPlayer() {
        mNextPlayer = MediaPlayer.create(mContext, mResId);
        mNextPlayer.setVolume(playerVolume, playerVolume);
        mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
        mCurrentPlayer.setOnCompletionListener(onCompletionListener);
    }

    private final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.release();
            mCurrentPlayer = mNextPlayer;
            createNextMediaPlayer();
            Log.d(TAG, String.format("Loop #%d", ++mCounter));
        }
    };

    // code-read additions:
    public boolean isPlaying() throws IllegalStateException {
        return mCurrentPlayer.isPlaying();
    }

    public void setVolume(float volume) {
        playerVolume = volume;
        mCurrentPlayer.setVolume(playerVolume, playerVolume);
        if (mNextPlayer != null) {
            mNextPlayer.setVolume(playerVolume, playerVolume);
        }
    }

    public void linearRampVolume(float targetVolume, int fadeDuration) {
        linearRampVolume(targetVolume, fadeDuration, 10);
    }

    public void linearRampVolume(float targetVolume, int fadeDuration, int updatePeriod) {
        if (fadeDuration > 0) {
            final float updateThreshold = 0.025f;
            final int updateDelay = 10;
            final float volumeAdjustment = (targetVolume - playerVolume) * updateDelay / fadeDuration;
            final Timer volumeRampTimer = new Timer(true);

            if (targetVolume < MIN_VOLUME) {
                targetVolume = MIN_VOLUME;
            } else if (targetVolume > MAX_VOLUME) {
                targetVolume = MAX_VOLUME;
            }

            final float target = targetVolume;

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    final float volumeChange = Math.abs(playerVolume - target);

                    try {
                        // Set volume to targetVolume and stop the timer task if it is within the update threshold,
                        // or if we have decreased the volume to below the target, or increased the volume to above the target
                        if (volumeChange <= updateThreshold
                                || volumeAdjustment < 0 && playerVolume + volumeAdjustment < target
                                || volumeAdjustment > 0 && playerVolume + volumeAdjustment > target) {
                            setVolume(target);
                            volumeRampTimer.cancel();
                            volumeRampTimer.purge();
                        } else {
                            setVolume(playerVolume + volumeAdjustment);
                        }
                    } catch (IllegalStateException ex) {
                        Log.e(TAG, "Failed to set the MediaPlayer volume");
                        volumeRampTimer.cancel();
                        volumeRampTimer.purge();
                    }
                }
            };

            volumeRampTimer.schedule(timerTask, updatePeriod, updatePeriod);
        }
    }

    public void start() throws IllegalStateException {
        mCurrentPlayer.start();
    }

    public void stop() throws IllegalStateException {
        mCurrentPlayer.stop();
    }

    public void pause() throws IllegalStateException {
        mCurrentPlayer.pause();
    }

    public void seekTo(int msec) {
        mCurrentPlayer.seekTo(msec);
    }

    public void release() {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.release();
        }
        if (mNextPlayer != null) {
            mNextPlayer.release();
        }
    }

    public void reset() {
        mCurrentPlayer.reset();
    }
}
