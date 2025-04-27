/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * <p>
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * <p>
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.bndg.smack.extensions;


import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;

import javax.xml.namespace.QName;

/**
 * @author r
 * @date 2024/8/23
 * @description xmpp附件信息扩展 https://xmpp.org/extensions/xep-0066.html
 */
public class OobDataExtension implements ExtensionElement {

    public static final String ELEMENT_NAME = "x";
    // xmlns
    public static final String NAMESPACE = "jabber:x:oob";

    private final String url;

    public OobDataExtension(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public CharSequence toXML(XmlEnvironment xmlEnvironment) {
        return "<" + ELEMENT_NAME + " xmlns='" + NAMESPACE + "'>" +
                "<url>" + url + "</url>" +
                "</" + ELEMENT_NAME + ">";
    }

    public static class Provider extends ExtensionElementProvider<OobDataExtension> {

        @Override
        public OobDataExtension parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException, SmackParsingException {
            String url = null;
            parser.next();
            // Iterate through the XML until we reach the end of the extension element
            while (true) {
                XmlPullParser.Event eventType = parser.getEventType();
                if (eventType == XmlPullParser.Event.START_ELEMENT) {
                    if ("url".equals(parser.getName())) {
                        url = parser.nextText();
                    }
                    // Skip any nested elements if necessary
                } else if (eventType == XmlPullParser.Event.END_ELEMENT && parser.getDepth() <= initialDepth) {
                    break;
                }
                // Move to the next token
                parser.next();
                if (eventType == XmlPullParser.Event.END_DOCUMENT) {
                    break;  // 如果是文档结束，则退出循环
                }
            }
            return new OobDataExtension(url);
        }
    }
}
