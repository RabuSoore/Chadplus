package com.rabusoore.chadplus.listener;

import com.rabusoore.chadplus.Main;
import com.rabusoore.chadplus.util.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> commandCooldowns = new ConcurrentHashMap<>();

    public CommandListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("chadplus.admin.unhidecommands")) {
            return;
        }

        List<String> blockedCmds = plugin.getFileManager().getCommands().getStringList("blocked-commands");
        for (String blocked : blockedCmds) {
            String clean = blocked.startsWith("/") ? blocked.substring(1).toLowerCase() : blocked.toLowerCase();
            event.getCommands().remove(clean);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String fullMessage = event.getMessage().toLowerCase();

        // 1. Command Execution Hiding Check
        if (!player.hasPermission("chadplus.admin.unhidecommands")) {
            List<String> blockedCmds = plugin.getFileManager().getCommands().getStringList("blocked-commands");
            for (String blocked : blockedCmds) {
                String cmd = blocked.toLowerCase();
                if (fullMessage.equalsIgnoreCase(cmd) || fullMessage.startsWith(cmd + " ")) {
                    event.setCancelled(true);
                    player.sendMessage(ColorUtils.parse("&cUnknown command. Type \"/help\" for help."));
                    return;
                }
            }
        }

        // 2. Command Cooldown Check
        if (!player.hasPermission("chadplus.bypass.cooldown.command")) {
            long cooldownSec = plugin.getFileManager().getConfig().getLong("cooldowns.command-seconds", 2);
            long now = System.currentTimeMillis();
            long lastTime = commandCooldowns.getOrDefault(player.getUniqueId(), 0L);

            if ((now - lastTime) < (cooldownSec * 1000L)) {
                long remaining = Math.max(1, (cooldownSec * 1000L - (now - lastTime)) / 1000L);
                event.setCancelled(true);
                String prefix = plugin.getFileManager().getConfig().getString("prefix", "");
                String cdMsg = plugin.getFileManager().getConfig().getString("cooldowns.cooldown-command-message", "")
                        .replace("<seconds>", String.valueOf(remaining));
                player.sendMessage(ColorUtils.parse(prefix + cdMsg));
                return;
            }
            commandCooldowns.put(player.getUniqueId(), now);
        }
    }
}
