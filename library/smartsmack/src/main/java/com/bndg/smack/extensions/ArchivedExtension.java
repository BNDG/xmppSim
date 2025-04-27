package com.bndg.smack.extensions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;

/**
 * @author r
 * @date 2024/9/12
 * @description 消息存档扩展 获取拉取历史消息时候消息id
 */

public class ArchivedExtension implements ExtensionElement {
    public static final String ELEMENT = "archived";
    public static final String NAMESPACE = "urn:xmpp:mam:tmp";
    
    private final String id;
    
    public ArchivedExtension(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
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
    public CharSequence toXML(org.jivesoftware.smack.packet.XmlEnvironment xmlEnvironment) {
        return "<" + ELEMENT + " xmlns='" + NAMESPACE + "' id='" + id + "'/>";
    }

    public static class Provider extends ExtensionElementProvider<ArchivedExtension> {
        @Override
        public ArchivedExtension parse(org.jivesoftware.smack.xml.XmlPullParser xmlPullParser, int i, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException, SmackParsingException {
            String id = xmlPullParser.getAttributeValue(null, "id");
            return new ArchivedExtension(id);
        }
    }
}
