package com.hjq.demo.utils;

import java.util.regex.Pattern;

public class Patterns {
    /**
     * Kept for backward compatibility reasons.
     *
     * @deprecated Deprecated since it does not include all IRI characters defined in RFC 3987
     */
    @Deprecated
    public static final String GOOD_IRI_CHAR =
            "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    public static final Pattern XMPP_PATTERN = Pattern
            .compile("xmpp\\:(?:(?:["
                    + Patterns.GOOD_IRI_CHAR
                    + "\\;\\/\\?\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])"
                    + "|(?:\\%[a-fA-F0-9]{2}))+");


}