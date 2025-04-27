package com.bndg.smack.extensions.base;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.Map;

/**
 * @author r
 * @date 2024/11/12
 * @description 扩展基类
 */
public abstract class BaseExtensionElement implements ExtensionElement {
    private String namespace;
    private String elementName;

    public BaseExtensionElement(String namespace, String elementName) {
        this.namespace = namespace;
        this.elementName = elementName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public CharSequence toXML(XmlEnvironment xmlEnvironment) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();
        // 获取额外数据：返回一个 Map
        Map<String, Object> extraData = getExtraData();

        if (extraData != null) {
            // 遍历 Map，将每个键值对添加到 XML 中
            xml.halfOpenElement("item");
            for (Map.Entry<String, Object> entry : extraData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    xml.attribute(key, value.toString());
                }
            }
            xml.closeEmptyElement();
        }
        xml.closeElement(this);
        return xml;
    }

    // 抽象方法：子类必须实现，返回 Map<String, Object> 代表子类的属性
    protected abstract Map<String, Object> getExtraData();
}
