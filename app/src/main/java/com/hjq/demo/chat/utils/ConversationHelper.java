// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.utils;

import com.hjq.demo.chat.entity.ChatEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话模块帮助类
 */
public class ConversationHelper {

    // 会话中是否包含@消息，内存缓存
    private static Map<String, Boolean> aitInfo = new HashMap<>();

    /**
     * 更新会话中是否包含@消息
     *
     * @param sessionIdList 会话ID列表
     * @param hasAit        是否包含@消息
     */
    public static void updateAitInfo(List<String> sessionIdList, boolean hasAit) {
        if (sessionIdList != null) {
            for (String sessionId : sessionIdList) {
                aitInfo.put(sessionId, hasAit);
            }
        }
    }

    /**
     * 更新某个会话是否 包含@消息
     */
    public static void updateAitInfo(String sessionId, boolean hasAit) {
        aitInfo.put(sessionId, hasAit);
        if(hasAit) {
            // 会话界面更新@消息
            ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
            event.obj = sessionId;
            EventBus.getDefault().post(event);
        }
    }

    /**
     * 获取会话中是否包含@消息
     *
     * @param sessionId 会话ID
     * @return 是否包含@消息
     */
    public static boolean hasAit(String sessionId) {
        if (aitInfo.containsKey(sessionId)) {
            return aitInfo.get(sessionId);
        }
        return false;
    }
}
