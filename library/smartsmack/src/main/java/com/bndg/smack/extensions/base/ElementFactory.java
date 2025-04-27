package com.bndg.smack.extensions.base;

import java.util.Map;

/**
 * @author r
 * @date 2024/11/13
 * @description
 */
public class ElementFactory {
    public static BaseExtensionElement createBaseExtensionElement(IExtension baseExtension) {
        return new BaseExtensionElement(baseExtension.getNamespace(), baseExtension.getElementName()) {

            @Override
            protected Map<String, Object> getExtraData() {
                return baseExtension.getExtraData();
            }
        };
    }
}
