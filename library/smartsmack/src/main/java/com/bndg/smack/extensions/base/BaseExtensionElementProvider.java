package com.bndg.smack.extensions.base;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author r
 * @date 2024/11/12
 * @description 扩展解析抽象类
 */
public abstract class BaseExtensionElementProvider<EE extends ExtensionElement> extends ExtensionElementProvider<EE> {

    protected abstract List<String> getProperty();

    protected abstract EE createInstance(Map<String, String> extraData);

    @Override
    public EE parse(XmlPullParser parser, int i, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException, SmackParsingException {
        // Ensure we start at the correct position
        // Move to the next token
        Map<String, String> extraData = new HashMap<>();
        parser.next();
        // Iterate through the XML until we reach the end of the extension element
        while (true) {
            XmlPullParser.Event eventType = parser.getEventType();
            if (eventType == XmlPullParser.Event.START_ELEMENT) {
                if ("item".equals(parser.getName())) {
                    List<String> property = getProperty();
                    if (property != null) {
                        for (String key : getProperty()) {
                            String value = parser.getAttributeValue("", key);
                            extraData.put(key, value);
                        }
                    }
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
        return createInstance(extraData);
    }
}
