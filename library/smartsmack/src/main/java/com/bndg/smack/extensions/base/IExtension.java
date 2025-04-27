package com.bndg.smack.extensions.base;

import java.util.Map;

/**
 * @author r
 * @date 2024/11/13
 * @description 扩展信息接口
 */
public interface IExtension {
    String getNamespace();
    String getElementName();
    Map<String, Object> getExtraData();
}
