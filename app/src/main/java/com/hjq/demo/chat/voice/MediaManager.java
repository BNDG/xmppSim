package com.hjq.demo.chat.voice;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.hjq.demo.chat.cons.Constant;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author r
 * @date 2024/9/20
 * @description 播放语音
 */

public class MediaManager {

    private static volatile MediaManager instance;
    private final MediaPlayer mPlayer;
    private boolean isPlaying;
    private OnCompletionListener onCompletionListener;
    private AudioManager audioManager;

    public void setCurrentPlayFile(String currentPlayFile) {
        this.currentPlayFile = currentPlayFile;
    }

    private String currentPlayFile = "";

    private MediaManager(Context context) {
        currentPlayFile = "";
        mPlayer = getMediaPlayer(context);
        mPlayer.setOnErrorListener((mp, what, extra) -> {
            mp.reset();
            currentPlayFile = "";
            return true; // 表示已经处理了错误
        });
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    }

    public static MediaManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MediaManager.class) {
                if (instance == null) {
                    instance = new MediaManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void playSound(String filePathString, OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
        try {
            mPlayer.reset(); // 重置 MediaPlayer
            mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL); // 设置音频流类型
            mPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    onCompletionListener.onCompletion(mp);
                }
            }); // 设置完成监听器
            mPlayer.setDataSource(filePathString); // 设置数据源
            mPlayer.setVolume(0.9f, 0.9f); // 设置音量
            mPlayer.setLooping(false); // 不循环
            mPlayer.prepare(); // 准备播放
            mPlayer.start(); // 开始播放
            // 控制音频输出
            if (SPUtils.getInstance().getBoolean(Constant.USE_SPEAKERPHONE, false)) {
                setSpeakerphoneOn(false); // 使用听筒
            } else {
                setSpeakerphoneOn(true); // 使用扬声器
            }
            isPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setSpeakerphoneOn(boolean on) {
        //切换到外放前，必须设置模式为MODE_IN_CALL.
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(on);
    }

    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPlaying = false;
        }
    }

    public void reset() {
        if (mPlayer.isPlaying()) {
            mPlayer.reset();
            currentPlayFile = "";
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(mPlayer);
            }
        }
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void resume() {
        if (!mPlayer.isPlaying() && isPlaying) {
            mPlayer.start();
            isPlaying = true;
        }
    }

    public void release() {
        if (mPlayer != null) {
            currentPlayFile = "";
            mPlayer.release();
            instance = null;
        }
    }

    public MediaPlayer getMediaPlayer(Context context) {
        MediaPlayer mediaplayer = new MediaPlayer();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return mediaplayer;
        }
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
            Object subtitleInstance = constructor.newInstance(context, null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                return mediaplayer;
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaplayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null);
        } catch (Exception e) {
            Log.d("mediaManager", "getMediaPlayer crash ,exception = " + e);
        }
        return mediaplayer;
    }

    public boolean isCurentPlayFile(String messageContent) {
        return currentPlayFile.equals(messageContent);
    }
}
