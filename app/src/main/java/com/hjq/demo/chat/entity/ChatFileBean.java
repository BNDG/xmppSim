package com.hjq.demo.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author r
 * @date 2024/7/30
 * @description Brief description of the file content.
 */
public class ChatFileBean implements Parcelable {

    public String originId;
    public String conversationId;
    public String contactTitle;
    public String conversationType;
    public String fileUrl;
    // 文件类型
    public String fileType;
    public String fileName;
    public String fileSize;
    public String fileLocalPath;
    // 消息类型
    public String msgType;

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContactTitle() {
        return contactTitle;
    }

    public void setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.originId);
        dest.writeString(this.conversationId);
        dest.writeString(this.contactTitle);
        dest.writeString(this.conversationType);
        dest.writeString(this.fileUrl);
        dest.writeString(this.fileType);
        dest.writeString(this.fileName);
        dest.writeString(this.fileSize);
        dest.writeString(this.fileLocalPath);
        dest.writeString(this.msgType);
    }

    public ChatFileBean() {
    }

    protected ChatFileBean(Parcel in) {
        this.originId = in.readString();
        this.conversationId = in.readString();
        this.contactTitle = in.readString();
        this.conversationType = in.readString();
        this.fileUrl = in.readString();
        this.fileType = in.readString();
        this.fileName = in.readString();
        this.fileSize = in.readString();
        this.fileLocalPath = in.readString();
        this.msgType = in.readString();
    }

    public static final Creator<ChatFileBean> CREATOR = new Creator<ChatFileBean>() {
        @Override
        public ChatFileBean createFromParcel(Parcel source) {
            return new ChatFileBean(source);
        }

        @Override
        public ChatFileBean[] newArray(int size) {
            return new ChatFileBean[size];
        }
    };
}
