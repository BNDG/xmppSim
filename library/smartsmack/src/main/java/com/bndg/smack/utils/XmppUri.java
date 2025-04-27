package com.bndg.smack.utils;

import android.net.Uri;

import androidx.annotation.NonNull;


import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class XmppUri {

    public static final String ACTION_JOIN = "join";
    public static final String ACTION_MESSAGE = "message";
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_ROSTER = "roster";
    public static final String PARAMETER_PRE_AUTH = "preauth";
    public static final String PARAMETER_IBR = "ibr";
    public static final String XMPP_PRE = "xmpp:";
    private static final String OMEMO_URI_PARAM = "omemo-sid-";
    protected Uri uri;
    private List<Fingerprint> fingerprints = new ArrayList<>();
    private Map<String, String> parameters = Collections.emptyMap();
    private boolean safeSource = true;
    private String jid;

    public static final String INVITE_DOMAIN = "conversations.im";

    public XmppUri(final String uri) {
        try {
            parse(Uri.parse(uri));
        } catch (IllegalArgumentException e) {
            try {
                jid = JidCreate.from(uri).asBareJid().asUrlEncodedString();
            } catch (XmppStringprepException e2) {
                jid = null;
            }
        }
    }

    public XmppUri(Uri uri) {
        parse(uri);
    }

    public XmppUri(Uri uri, boolean safeSource) {
        this.safeSource = safeSource;
        parse(uri);
    }

    private static Map<String, String> parseParameters(final String query, final char seperator) {
        final HashMap<String, String> builder = new HashMap<>();
        final String[] pairs = query == null ? new String[0] : query.split(String.valueOf(seperator));
        for (String pair : pairs) {
            final String[] parts = pair.split("=", 2);
            if (parts.length == 0) {
                continue;
            }
            final String key = parts[0].toLowerCase(Locale.US);
            final String value;
            if (parts.length == 2) {
                String decoded;
                try {
                    decoded = URLDecoder.decode(parts[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    decoded = "";
                }
                value = decoded;
            } else {
                value = "";
            }
            builder.put(key, value);
        }
        return builder;
    }

    private static List<Fingerprint> parseFingerprints(Map<String, String> parameters) {
        ArrayList<Fingerprint> builder = new ArrayList<>();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            final String key = parameter.getKey();
            final String value = parameter.getValue().toLowerCase(Locale.US);
            if (key.startsWith(OMEMO_URI_PARAM)) {
                try {
                    final int id = Integer.parseInt(key.substring(OMEMO_URI_PARAM.length()));
                    builder.add(new Fingerprint(FingerprintType.OMEMO, value, id));
                } catch (Exception e) {
                    //ignoring invalid device id
                }
            } else if ("omemo".equals(key)) {
                builder.add(new Fingerprint(FingerprintType.OMEMO, value, 0));
            }
        }
        return builder;
    }

    public static String getFingerprintUri(final String base, final List<Fingerprint> fingerprints, char separator) {
        final StringBuilder builder = new StringBuilder(base);
        builder.append('?');
        for (int i = 0; i < fingerprints.size(); ++i) {
            FingerprintType type = fingerprints.get(i).type;
            if (type == FingerprintType.OMEMO) {
                builder.append(XmppUri.OMEMO_URI_PARAM);
                builder.append(fingerprints.get(i).deviceId);
            }
            builder.append('=');
            builder.append(fingerprints.get(i).fingerprint);
            if (i != fingerprints.size() - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    private static String lameUrlDecode(String url) {
        return url.replace("%23", "#").replace("%25", "%");
    }

    public static String lameUrlEncode(String url) {
        return url.replace("%", "%25").replace("#", "%23");
    }

    public static String getRoomLinkly(String groupId) {
        return XMPP_PRE + groupId + "?join";
    }

    public boolean isSafeSource() {
        return safeSource;
    }

    protected void parse(final Uri uri) {
        if (uri == null) {
            return;
        }
        this.uri = uri;
        final String scheme = uri.getScheme();
        final String host = uri.getHost();
        List<String> segments = uri.getPathSegments();
        if ("https".equalsIgnoreCase(scheme) && INVITE_DOMAIN.equalsIgnoreCase(host)) {
            if (segments.size() >= 2 && segments.get(1).contains("@")) {
                // sample : https://conversations.im/i/foo@bar.com
                try {
                    jid = JidCreate.from(lameUrlDecode(segments.get(1))).asUrlEncodedString();
                } catch (Exception e) {
                    jid = null;
                }
            } else if (segments.size() >= 3) {
                // sample : https://conversations.im/i/foo/bar.com
                jid = segments.get(1) + "@" + segments.get(2);
            }
            if (segments.size() > 1 && "j".equalsIgnoreCase(segments.get(0))) {

                Map<String, String> hashMap = new HashMap<>();
                hashMap.put(ACTION_JOIN, "");
                this.parameters = hashMap;
            }
            final Map<String, String> parameters = parseParameters(uri.getQuery(), '&');
            this.fingerprints = parseFingerprints(parameters);
        } else if ("xmpp".equalsIgnoreCase(scheme)) {
            // sample: xmpp:foo@bar.com
            this.parameters = parseParameters(uri.getQuery(), ';');
            if (uri.getAuthority() != null) {
                jid = uri.getAuthority();
            } else {
                final String[] parts = uri.getSchemeSpecificPart().split("\\?");
                if (parts.length > 0) {
                    jid = parts[0];
                } else {
                    return;
                }
            }
            this.fingerprints = parseFingerprints(parameters);
        } else if ("imto".equalsIgnoreCase(scheme) && Arrays.asList("xmpp", "jabber").contains(uri.getHost())) {
            // sample: imto://xmpp/foo@bar.com
            try {
                jid = URLDecoder.decode(uri.getEncodedPath(), "UTF-8").split("/")[1].trim();
            } catch (final UnsupportedEncodingException ignored) {
                jid = null;
            }
        } else {
            jid = null;
        }
    }

    @Override
    @NonNull
    public String toString() {
        if (uri != null) {
            return uri.toString();
        }
        return "";
    }

    public Jid getJid() {
        try {
            return this.jid == null ? null : JidCreate.from(this.jid);
        } catch (XmppStringprepException e) {
            return null;
        }
    }

    public boolean isValidJid() {
        if (jid == null) {
            return false;
        }
        try {
            return JidCreate.from(jid) != null;
        } catch (XmppStringprepException e) {
            return false;
        }
    }

    public String getBody() {
        return parameters.get("body");
    }

    public String getName() {
        return parameters.get("name");
    }

    public String getParameter(String key) {
        return this.parameters.get(key);
    }

    public List<Fingerprint> getFingerprints() {
        return this.fingerprints;
    }

    public boolean hasFingerprints() {
        return fingerprints.size() > 0;
    }

    public enum FingerprintType {
        OMEMO
    }

    public static class Fingerprint {
        public final FingerprintType type;
        public final String fingerprint;
        final int deviceId;

        public Fingerprint(FingerprintType type, String fingerprint, int deviceId) {
            this.type = type;
            this.fingerprint = fingerprint;
            this.deviceId = deviceId;
        }

        @NonNull
        @Override
        public String toString() {
            return type.toString() + ": " + fingerprint + (deviceId != 0 ? " " + deviceId : "");
        }
    }
}
