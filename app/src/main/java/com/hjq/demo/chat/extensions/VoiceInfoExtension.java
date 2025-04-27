package com.hjq.demo.chat.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.base.IExtensionProvider;

/**
 * @author r
 * @date 2024/11/15
 * @description Brief description of the file content.
 */
public class VoiceInfoExtension implements IExtension {
    private int voiceDuration;

    public static final String NAMESPACE = "urn:xmpp:sim:voice-info:0";
    public static final String ELEMENT_NAME = "voice-info";

    public VoiceInfoExtension(int voiceDuration) {
        this.voiceDuration = voiceDuration;
    }

    public int getVoiceDuration() {
        return voiceDuration;
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
        HashMap<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("voiceDuration", voiceDuration);
        return propertyMap;
    }

    public static class Provider implements IExtensionProvider<VoiceInfoExtension> {
        @Override
        public VoiceInfoExtension createExtension(Map<String, String> extraData) {
            String voiceDuration = extraData.get("voiceDuration");
            if (voiceDuration == null) {
                return null;
            }
            return new VoiceInfoExtension(Integer.parseInt(voiceDuration));
        }

        @Override
        public List<String> getProperty() {
            List<String> lists = new ArrayList<>();
            lists.add("voiceDuration");
            return lists;
        }
    }
}
