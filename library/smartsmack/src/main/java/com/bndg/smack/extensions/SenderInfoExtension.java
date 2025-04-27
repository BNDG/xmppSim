package com.bndg.smack.extensions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;

/**
 * @author r
 * @date 2024/9/12
 * @description 群发送消息携带发送者的userid和昵称
 */

public class SenderInfoExtension implements ExtensionElement {
    public static final String NAMESPACE = "urn:xmpp:sim-sender-info:2";
    public static final String ELEMENT_NAME = "sender-info";

    private String senderBareJid;

    private String senderNickname;

    public SenderInfoExtension(String senderBareJid, String senderNickname) {
        this.senderBareJid = senderBareJid;
        this.senderNickname = senderNickname;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public String getSenderBareJid() {
        return senderBareJid;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    @Override
    public CharSequence toXML(XmlEnvironment xmlEnvironment) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();

        xml.halfOpenElement("item");
        xml.attribute("senderBareJid", senderBareJid);
        xml.attribute("senderNickname", senderNickname);
        xml.closeEmptyElement();

        xml.closeElement(this);
        return xml;
    }

    public static class Provider extends ExtensionElementProvider<SenderInfoExtension> {
        @Override
        public SenderInfoExtension parse(org.jivesoftware.smack.xml.XmlPullParser parser, int i, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException, SmackParsingException {
            String senderBareJid = null;
            String senderNickname = null;
            // Ensure we start at the correct position
            // Move to the next token
            parser.next();
            // Iterate through the XML until we reach the end of the extension element
            while (true) {
                XmlPullParser.Event eventType = parser.getEventType();
                if (eventType == XmlPullParser.Event.START_ELEMENT) {
                    if ("item".equals(parser.getName())) {
                        senderBareJid = parser.getAttributeValue("", "senderBareJid");
                        senderNickname = parser.getAttributeValue("", "senderNickname");
                    }
                    // Skip any nested elements if necessary
                } else if (eventType == XmlPullParser.Event.END_ELEMENT && parser.getDepth() <= i) {
                    break;
                }
                // Move to the next token
                parser.next();
                if (eventType == XmlPullParser.Event.END_DOCUMENT) {
                    break;  // 如果是文档结束，则退出循环
                }
            }
            return new SenderInfoExtension(senderBareJid, senderNickname);
        }
    }
}