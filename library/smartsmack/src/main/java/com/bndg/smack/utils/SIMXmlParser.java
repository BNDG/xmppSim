package com.bndg.smack.utils;

import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserFactory;
import org.jivesoftware.smack.xml.xpp3.Xpp3XmlPullParserFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.base.IExtensionProvider;

public class SIMXmlParser {
    private static volatile SIMXmlParser instance;
    XmlPullParserFactory factory;

    private SIMXmlParser() {
        factory = new Xpp3XmlPullParserFactory();
    }

    public static SIMXmlParser getInstance() {
        if (instance == null) {
            synchronized (SIMXmlParser.class) {
                if (instance == null) {
                    instance = new SIMXmlParser();
                }
            }
        }
        return instance;
    }

    public <T extends IExtension> IExtension parseXml(CharSequence xmlRepresentation, IExtensionProvider<T> provider) {
        try {
            XmlPullParser parser = factory.newXmlPullParser(new StringReader(xmlRepresentation.toString()));
            Map<String, String> extraData = new HashMap<>();
            parser.next();
            while (true) {
                XmlPullParser.Event eventType = parser.getEventType();
                if (eventType == XmlPullParser.Event.START_ELEMENT) {
                    if ("item".equals(parser.getName())) {
                        List<String> property = provider.getProperty();
                        if (property != null) {
                            for (String key : property) {
                                String value = parser.getAttributeValue("", key);
                                extraData.put(key, value);
                            }
                        }
                    }
                }
                parser.next();
                if (eventType == XmlPullParser.Event.END_DOCUMENT) {
                    break;  // 如果是文档结束，则退出循环
                }
            }
            return provider.createExtension(extraData);
        } catch (Exception e) {
            return null;
        }
    }
}
