package com.bndg.smack.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.base.IExtensionProvider;

public class TestGroupMembersExtension implements IExtension {
    public static final String NAMESPACE = "urn:xmpp:sim-group-data:3";
    public static final String ELEMENT_NAME = "group-data";


    private String roomId;
    // jid的格式
    private String memberIds;

    private String memberNicknames;

    public TestGroupMembersExtension(String roomId, String memberIds, String senderNickname) {
        this.roomId = roomId;
        this.memberIds = memberIds;
        this.memberNicknames = senderNickname;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public Map<String, Object> getExtraData() {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("roomId", roomId);
        stringObjectHashMap.put("memberIds", memberIds);
        stringObjectHashMap.put("memberNicknames", memberNicknames);
        return stringObjectHashMap;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public String getMemberIds() {
        return memberIds;
    }

    public String getMemberNicknames() {
        return memberNicknames;
    }

    public static class Provider implements IExtensionProvider<TestGroupMembersExtension> {
        @Override
        public List<String> getProperty() {
            List<String> lists = new ArrayList<>();
            lists.add("roomId");
            lists.add("memberIds");
            lists.add("memberNicknames");
            return lists;
        }

        @Override
        public TestGroupMembersExtension createExtension(Map<String, String> extraData) {
            if (extraData != null) {
                String callId = extraData.get("roomId");
                String type = extraData.get("memberIds");
                String callUserIds = extraData.get("memberNicknames");
                return new TestGroupMembersExtension(callId, type, callUserIds);
            }
            return null;
        }
    }
}