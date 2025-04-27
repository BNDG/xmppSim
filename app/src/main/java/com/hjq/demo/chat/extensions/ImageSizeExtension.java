package com.hjq.demo.chat.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.base.IExtensionProvider;

/**
 * @author r
 * @date 2024/11/14
 * @description Brief description of the file content.
 */
public class ImageSizeExtension implements IExtension {
    private final int imageWidth;
    private final int imageHeight;

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public static final String NAMESPACE = "urn:xmpp:sim:image-size:0";
    public static final String ELEMENT_NAME = "image-size";

    public ImageSizeExtension(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
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
        propertyMap.put("imageWidth", imageWidth);
        propertyMap.put("imageHeight", imageHeight);
        return propertyMap;
    }

    public static class Provider implements IExtensionProvider<ImageSizeExtension> {
        @Override
        public ImageSizeExtension createExtension(Map<String, String> extraData) {
            String imageWidth1 = extraData.get("imageWidth");
            String imageHeight1 = extraData.get("imageHeight");
            if (imageWidth1 == null || imageHeight1 == null) {
                return null;
            }
            return new ImageSizeExtension(Integer.parseInt(imageWidth1), Integer.parseInt(imageHeight1));
        }

        @Override
        public List<String> getProperty() {
            List<String> lists = new ArrayList<>();
            lists.add("imageWidth");
            lists.add("imageHeight");
            return lists;
        }
    }
}
