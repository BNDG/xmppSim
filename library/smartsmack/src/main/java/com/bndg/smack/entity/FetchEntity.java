package com.bndg.smack.entity;

/**
 * @author r
 * @date 2024/9/24
 * @description 记录消息时间和id 用来处理重连后拉取
 */
public class FetchEntity {
    // 消息存档id 拉取历史消息用的id
    public String archivedId;
    // 消息时间
    public long createTime;
    // 是否需要拉取
    public boolean needPull;
    // id
    public String conversationId;
    // 是否是拉取历史消息
    public boolean isFetchHistory;
    // 是否是单聊消息
    public boolean isSingle;

}
