package com.hjq.demo.chat.entity;

import com.hjq.demo.utils.JsonParser;

/**
 * @author r
 * @date 2024/7/17
 * @description 由于群聊可以接收任何人的挂断消息，所以需要判断是否是主持人的消息
 */
public class CallCreatorInfo {
    public String creatorJid;
    public String creatorName;
    public String callServiceUrl;

    public static String create(String userId, String userNickName, String callServiceUrl) {
        CallCreatorInfo creatorInfo = new CallCreatorInfo();
        creatorInfo.creatorJid = userId;
        creatorInfo.creatorName = userNickName;
        creatorInfo.callServiceUrl = callServiceUrl;
        return JsonParser.serializeToJson(creatorInfo);
    }
}
