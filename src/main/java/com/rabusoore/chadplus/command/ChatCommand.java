package com.rabusoore.chadplus.command;

import com.rabusoore.chadplus.Main;
import com.rabusoore.chadplus.util.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public ChatCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String prefix = plugin.getFileManager().getConfig().getString("prefix", "");

        if (label.equalsIgnoreCase("clearchat") && args.length == 0) {
            return handleClear(sender);
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtils.parse(prefix + "&cUsage: /chat <clear|on|off|toggle|reload>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "clear" -> {
                return handleClear(sender);
            }
            case "on" -> {
                if (!sender.hasPermission("chadplus.admin.chattoggle")) {
                    sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.setChatEnabled(true);
                String msg = plugin.getFileManager().getConfig().getString("messages.chat-toggled-on", "")
                        .replace("<player>", sender.getName());
                Bukkit.broadcast(ColorUtils.parse(prefix + msg));
            }
            case "off" -> {
                if (!sender.hasPermission("chadplus.admin.chattoggle")) {
                    sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.setChatEnabled(false);
                String msg = plugin.getFileManager().getConfig().getString("messages.chat-toggled-off", "")
                        .replace("<player>", sender.getName());
                Bukkit.broadcast(ColorUtils.parse(prefix + msg));
            }
            case "toggle" -> {
                if (!sender.hasPermission("chadplus.admin.chattoggle")) {
                    sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                boolean state = !plugin.isChatEnabled();
                plugin.setChatEnabled(state);
                String path = state ? "messages.chat-toggled-on" : "messages.chat-toggled-off";
                String msg = plugin.getFileManager().getConfig().getString(path, "").replace("<player>", sender.getName());
                Bukkit.broadcast(ColorUtils.parse(prefix + msg));
            }
            case "reload" -> {
                if (!sender.hasPermission("chadplus.admin.reload")) {
                    sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.getFileManager().reloadAll();
                plugin.getBroadcastManager().loadBroadcasts();
                sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.reload-success")));
            }
            default -> sender.sendMessage(ColorUtils.parse(prefix + "&cUnknown subcommand. Use: clear, on, off, toggle, reload."));
        }
        return true;
    }

    private boolean handleClear(CommandSender sender) {
        String prefix = plugin.getFileManager().getConfig().getString("prefix", "");
        if (!sender.hasPermission("chadplus.use.clearchat")) {
            sender.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.no-permission")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.parse(prefix + "&cOnly players can clear their personal chat view!"));
            return true;
        }

        int lines = plugin.getFileManager().getConfig().getInt("chat.clear-chat-lines", 100);
        for (int i = 0; i < lines; i++) {
            player.sendMessage(Component.empty());
        }
        player.sendMessage(ColorUtils.parse(prefix + plugin.getFileManager().getConfig().getString("messages.chat-cleared-personal")));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("chadplus.use.clearchat")) completions.add("clear");
            if (sender.hasPermission("chadplus.admin.chattoggle")) {
                completions.add("on");
                completions.add("off");
                completions.add("toggle");
            }
            if (sender.hasPermission("chadplus.admin.reload")) completions.add("reload");
        }
        return completions;
    }
}
