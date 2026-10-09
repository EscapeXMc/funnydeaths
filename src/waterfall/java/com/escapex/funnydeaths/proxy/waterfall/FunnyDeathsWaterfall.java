package com.escapex.funnydeaths.proxy.waterfall;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Waterfall / BungeeCord companion of the FunnyDeaths Bukkit plugin: receives the funny
 * death message from a backend server and broadcasts it to the whole network.
 * Works with clients from 1.8.9 through 26.3 (and future versions).
 */
public final class FunnyDeathsWaterfall extends Plugin implements Listener {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel(ProxyCodec.CHANNEL);
        getLogger().info("FunnyDeaths v2.0 by EscapeX enabled - listening for network-wide death messages.");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!ProxyCodec.CHANNEL.equals(event.getTag())) {
            return;
        }
        // our traffic must never reach the player's client
        event.setCancelled(true);

        byte[] data = event.getData();

        if (ProxyCodec.isPing(data)) {
            answerPing(event);
            return;
        }

        String[] death = ProxyCodec.readDeath(data);
        if (death == null) {
            return;
        }

        BaseComponent[] message = TextComponent.fromLegacyText(ProxyCodec.toSectionCodes(death[2]));
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private void answerPing(PluginMessageEvent event) {
        try {
            if (event.getReceiver() instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                if (player.getServer() != null) {
                    player.getServer().sendData(ProxyCodec.CHANNEL, ProxyCodec.pong());
                }
            }
        } catch (Throwable failed) {
            getLogger().warning("Could not answer a FunnyDeaths ping: " + failed.getMessage());
        }
    }
}
