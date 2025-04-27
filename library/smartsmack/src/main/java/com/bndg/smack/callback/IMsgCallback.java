package com.bndg.smack.callback;


import com.bndg.smack.entity.SmartMessage;

/**
 * @author r
 * @date 2024/11/22
 * @description 发送消息回调
 */

public interface IMsgCallback {
    void onSuccess(SmartMessage msgEntity);
    void onError(int code, String desc);
  }
