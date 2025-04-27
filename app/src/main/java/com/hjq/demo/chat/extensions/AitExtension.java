package com.hjq.demo.chat.extensions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.base.IExtensionProvider;

/**
 * @author r
 * @date 2024/6/19
 * @description @消息自定义扩展
 */
public class AitExtension implements IExtension {
    public static final String NAMESPACE = "urn:xmpp:sim:mention:2";
    public static final String ELEMENT_NAME = "mention";

    public String getJsonData() {
        return jsonData;
    }

    private String jsonData;

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    // 构造函数
    public AitExtension(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public Map<String, Object> getExtraData() {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("jsonData", jsonData);
        return stringObjectHashMap;
    }

    public static class Provider implements IExtensionProvider<AitExtension> {
        @Override
        public List<String> getProperty() {
            return Collections.singletonList("jsonData");
        }

        @Override
        public AitExtension createExtension(Map<String, String> extraData) {
            if (extraData != null) {
                String jsonData = extraData.get("jsonData");
                return new AitExtension(jsonData);
            }
            return null;
        }
    }
}
