package com.escapex.funnydeaths;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Forwards death messages to a companion FunnyDeaths proxy plugin (Velocity or
 * Waterfall) running in front of this server, so the funny message can be shown
 * to the whole network instead of a single backend server.
 *
 * <p>Protocol (small, dependency free):</p>
 * <pre>
 *   byte  protocol (= 1)
 *   byte  type      (1 = ping backend -&gt; proxy, 2 = pong proxy -&gt; backend, 3 = death backend -&gt; proxy)
 *   utf   victim        (only for death)
 *   utf   killer        (only for death, "" when unknown)
 *   utf   rawMessage    (only for death, still with &amp; colour codes)
 * </pre>
 */
public final class ProxyBridge implements PluginMessageListener {

    public static final String CHANNEL = "funnydeaths";

    private static final byte PROTOCOL_VERSION = 1;
    private static final byte TYPE_PING = 1;
    private static final byte TYPE_PONG = 2;
    private static final byte TYPE_DEATH = 3;

    private static final int HANDSHAKE_INTERVAL_TICKS = 100;

    private final FunnyDeaths plugin;
    private volatile boolean proxyDetected = false;

    public ProxyBridge(FunnyDeaths plugin) {
        this.plugin = plugin;
    }

    public void registerChannels() {
        try {
            Messenger messenger = plugin.getServer().getMessenger();
            messenger.registerIncomingPluginChannel(plugin, CHANNEL, this);
            messenger.registerOutgoingPluginChannel(plugin, CHANNEL);
        } catch (Throwable ignored) {
            // very old servers may behave differently, they simply never forward
        }
    }

    /** Pings the proxy through any online player until it answers. */
    public void startHandshake() {
        try {
            plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {
                    if (proxyDetected) {
                        return;
                    }
                    Player carrier = firstOnlinePlayer();
                    if (carrier != null) {
                        carrier.sendPluginMessage(plugin, CHANNEL, packet(TYPE_PING, new String[0]));
                    }
                }
            }, 20L, HANDSHAKE_INTERVAL_TICKS);
        } catch (Throwable foliaStyleServer) {
            // Folia rejects the legacy scheduler: retry the handshake as soon as a player joins
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel) || message == null || message.length < 2) {
            return;
        }
        if (message[0] != PROTOCOL_VERSION || message[1] != TYPE_PONG) {
            return;
        }
        if (!proxyDetected) {
            proxyDetected = true;
            plugin.getLogger().info("FunnyDeaths proxy plugin detected - death messages are now broadcast network-wide.");
        }
    }

    /**
     * Publishes a death message to the companion proxy plugin.
     *
     * @return true when the message was handed over to a proxy (and will therefore be
     *         broadcast network-wide by the proxy), false when this server must broadcast it itself.
     */
    public boolean publish(Player sender, String rawMessage) {
        if (!proxyDetected) {
            return false;
        }
        Player carrier = sender != null && sender.isOnline() ? sender : firstOnlinePlayer();
        if (carrier == null) {
            return false;
        }
        try {
            String victimName = sender != null ? sender.getName() : "";
            carrier.sendPluginMessage(plugin, CHANNEL,
                    packet(TYPE_DEATH, new String[] {victimName, "", rawMessage}));
            return true;
        } catch (Throwable notSent) {
            return false;
        }
    }

    private Player firstOnlinePlayer() {
        try {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online != null && online.isOnline()) {
                    return online;
                }
            }
        } catch (Throwable ignored) {
            // Folia: getOnlinePlayers() can throw from the global thread
        }
        return null;
    }

    private static byte[] packet(byte type, String[] payload) {
        try {
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(raw);
            out.writeByte(PROTOCOL_VERSION);
            out.writeByte(type);
            for (int i = 0; i < payload.length; i++) {
                out.writeUTF(payload[i] == null ? "" : payload[i]);
            }
            out.close();
            return raw.toByteArray();
        } catch (IOException impossible) {
            return new byte[] {PROTOCOL_VERSION, type};
        }
    }

    /** Reads the payload strings of a death/ping message, or null when malformed. */
    public static String[] readDeath(byte[] message) {
        if (message == null || message.length < 2) {
            return null;
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            byte protocol = in.readByte();
            byte type = in.readByte();
            if (protocol != PROTOCOL_VERSION || type != TYPE_DEATH) {
                return null;
            }
            String victim = in.readUTF();
            String killer = in.readUTF();
            String rawMessage = in.readUTF();
            return new String[] {victim, killer, rawMessage};
        } catch (IOException malformed) {
            return null;
        }
    }
}
