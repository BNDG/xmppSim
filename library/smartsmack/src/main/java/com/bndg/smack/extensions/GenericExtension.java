package com.bndg.smack.extensions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;

public abstract class GenericExtension<T extends GenericExtension<T>> implements ExtensionElement {
    private final String namespace;
    private final String elementName;
    private final String[] attributes;

    protected GenericExtension(String namespace, String elementName, String... attributes) {
        this.namespace = namespace;
        this.elementName = elementName;
        this.attributes = attributes;
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

        // 添加每个属性
        for (String attr : attributes) {
            xml.halfOpenElement(attr);
            xml.attribute(attr, getAttributeValue(attr));
            xml.closeEmptyElement();
        }

        xml.closeElement(this);
        return xml;
    }

    protected abstract String getAttributeValue(String attributeName);

    public static abstract class Provider<T extends GenericExtension<T>> extends ExtensionElementProvider<T> {
        private final String[] attributes;
        public Provider(String... attributes) {
            this.attributes = attributes;
        }
        @Override
        public T parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException, SmackParsingException {
            String[] values = new String[attributes.length];
            while (true) {
                XmlPullParser.Event eventType = parser.getEventType();
                if (eventType == XmlPullParser.Event.START_ELEMENT) {
                    for (int j = 0; j < attributes.length; j++) {
                        if (attributes[j].equals(parser.getName())) {
                            values[j] = parser.getAttributeValue("", parser.getName()); // 您可以根据需要修改属性名称
                        }
                    }
                } else if (eventType == XmlPullParser.Event.END_ELEMENT && parser.getDepth() <= initialDepth) {
                    break;
                }
                parser.next();
            }
            return createExtension(values);
        }

        protected abstract T createExtension(String[] values);
    }
}