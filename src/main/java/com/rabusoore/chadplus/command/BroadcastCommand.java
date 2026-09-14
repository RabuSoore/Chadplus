package com.rabusoore.chadplus.command;

import com.rabusoore.chadplus.Main;
import com.rabusoore.chadplus.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BroadcastCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public BroadcastCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String prefix = plugin.getFileManager().getConfig().getString("prefix", "");

        if (label.equalsIgnoreCase("autobroadcast")) {
            if (!sender.hasPermission("chadplus.admin.autobroadcast")) {
                sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                return true;
            }
            boolean enabled = plugin.getBroadcastManager().toggleAutoBroadcast();
            String msgKey = enabled ? "messages.autobroadcast-enabled" : "messages.autobroadcast-disabled";
            sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString(msgKey)));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.parse(prefix + "&cUsage: /broadcast <send <message...> | play <id>>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("send")) {
            if (!sender.hasPermission("chadplus.admin.broadcast.send")) {
                sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                return true;
            }
            String messageStr = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            Bukkit.broadcast(ColorUtils.parse(messageStr));
            return true;
        } else if (sub.equals("play")) {
            if (!sender.hasPermission("chadplus.admin.broadcast.play")) {
                sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                return true;
            }
            String id = args[1];
            boolean success = plugin.getBroadcastManager().triggerBroadcast(id);
            if (success) {
                String msg = plugin.getFileManager().getConfig().getString("messages.broadcast-played", "").replace("<id>", id);
                sender.sendMessage(ColorUtils.parse(prefix + msg));
            } else {
                String msg = plugin.getFileManager().getConfig().getString("messages.broadcast-not-found", "").replace("<id>", id);
                sender.sendMessage(ColorUtils.parse(prefix + msg));
            }
            return true;
        }

        sender.sendMessage(ColorUtils.parse(prefix + "&cUsage: /broadcast <send <message...> | play <id>>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("autobroadcast")) {
            if (args.length == 1) completions.add("toggle");
            return completions;
        }

        if (args.length == 1) {
            if (sender.hasPermission("chadplus.admin.broadcast.send")) completions.add("send");
            if (sender.hasPermission("chadplus.admin.broadcast.play")) completions.add("play");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("play")) {
            completions.addAll(plugin.getBroadcastManager().getLoadedBroadcasts().keySet());
        }
        return completions;
    }
}
