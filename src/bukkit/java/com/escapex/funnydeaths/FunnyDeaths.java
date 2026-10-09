package com.escapex.funnydeaths;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

/**
 * FunnyDeaths - a universal funny death message plugin.
 *
 * <p>One jar for every server platform and version: Bukkit / CraftBukkit / Spigot /
 * Paper / Folia / Purpur / Leaves / Pufferfish and their forks, Minecraft 1.8.9
 * through 26.3 (and anything newer that keeps the Bukkit API).</p>
 */
public final class FunnyDeaths extends JavaPlugin implements Listener {

    private static final String PLUGIN_NAME = "FunnyDeaths";
    private static final String PLUGIN_VERSION = "2.0";

    private DeathMessages deathMessages;
    private ProxyBridge proxyBridge;
    private boolean pluginEnabled = true;
    private Method playerGetKiller;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginEnabled = getConfig().getBoolean("enabled", true);

        deathMessages = new DeathMessages(this);
        proxyBridge = new ProxyBridge(this);

        getServer().getPluginManager().registerEvents(this, (Plugin) this);
        proxyBridge.registerChannels();

        PluginCommand command = getCommand("fd");
        if (command != null) {
            FunnyDeathsCommand executor = new FunnyDeathsCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("The 'fd' command is not present in plugin.yml!");
        }

        proxyBridge.startHandshake();

        getLogger().info(PLUGIN_NAME + " v" + PLUGIN_VERSION + " by EscapeX has been enabled!");
        getLogger().info("Running on " + VersionUtil.describe());
        getLogger().info("Supported Minecraft versions: 1.8.9 - 26.3+");
        getLogger().info("Plugin status: " + (pluginEnabled ? "Enabled" : "Disabled"));
        getLogger().info("Loaded death messages for causes: " + deathMessages.getLoadedCauses());
    }

    @Override
    public void onDisable() {
        getLogger().info(PLUGIN_NAME + " v" + PLUGIN_VERSION + " has been disabled!");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!pluginEnabled) {
            return;
        }
        Player victim = event.getEntity();
        String killerName = findKillerName(victim);
        EntityDamageEvent lastDamage = victim.getLastDamageCause();

        // stop the vanilla death message, then print our own one tick later
        event.setDeathMessage(null);

        SchedulerCompat.runEntity(this, victim, new Runnable() {
            @Override
            public void run() {
                String rawMessage = deathMessages.getRandomDeathMessage(victim.getName(), killerName, lastDamage);
                boolean sentToProxy = proxyBridge.publish(victim, rawMessage);
                if (!sentToProxy) {
                    Bukkit.broadcastMessage(Text.color(rawMessage));
                }
                if (victim.isOnline()) {
                    Particles.resurrection(victim.getLocation().add(0.0, 1.0, 0.0));
                }
            }
        });
    }

    /** {@code Player#getKiller()} only exists from 1.9 onwards, so resolve it reflectively. */
    private String findKillerName(Player victim) {
        if (playerGetKiller == null) {
            try {
                playerGetKiller = victim.getClass().getMethod("getKiller");
            } catch (NoSuchMethodException olderServer) {
                playerGetKiller = null;
            }
        }
        if (playerGetKiller != null) {
            try {
                Player killer = (Player) playerGetKiller.invoke(victim);
                if (killer != null) {
                    String name = killer.getName();
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            } catch (Exception reflective) {
                // fall through to the damage event
            }
        }
        EntityDamageEvent damage = victim.getLastDamageCause();
        if (damage instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
            if (damager != null) {
                String name = damager.getName();
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        }
        return null;
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
        getConfig().set("enabled", Boolean.valueOf(enabled));
        saveConfig();
    }

    public void reloadPlugin() {
        reloadConfig();
        pluginEnabled = getConfig().getBoolean("enabled", true);
        deathMessages.loadFromConfig();
        getLogger().info("Configuration reloaded. Plugin status: " + (pluginEnabled ? "Enabled" : "Disabled"));
    }
}
