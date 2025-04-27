package com.hjq.demo.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.hjq.demo.utils.JsonParser;

/**
 * @author r
 * @date 2024/5/30
 * @description Brief description of the file content.
 */

public class ChatVoiceBean extends ChatFileBean implements Parcelable {
    public int getVoiceDuration() {
        return voiceDuration;
    }

    public void setVoiceDuration(int voiceDuration) {
        this.voiceDuration = voiceDuration;
    }

    public int voiceDuration = 0;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.voiceDuration);
    }

    public ChatVoiceBean() {
    }

    protected ChatVoiceBean(Parcel in) {
        super(in);
        this.voiceDuration = in.readInt();
    }

    public static final Creator<ChatVoiceBean> CREATOR = new Creator<ChatVoiceBean>() {
        @Override
        public ChatVoiceBean createFromParcel(Parcel source) {
            return new ChatVoiceBean(source);
        }

        @Override
        public ChatVoiceBean[] newArray(int size) {
            return new ChatVoiceBean[size];
        }
    };

    public static String getVoiceTime(String msg) {
        ChatVoiceBean chatVoiceMessage = JsonParser.deserializeByJson(msg, ChatVoiceBean.class);
        if (chatVoiceMessage != null) {
            String showTime;
            String mStr;
            String sStr;
            if (chatVoiceMessage.voiceDuration >= 60) {
                int m = chatVoiceMessage.voiceDuration / 60;
                int s = chatVoiceMessage.voiceDuration % 60;
                mStr = String.valueOf(m);
                sStr = String.valueOf(s);
                if (m < 10) {
                    mStr = "0$m";
                }
                if (s < 10) {
                    sStr = "0$s";
                }
            } else {
                mStr = "00";
                sStr = chatVoiceMessage.voiceDuration < 10 ?
                        "0" + chatVoiceMessage.voiceDuration
                        : String.valueOf(chatVoiceMessage.voiceDuration);
            }
            showTime = String.format("%s:%s", mStr, sStr);
            return showTime;
        }
        return "";
    }
}

