package com.escapex.funnydeaths;

/**
 * Legacy chat-code translation that works on every version and needs no Bukkit classes
 * of its own, so it behaves identically on 1.8.9 and 26.3.
 *
 * <p>Supports {@code &} codes, literal {@code §} codes and {@code &#RRGGBB} /
 * {@code &x§R§R§G§G§B§B} hex colours on servers that support them (1.16+).</p>
 */
public final class Text {

    private static final char SECTION = '\u00a7';
    private static final String HEX_DIGITS = "0123456789abcdefABCDEF";
    private static final String COLOR_CODES = "0123456789abcdefklmnor";

    private Text() {
    }

    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        boolean hexAllowed = VersionUtil.supportsHexColors();
        char[] chars = message.toCharArray();
        StringBuilder out = new StringBuilder(chars.length + 8);
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if ((current == '&' || current == SECTION) && i + 1 < chars.length) {
                char next = chars[i + 1];
                // &#RRGGBB style hex colour
                if (i + 8 <= chars.length && (next == '#' || next == 'x') && isHex(chars, i + 2, i + 8)) {
                    if (hexAllowed) {
                        out.append(SECTION).append('x');
                        for (int j = i + 2; j < i + 8; j++) {
                            out.append(SECTION).append(chars[j]);
                        }
                    }
                    // servers below 1.16 cannot render hex: drop the marker cleanly instead of printing it
                    i += 7;
                    continue;
                }
                // plain legacy colour code
                if (COLOR_CODES.indexOf(Character.toLowerCase(next)) >= 0) {
                    out.append(SECTION).append(Character.toLowerCase(next));
                    i++;
                    continue;
                }
            }
            out.append(current);
        }
        return out.toString();
    }

    /** Strips every colour code so a message can be logged or used as a plain string. */
    public static String strip(String message) {
        if (message == null) {
            return "";
        }
        return color(message).replace("§", "").replace("&", "&").replaceAll("[&§](?:[0-9a-fk-orA-FK-OR]|#[0-9a-fA-F]{6}|#[0-9a-fA-F]{6})", "");
    }

    private static boolean isHex(char[] chars, int start, int end) {
        for (int i = start; i < end; i++) {
            if (HEX_DIGITS.indexOf(chars[i]) < 0) {
                return false;
            }
        }
        return true;
    }
}
