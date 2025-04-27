package com.hjq.demo.chat.utils;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.model.ait.AtContactsModel;

import java.util.Comparator;

import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;


public class JimUtil {
    final static String DEFAULT_LATEST_MESSAGE = "[消息]";

    public static String getMessageContent(String messageType, String messageContent) {
        if (SmartContentType.TEXT.equals(messageType)) {
            return messageContent;
        } else if (SmartContentType.IMAGE.equals(messageType)) {
            return "[" + StringUtils.getString(R.string.image) + "]";
        } else if (SmartContentType.VOICE.equals(messageType)) {
            return "[" + StringUtils.getString(R.string.voice) + "]";
        } else if (ChatMessage.isCallMsgType(messageType)) {
            return "[" + StringUtils.getString(R.string.call_msg) + "]";
        } else if (SmartContentType.VIDEO.equals(messageType)) {
            return"[" + StringUtils.getString(R.string.video) + "]";
        } else {
            return DEFAULT_LATEST_MESSAGE;
        }
    }

    public static String truncateConversationTitle(String title) {
        title = User.getAccountById(title);
        int maxLength = 10; // 限定的最大长度
        int preserveStart = 5; // 保留开头的字符数
        int preserveEnd = 4; // 保留结尾的字符数

        if (title.length() > maxLength) {
            String start = title.substring(0, preserveStart); // 前5个字符
            String end = title.substring(title.length() - preserveEnd); // 后4个字符
            return start + "···" + end;
        } else {
            return title; // 如果长度不超过10个字符，返回原始字符串
        }
    }

    public static AtContactsModel getAitBlockFromMsg(SmartMessage message) {
        if (message != null) {
        }
        return null;
    }

    /**
     * 会话排序比较器
     */
    public static Comparator<ConversationInfo> simpleComparator() {
        return (a, b) -> {
            if ((a.isPinned() && b.isPinned()) || (!a.isPinned() && !b.isPinned())) {
                return Long.compare(b.getLastMsgDate(), a.getLastMsgDate());
            } else if (a.isPinned() && !b.isPinned()) {
                return -1;
            } else {
                return 1;
            }
        };
    }
}
