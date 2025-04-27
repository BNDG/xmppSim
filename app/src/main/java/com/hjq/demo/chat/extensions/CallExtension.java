package com.hjq.demo.chat.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.base.IExtensionProvider;

/**
 * @author r
 * @date 2024/6/19
 * @description 通话消息自定义扩展
 */
public class CallExtension implements IExtension {
    public static final String NAMESPACE = "urn:xmpp:call:3";
    public static final String ELEMENT_NAME = "request";

    // 通话ID
    private String callId;
    // 通话类型
    private String type;
    // 通话用户ID
    private String callUserIds;
    // 通话创建者信息
    private String callCreatorInfo;
    // 通话服务器地址
    private String serviceUrl;

    // 构造函数
    public CallExtension(String callId, String type, String callUserIds, String callCreatorInfo, String serviceUrl) {
        this.callId = callId;
        this.type = type;
        this.callUserIds = callUserIds;
        this.callCreatorInfo = callCreatorInfo;
        this.serviceUrl = serviceUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getCallCreatorInfo() {
        return callCreatorInfo;
    }

    public String getCallUserIds() {
        return callUserIds;
    }

    public String getCallId() {
        return callId;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }


    @Override
    public Map<String, Object> getExtraData() {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("callId", callId);
        stringObjectHashMap.put("type", type);
        stringObjectHashMap.put("callUserIds", callUserIds);
        stringObjectHashMap.put("callCreatorInfo", callCreatorInfo);
        stringObjectHashMap.put("serviceUrl", serviceUrl);
        return stringObjectHashMap;
    }

    public static class Provider implements IExtensionProvider<CallExtension> {
        @Override
        public List<String> getProperty() {
            List<String> lists = new ArrayList<>();
            lists.add("callId");
            lists.add("type");
            lists.add("callUserIds");
            lists.add("callCreatorInfo");
            lists.add("serviceUrl");
            return lists;
        }
        @Override
        public CallExtension createExtension(Map<String, String> extraData) {
            if (extraData != null) {
                String callId = extraData.get("callId");
                String type = extraData.get("type");
                String callUserIds = extraData.get("callUserIds");
                String callCreatorInfo = extraData.get("callCreatorInfo");
                String serviceUrl = extraData.get("serviceUrl");
                return new CallExtension(callId, type, callUserIds, callCreatorInfo, serviceUrl);
            }
            return null;
        }
    }
}
