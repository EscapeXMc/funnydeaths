package com.escapex.funnydeaths.proxy.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Velocity companion of the FunnyDeaths Bukkit plugin: receives the funny death
 * message from a backend server and broadcasts it to the entire network.
 * Works with clients from 1.8.9 through 26.3 (and future versions).
 */
@Plugin(
        id = "funnydeaths",
        name = "FunnyDeaths",
        version = "2.0",
        description = "Network-wide funny death messages",
        authors = {"EscapeX"}
)
public final class FunnyDeathsVelocity {

    private final ProxyServer server;
    private final ChannelIdentifier identifier;
    private final Object console;

    @Inject
    public FunnyDeathsVelocity(ProxyServer server) {
        this.server = server;
        this.identifier = MinecraftChannelIdentifier.from(ProxyCodec.CHANNEL);
        this.console = server.getConsoleCommandSource();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(identifier);
        log("FunnyDeaths v2.0 by EscapeX enabled - listening for network-wide death messages.");
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!identifier.equals(event.getIdentifier())) {
            return;
        }
        // never leak our internal traffic to clients or backends
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        byte[] data = event.getData();

        if (ProxyCodec.isPing(data)) {
            answerPing(event);
            return;
        }

        // only a backend server may announce a death
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        String[] death = ProxyCodec.readDeath(data);
        if (death == null) {
            return;
        }

        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(death[2]);
        Collection<Player> players = server.getAllPlayers();
        for (Player player : players) {
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private void answerPing(PluginMessageEvent event) {
        try {
            Object target = event.getTarget();
            if (target instanceof ServerConnection) {
                ((ServerConnection) target).sendPluginMessage(identifier, ProxyCodec.pong());
            } else if (event.getSource() instanceof ServerConnection) {
                ((ServerConnection) event.getSource()).sendPluginMessage(identifier, ProxyCodec.pong());
            }
        } catch (Throwable failed) {
            log("Could not answer a FunnyDeaths ping: " + failed.getMessage());
        }
    }

    private void log(String message) {
        try {
            server.getConsoleCommandSource().sendPlainMessage("[FunnyDeaths] " + message);
        } catch (Throwable ignored) {
            // console unavailable, nothing we can do
        }
    }
}
