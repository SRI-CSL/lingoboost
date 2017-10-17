package edu.northwestern.langlearn;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by ttm on 10/17/17.
 * Sourced from https://stackoverflow.com/a/38487142
 */

public class LoopingMediaPlayer {
    private static final String TAG = LoopingMediaPlayer.class.getSimpleName();

    private Context mContext = null;
    private int mResId   = 0;
    private int mCounter = 1;
    private float playerVolume = 1.0f;

    private MediaPlayer mCurrentPlayer = null;
    private MediaPlayer mNextPlayer    = null;

    public static LoopingMediaPlayer create(Context context, int resId) {
        return new LoopingMediaPlayer(context, resId);
    }

    private LoopingMediaPlayer(Context context, int resId) {
        mContext = context;
        mResId   = resId;

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

    public void start() throws IllegalStateException {
        mCurrentPlayer.start();
    }

    public void stop() throws IllegalStateException {
        mCurrentPlayer.stop();
    }

    public void pause() throws IllegalStateException {
        mCurrentPlayer.pause();
    }

    public void release() {
        mCurrentPlayer.release();
        mNextPlayer.release();
    }

    public void reset() {
        mCurrentPlayer.reset();
    }
}
