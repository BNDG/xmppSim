package com.hjq.demo.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author r
 * @date 2024/7/29
 * @description Brief description of the file content.
 */
public class ChatVideoBean extends ChatFileBean implements Parcelable {
    private String thumbnailLocalPath;
    private String videoLocalPath;
    private long duration;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private String thumbnailUrl;
    private String videoUrl;

    public String getThumbnailLocalPath() {
        return thumbnailLocalPath;
    }

    public void setThumbnailLocalPath(String thumbnailLocalPath) {
        this.thumbnailLocalPath = thumbnailLocalPath;
    }

    public String getVideoLocalPath() {
        return videoLocalPath;
    }

    public void setVideoLocalPath(String videoLocalPath) {
        this.videoLocalPath = videoLocalPath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.thumbnailLocalPath);
        dest.writeString(this.videoLocalPath);
        dest.writeLong(this.duration);
        dest.writeInt(this.thumbnailWidth);
        dest.writeInt(this.thumbnailHeight);
        dest.writeString(this.thumbnailUrl);
        dest.writeString(this.videoUrl);
    }

    public ChatVideoBean() {
    }

    protected ChatVideoBean(Parcel in) {
        super(in);
        this.thumbnailLocalPath = in.readString();
        this.videoLocalPath = in.readString();
        this.duration = in.readLong();
        this.thumbnailWidth = in.readInt();
        this.thumbnailHeight = in.readInt();
        this.thumbnailUrl = in.readString();
        this.videoUrl = in.readString();
    }

    public static final Creator<ChatVideoBean> CREATOR = new Creator<ChatVideoBean>() {
        @Override
        public ChatVideoBean createFromParcel(Parcel source) {
            return new ChatVideoBean(source);
        }

        @Override
        public ChatVideoBean[] newArray(int size) {
            return new ChatVideoBean[size];
        }
    };
}
