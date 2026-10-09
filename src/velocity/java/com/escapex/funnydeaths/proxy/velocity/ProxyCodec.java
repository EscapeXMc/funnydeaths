package com.escapex.funnydeaths.proxy.velocity;

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
}
