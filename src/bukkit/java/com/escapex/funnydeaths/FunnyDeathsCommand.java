package com.escapex.funnydeaths;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /fd toggle [on|off], /fd reload and tab completion. */
public class FunnyDeathsCommand implements CommandExecutor, TabCompleter {

    private final FunnyDeaths plugin;

    public FunnyDeathsCommand(FunnyDeaths plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("funnydeath.admin")) {
            sender.sendMessage(Text.color("&cYou don't have permission to use this command!"));
            return true;
        }
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        if ("toggle".equalsIgnoreCase(args[0])) {
            handleToggleCommand(sender, args);
        } else if ("reload".equalsIgnoreCase(args[0])) {
            handleReloadCommand(sender);
        } else {
            sendHelpMessage(sender);
        }
        return true;
    }

    private void handleToggleCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            boolean newState = !plugin.isPluginEnabled();
            plugin.setPluginEnabled(newState);
            sender.sendMessage(Text.color("&aFunnyDeaths " + (newState ? "enabled" : "disabled") + "!"));
        } else if (args.length == 2) {
            String state = args[1].toLowerCase();
            if (state.equals("on") || state.equals("true")) {
                plugin.setPluginEnabled(true);
                sender.sendMessage(Text.color("&aFunnyDeaths enabled!"));
            } else if (state.equals("off") || state.equals("false")) {
                plugin.setPluginEnabled(false);
                sender.sendMessage(Text.color("&cFunnyDeaths disabled!"));
            } else {
                sender.sendMessage(Text.color("&cUsage: /fd toggle [on|off]"));
            }
        } else {
            sender.sendMessage(Text.color("&cUsage: /fd toggle [on|off]"));
        }
    }

    private void handleReloadCommand(CommandSender sender) {
        try {
            plugin.reloadPlugin();
            sender.sendMessage(Text.color("&aFunnyDeaths configuration reloaded successfully!"));
        } catch (Exception exception) {
            sender.sendMessage(Text.color("&cFailed to reload configuration: " + exception.getMessage()));
            plugin.getLogger().severe("Failed to reload configuration: " + exception.getMessage());
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Text.color("&6=== FunnyDeaths Commands ==="));
        sender.sendMessage(Text.color("&e/fd toggle [on|off] &f- Toggle plugin on/off"));
        sender.sendMessage(Text.color("&e/fd reload &f- Reload plugin configuration"));
        sender.sendMessage(Text.color("&7Status: " + (plugin.isPluginEnabled() ? "&aEnabled" : "&cDisabled")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("funnydeath.admin")) {
            return new ArrayList<String>();
        }
        if (args.length == 1) {
            return Arrays.asList("toggle", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return Arrays.asList("on", "off");
        }
        return new ArrayList<String>();
    }
}
