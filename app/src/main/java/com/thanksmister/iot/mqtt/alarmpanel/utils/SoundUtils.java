package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.content.Context;
import android.os.Handler;

import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.thanksmister.iot.mqtt.alarmpanel.AlarmSounds;
import com.thanksmister.iot.mqtt.alarmpanel.BoardDefaults;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by michaelritchie on 8/25/17.
 */

public class SoundUtils {

    private static final long PLAYBACK_BEEP_DELAY = 800;
    private Handler mHandler;
    private Speaker speaker;
    private Context context;

    public SoundUtils(Context context) {
        this.context = context;
    }

    public void destroyBuzzer() {

        // handle buzzer
        if (speaker != null) {
            try {
                speaker.stop();
                speaker.close();
            } catch (Exception e) {
                Timber.e("Error closing speaker " + e.getMessage());
            } finally {
                speaker = null;
            }
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mPlaybackRunnable);
        }
    }

    private void initSpeaker() {

        if(speaker == null) {
            try {
                speaker = new Speaker(BoardDefaults.getPwmPin());
                speaker.stop(); // in case the PWM pin was enabled already
            } catch (IOException e) {
                Timber.e("Error initializing speaker");
            }
        }
    }

    public void playBuzzerOnButtonPress() {

        destroyBuzzer();
        stopBuzzerRepeat(); // stop the buzzer if
        initSpeaker();

        // if speaker is null try media player
        if(speaker != null) {
            double note = AlarmSounds.A4;
            try {
                speaker.play(note);
                Thread.sleep(100);
                speaker.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopBuzzerRepeat() {
        if(mHandler != null) {
            mHandler.removeCallbacks(mPlaybackRunnable);
            mHandler = null;
        }
    }

    public void playBuzzerRepeat() {
        mHandler = new Handler();
        mHandler.post(mPlaybackRunnable);
    }

    private Runnable mPlaybackRunnable = new Runnable() {
        @Override
        public void run() {
            initSpeaker();
            if(speaker != null) {
                try {
                    double note = AlarmSounds.A4;
                    speaker.play(note);
                    Thread.sleep(200);
                    speaker.stop();
                    mHandler.postDelayed(mPlaybackRunnable, PLAYBACK_BEEP_DELAY);
                } catch (Exception e) {
                    Timber.e("Error initializing speaker");
                }
            }
        }
    };
}