// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.views.ait;

import android.content.Context;
import android.media.tv.AitInfo;

import com.hjq.demo.chat.model.ait.AtContactsModel;
import com.hjq.demo.chat.utils.JimUtil;
import com.hjq.demo.utils.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bndg.smack.entity.SmartMessage;

/**
 * 用于@功能服务类，用于管理@信息，包括接收到的@信息，本地保存的@信息，以及发送@信息事件
 */
public class AitService {

    private static final String TAG = "AitService";
    private static AitService instance;
    private final Map<String, AitInfo> aitInfoMapCache = new HashMap<>();
    private final CopyOnWriteArrayList<AitInfo> updateList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<AitInfo> insertList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<AitInfo> deleteList = new CopyOnWriteArrayList<>();
    private Context mContext;
    private boolean hasRegister;

    private AitService() {
    }

    public static AitService getInstance() {
        if (instance == null) {
            instance = new AitService();
        }
        return instance;
    }

    // 初始化
    public void init(Context context) {
        Trace.d("init");
        mContext = context;
        // 监听登录状态
        // 获取数据库ait消息
      /*      AitDBHelper.getInstance(context).openWrite();
            List<AitInfo> result = AitDBHelper.getInstance(context).queryAll();
            List<AitInfo> aitInfoList = new ArrayList<>();
            for (AitInfo aitInfo : params) {
                if (TextUtils.equals(aitInfo.getAccountId(), IMKitClient.account())
                        || TextUtils.equals(
                        AtContactsModel.ACCOUNT_ALL, aitInfo.getAccountId())) {
                    if (!aitInfoMapCache.containsKey(aitInfo.getConversationId())) {
                        aitInfoMapCache.put(aitInfo.getConversationId(), aitInfo);
                        Trace.d(TAG, "init,load,add cache:" + aitInfo.getConversationId());
                    }
                    aitInfoList.add(aitInfo);
                    Trace.d(TAG, "init,load:" + aitInfo.getConversationId());
                }
            }
            if (aitInfoList.size() > 0) {
                sendAitEvent(aitInfoList, AitEvent.AitEventType.Load);
            }*/
    }

    // 清除某个会话@记录
    public void clearAitInfo(String conversationId) {
        if (mContext == null) {
            return;
        }
        Trace.d("clearAitInfo:" + conversationId);
/*        AitInfo aitInfo = aitInfoMapCache.remove(conversationId);
        if (aitInfo == null) {
            aitInfo = new AitInfo();
            aitInfo.setConversationId(conversationId);
        }
        List<AitInfo> aitInfoList = new ArrayList<>();
        aitInfoList.add(aitInfo);
        sendAitEvent(aitInfoList, AitEvent.AitEventType.Clear);
        deleteList.add(aitInfo);
        CoroutineUtils.runIO(deleteCoroutine);*/
    }

    // 发送加载@信息事件
    public void sendLocalAitEvent() {
        if (mContext == null) {
            return;
        }
        // 发送加载@信息事件
    }

    // 发送@信息事件，如果收到@信息则发送事件，会话列表接收到事件之后，在相关的会话中增加@提示。事件包括加载、清理和新增
    public void sendAitEvent(List<AitInfo> aitInfoList) {
        if (aitInfoList == null || mContext == null) {
            return;
        }
    }

    // 注册消息监听,监听接受到的消息和撤回消息
    public void registerObserver() {
        Trace.d("registerObserver");
        // 发送@信息事件，如果收到@信息则发送事件，会话列表接收到事件之后，在相关的会话中增加@提示。事件包括加载、清理和新增
    }

    // 取消注册消息监听
    public void unRegisterObserver() {
        Trace.d("unRegisterObserver");
    }

    // 解析接受到的消息，获取@信息，如果有则本地保存
    private Map<String, AitInfo> parseMessage(SmartMessage msg) {
        Trace.d("parseMessage");
        Map<String, AitInfo> aitInfoMap = new HashMap<>();
        AtContactsModel aitModel = JimUtil.getAitBlockFromMsg(msg);
        if (aitModel != null) {
            List<String> aitAccount = aitModel.getAtTeamMember();
            for (String account : aitAccount) {
            }
        }
        return aitInfoMap;
    }

    // 更新@信息
    public void updateAitInfo(Map<String, AitInfo> aitInfoMap) {
        if (mContext == null) {
            return;
        }

        for (String conversationId : aitInfoMap.keySet()) {
            AitInfo newAitInfo = aitInfoMap.get(conversationId);
            if (newAitInfo == null) {
                continue;
            }
            if (aitInfoMapCache.containsKey(conversationId)) {
                AitInfo cacheAitInfo = aitInfoMapCache.get(conversationId);
                if (cacheAitInfo != null) {
                }
            } else {
                insertList.add(newAitInfo);
                aitInfoMapCache.put(conversationId, newAitInfo);
            }
        }
        // 更新本地数据库@信息
    }

    // 删除@信息
    public void removeAitInfo(Map<String, AitInfo> aitInfoMap) {
        if (mContext == null) {
            return;
        }

        List<AitInfo> notifyDelete = new ArrayList<>();
        for (String conversationId : aitInfoMap.keySet()) {
            AitInfo newAitInfo = aitInfoMap.get(conversationId);
            if (newAitInfo == null) {
                continue;
            }
            if (aitInfoMapCache.containsKey(conversationId)) {
                AitInfo cacheAitInfo = aitInfoMapCache.get(conversationId);
                if (cacheAitInfo != null) {
                    //发送
                    deleteList.add(cacheAitInfo);
                    notifyDelete.add(cacheAitInfo);
                    aitInfoMapCache.remove(conversationId);
                    //更新
                    updateList.add(cacheAitInfo);
                }
            } else {
                deleteList.add(newAitInfo);
                notifyDelete.add(newAitInfo);
            }
        }
        if (notifyDelete.size() > 0) {
            // 发送@信息事件清理
        }
        // 更新本地数据库@信息
        // 删除本地数据库中@信息
    }

    // 删除本地数据库中@信息
    private void deleteAit() {
        List<String> conversationList = new ArrayList<>();
        for (AitInfo info : deleteList) {
        }
        deleteList.clear();
    }

    // 更新本地数据库@信息
    public void updateAit() {
        for (AitInfo info : updateList) {
        }
        for (AitInfo insertInfo : insertList) {
        }
        insertList.clear();
        updateList.clear();
    }

}
