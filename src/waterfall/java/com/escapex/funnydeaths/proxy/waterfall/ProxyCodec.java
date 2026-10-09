package com.escapex.funnydeaths.proxy.waterfall;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Tiny wire format shared with the FunnyDeaths Bukkit plugin.
 * Duplicated in this artifact so each jar stays standalone.
 */
public final class ProxyCodec {

    public static final String CHANNEL = "funnydeaths";

    private static final byte PROTOCOL_VERSION = 1;
    private static final byte TYPE_PING = 1;
    private static final byte TYPE_PONG = 2;
    private static final byte TYPE_DEATH = 3;

    private ProxyCodec() {
    }

    public static byte[] pong() {
        try {
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(raw);
            out.writeByte(PROTOCOL_VERSION);
            out.writeByte(TYPE_PONG);
            out.writeUTF("funnydeaths");
            out.close();
            return raw.toByteArray();
        } catch (IOException impossible) {
            return new byte[] {PROTOCOL_VERSION, TYPE_PONG};
        }
    }

    public static boolean isPing(byte[] data) {
        return data != null && data.length >= 2 && data[0] == PROTOCOL_VERSION && data[1] == TYPE_PING;
    }

    /** Returns {victim, killer, rawMessage} or null when the packet is not a death message. */
    public static String[] readDeath(byte[] data) {
        if (data == null || data.length < 2) {
            return null;
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            byte protocol = in.readByte();
            byte type = in.readByte();
            if (protocol != PROTOCOL_VERSION || type != TYPE_DEATH) {
                return null;
            }
            String victim = in.readUTF();
            String killer = in.readUTF();
            String rawMessage = in.readUTF();
            if (rawMessage.isEmpty()) {
                return null;
            }
            return new String[] {victim, killer, rawMessage};
        } catch (IOException malformed) {
            return null;
        }
    }

    /** Rewrites {@code &} style codes (and {@code &<#hex>}) into section-sign codes for BungeeCord. */
    public static String toSectionCodes(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        char section = '\u00a7';
        char[] chars = message.toCharArray();
        StringBuilder out = new StringBuilder(chars.length + 8);
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if (current == '&' && i + 1 < chars.length) {
                char next = chars[i + 1];
                if (next == '#' && i + 8 <= chars.length && isHex(chars, i + 2, i + 8)) {
                    out.append(section).append('x');
                    for (int j = i + 2; j < i + 8; j++) {
                        out.append(section).append(chars[j]);
                    }
                    i += 7;
                    continue;
                }
                if (i + 1 < chars.length && "0123456789abcdefABCDEFk-oK-OrR".indexOf(next) >= 0) {
                    out.append(section).append(Character.toLowerCase(next));
                    i++;
                    continue;
                }
            }
            out.append(current);
        }
        return out.toString();
    }

    private static boolean isHex(char[] chars, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = chars[i];
            boolean digit = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!digit) {
                return false;
            }
        }
        return true;
    }
}
