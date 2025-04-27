package com.hjq.demo.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author r
 * @date 2024/11/14
 * @description Brief description of the file content.
 */
public class ChatImageBean extends ChatFileBean implements Parcelable {
    private int imageWidth;
    private int imageHeight;


    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.imageWidth);
        dest.writeInt(this.imageHeight);
    }

    public ChatImageBean() {
    }

    protected ChatImageBean(Parcel in) {
        super(in);
        this.imageWidth = in.readInt();
        this.imageHeight = in.readInt();
    }

    public static final Creator<ChatImageBean> CREATOR = new Creator<ChatImageBean>() {
        @Override
        public ChatImageBean createFromParcel(Parcel source) {
            return new ChatImageBean(source);
        }

        @Override
        public ChatImageBean[] newArray(int size) {
            return new ChatImageBean[size];
        }
    };
}
