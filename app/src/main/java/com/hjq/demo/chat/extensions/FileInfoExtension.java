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
 * @description 文件信息扩展
 */
public class FileInfoExtension implements IExtension {
    private String fileName;
    private String fileSize;

    public static final String NAMESPACE = "urn:xmpp:sim:file-info:0";
    public static final String ELEMENT_NAME = "file-info";

    public FileInfoExtension(String fileName, String fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
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
        propertyMap.put("fileName", fileName);
        propertyMap.put("fileSize", fileSize);
        return propertyMap;
    }

    public static class Provider implements IExtensionProvider<FileInfoExtension> {
        @Override
        public FileInfoExtension createExtension(Map<String, String> extraData) {
            String fileName = extraData.get("fileName");
            String fileSize = extraData.get("fileSize");
            return new FileInfoExtension(fileName, fileSize);
        }

        @Override
        public List<String> getProperty() {
            List<String> lists = new ArrayList<>();
            lists.add("fileName");
            lists.add("fileSize");
            return lists;
        }
    }
}
