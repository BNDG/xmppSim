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
public class VideoInfoExtension implements IExtension {
    private String thumbnailUrl;
    private long duration;
    private int thumbnailWidth;
    private int thumbnailHeight;

    public static final String NAMESPACE = "urn:xmpp:sim:video-info:0";
    public static final String ELEMENT_NAME = "video-info";

    public VideoInfoExtension(String thumbnailUrl, long duration, int thumbnailWidth, int thumbnailHeight) {
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
    }

    public long getDuration() {
        return duration;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
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
        propertyMap.put("thumbnailUrl", thumbnailUrl);
        propertyMap.put("duration", duration);
        propertyMap.put("thumbnailWidth", thumbnailWidth);
        propertyMap.put("thumbnailHeight", thumbnailHeight);
        return propertyMap;
    }

    public static class Provider implements IExtensionProvider<VideoInfoExtension> {
        @Override
        public VideoInfoExtension createExtension(Map<String, String> extraData) {
            String thumbnailUrl = extraData.get("thumbnailUrl");
            String duration = extraData.get("duration");
            String thumbnailWidth = extraData.get("thumbnailWidth");
            String thumbnailHeight = extraData.get("thumbnailHeight");
            return new VideoInfoExtension(thumbnailUrl, Long.parseLong(duration), Integer.parseInt(thumbnailWidth), Integer.parseInt(thumbnailHeight));
        }

        @Override
        public List<String> getProperty() {
            List<String> lists = new ArrayList<>();
            lists.add("thumbnailUrl");
            lists.add("duration");
            lists.add("thumbnailWidth");
            lists.add("thumbnailHeight");
            return lists;
        }
    }

}
