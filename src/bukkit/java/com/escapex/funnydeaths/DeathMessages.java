package com.escapex.funnydeaths;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Loads the funny death messages from the config and picks a fitting one.
 * Messages are stored raw (with {@code &} codes) and only translated to section
 * codes right before they are sent, which keeps behaviour identical on every platform.
 */
public class DeathMessages {

    private final Map<String, List<String>> mobDeathMessages = new HashMap<String, List<String>>();
    private final Map<EntityDamageEvent.DamageCause, List<String>> deathMessages =
            new HashMap<EntityDamageEvent.DamageCause, List<String>>();
    private final List<String> genericDeathMessages = new ArrayList<String>();
    private final FunnyDeaths plugin;

    public DeathMessages(FunnyDeaths plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    public void loadFromConfig() {
        mobDeathMessages.clear();
        deathMessages.clear();
        genericDeathMessages.clear();

        FileConfiguration config = plugin.getConfig();

        List<String> genericList = config.getStringList("generic-messages");
        if (genericList.isEmpty()) {
            addFallbackGenericMessages();
        } else {
            for (String message : genericList) {
                if (message != null && !message.trim().isEmpty()) {
                    genericDeathMessages.add(message);
                }
            }
        }

        ConfigurationSection damageSection = config.getConfigurationSection("damage-causes");
        if (damageSection != null) {
            for (String causeKey : damageSection.getKeys(false)) {
                String resolved = Names.cause(causeKey);
                List<String> messages = damageSection.getStringList(causeKey);
                if (resolved == null) {
                    plugin.getLogger().warning("Damage cause '" + causeKey + "' does not exist on this server version, skipping.");
                    continue;
                }
                if (messages.isEmpty()) {
                    continue;
                }
                deathMessages.put(EntityDamageEvent.DamageCause.valueOf(resolved), new ArrayList<String>(messages));
            }
        }

        ConfigurationSection mobSection = config.getConfigurationSection("mob-messages");
        if (mobSection != null) {
            for (String mobKey : mobSection.getKeys(false)) {
                List<String> messages = mobSection.getStringList(mobKey);
                if (messages.isEmpty()) {
                    continue;
                }
                mobDeathMessages.put(Names.entity(mobKey), new ArrayList<String>(messages));
            }
        }

        plugin.getLogger().info("Loaded " + genericDeathMessages.size() + " generic messages, "
                + deathMessages.size() + " damage cause types, " + mobDeathMessages.size() + " mob types");
    }

    private void addFallbackGenericMessages() {
        genericDeathMessages.add("&c%player% &7forgot how to breathe!");
        genericDeathMessages.add("&c%player% &7uninstalled life.exe!");
        genericDeathMessages.add("&c%player% &7chose the permanent sleep option!");
        genericDeathMessages.add("&c%player% &7went to find their lost dignity!");
        genericDeathMessages.add("&c%player% &7ko mil gaya uska karma!");
        genericDeathMessages.add("&c%player% &7discovered the respawn button... the hard way!");
        genericDeathMessages.add("&c%player% &7failed the living challenge!");
        genericDeathMessages.add("&c%player% &7became a ghost... unsuccessfully!");
    }

    public String getRandomDeathMessage(String playerName, String killerName, EntityDamageEvent lastDamageCause) {
        List<String> messages = pick(lastDamageCause);
        String message = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        if (killerName != null && !killerName.isEmpty()) {
            return message.replace("%player%", playerName).replace("%killer%", killerName);
        }
        return message.replace("%player%", playerName);
    }

    private List<String> pick(EntityDamageEvent lastDamageCause) {
        if (lastDamageCause != null && lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
            if (damager != null) {
                List<String> mobMessages = mobDeathMessages.get(Names.entity(damager.getType().name()));
                if (mobMessages != null && !mobMessages.isEmpty()) {
                    return mobMessages;
                }
            }
        }
        if (lastDamageCause != null) {
            List<String> byCause = deathMessages.get(lastDamageCause.getCause());
            if (byCause != null && !byCause.isEmpty()) {
                return byCause;
            }
        }
        return genericDeathMessages.isEmpty() ? fallbackIfNeeded() : genericDeathMessages;
    }

    private List<String> fallbackIfNeeded() {
        addFallbackGenericMessages();
        return genericDeathMessages;
    }

    public String getLoadedCauses() {
        StringBuilder causes = new StringBuilder();
        for (Map.Entry<EntityDamageEvent.DamageCause, List<String>> entry : deathMessages.entrySet()) {
            causes.append(entry.getKey()).append('(').append(entry.getValue().size()).append(" messages), ");
        }
        for (Map.Entry<String, List<String>> entry : mobDeathMessages.entrySet()) {
            causes.append(entry.getKey()).append('(').append(entry.getValue().size()).append(" messages), ");
        }
        causes.append("Generic(").append(genericDeathMessages.size()).append(" messages)");
        return causes.toString();
    }
}
