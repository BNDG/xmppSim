package com.bndg.smack.extensions.base;

import java.util.List;
import java.util.Map;

/**
 * @author r
 * @date 2024/11/13
 * @description Provider工厂类
 */
public class ProviderFactory {
    public static BaseExtensionElementProvider<BaseExtensionElement> createProvider(IExtensionProvider<? extends IExtension> provider) {
        return new BaseExtensionElementProvider<BaseExtensionElement>() {
            @Override
            protected List<String> getProperty() {
                return provider.getProperty();
            }

            @Override
            protected BaseExtensionElement createInstance(Map<String, String> extraData) {
                return ElementFactory.createBaseExtensionElement(provider.createExtension(extraData));
            }
        };
    }
}
