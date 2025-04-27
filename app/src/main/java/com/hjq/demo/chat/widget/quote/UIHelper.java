package com.hjq.demo.chat.widget.quote;

/**
 * @author r
 * @date 2024/11/1
 * @description Brief description of the file content.
 */
public class UIHelper {
    public static boolean isPositionPrecededByLineStart(CharSequence body, int pos){
        if (isPositionPrecededByBodyStart(body, pos)){
            return true;
        }
        return body.charAt(pos - 1) == '\n';
    }
    public static boolean isPositionPrecededByBodyStart(CharSequence body, int pos){
        // true if not a single linebreak before current position
        for (int i = pos - 1; i >= 0; i--){
            if (body.charAt(i) != ' '){
                return false;
            }
        }
        return true;
    }


    public static boolean isPositionFollowedByQuoteableCharacter(CharSequence body, int pos) {
        return !isPositionFollowedByNumber(body, pos)
                && !isPositionFollowedByEmoticon(body, pos)
                && !isPositionFollowedByEquals(body, pos);
    }
    private static boolean isPositionFollowedByNumber(CharSequence body, int pos) {
        boolean previousWasNumber = false;
        for (int i = pos + 1; i < body.length(); i++) {
            char c = body.charAt(i);
            if (Character.isDigit(body.charAt(i))) {
                previousWasNumber = true;
            } else if (previousWasNumber && (c == '.' || c == ',')) {
                previousWasNumber = false;
            } else {
                return (Character.isWhitespace(c) || c == '%' || c == '+') && previousWasNumber;
            }
        }
        return previousWasNumber;
    }

    private static boolean isPositionFollowedByEquals(CharSequence body, int pos) {
        return body.length() > pos + 1 && body.charAt(pos + 1) == '=';
    }

    private static boolean isPositionFollowedByEmoticon(CharSequence body, int pos) {
        if (body.length() <= pos + 1) {
            return false;
        } else {
            final char first = body.charAt(pos + 1);
            return first == ';'
                    || first == ':'
                    || first == '.' // do not quote >.< (but >>.<)
                    || closingBeforeWhitespace(body, pos + 1);
        }
    }

    private static boolean closingBeforeWhitespace(CharSequence body, int pos) {
        for (int i = pos; i < body.length(); ++i) {
            final char c = body.charAt(i);
            if (Character.isWhitespace(c)) {
                return false;
            } else if (QuoteHelper.isPositionQuoteCharacter(body, pos) || QuoteHelper.isPositionQuoteEndCharacter(body, pos)) {
                return body.length() == i + 1 || Character.isWhitespace(body.charAt(i + 1));
            }
        }
        return false;
    }


}
