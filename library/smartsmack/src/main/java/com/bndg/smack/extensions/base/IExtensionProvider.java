package com.bndg.smack.extensions.base;

import java.util.List;
import java.util.Map;

/**
 * @author r
 * @date 2024/11/13
 * @description 解析类实现接口
 */
public interface IExtensionProvider<T extends IExtension> {
    List<String> getProperty();
    T createExtension(Map<String, String> extraData);
}
