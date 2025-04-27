package com.bndg.smack.extensions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;

/**
 * @author r
 * @date 2024/9/12
 * @description 消息类型扩展
 */

public class MessageTypeExtension implements ExtensionElement {
    public static final String ELEMENT = "message-type";
    public static final String NAMESPACE = "urn:xmpp:sim-message-type";

    private final String type;

    public MessageTypeExtension(String type) {
        this.type = type;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toXML(XmlEnvironment xmlEnvironment) {
        return "<" + ELEMENT + " xmlns='" + NAMESPACE + "'>" + type + "</" + ELEMENT + ">";
    }

    public String getType() {
        return type;
    }
}