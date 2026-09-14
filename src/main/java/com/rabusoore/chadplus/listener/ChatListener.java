package com.rabusoore.chadplus.listener;

import com.rabusoore.chadplus.Main;
import com.rabusoore.chadplus.util.ColorUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> chatCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastMessages = new ConcurrentHashMap<>();

    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.getFileManager().getConfig().getString("prefix", "");

        // Filter: Hapus player yang mematikan chat pribadi dari daftar penerima (viewers)
        event.viewers().removeIf(audience -> {
            if (audience instanceof Player viewer) {
                return plugin.isChatDisabledForPlayer(viewer.getUniqueId());
            }
            return false;
        });

        // 1. Cooldown Check
        if (!player.hasPermission("chadplus.bypass.cooldown.chat")) {
            long cooldownSec = plugin.getFileManager().getConfig().getLong("cooldowns.chat-seconds", 3);
            long now = System.currentTimeMillis();
            long lastTime = chatCooldowns.getOrDefault(player.getUniqueId(), 0L);

            if ((now - lastTime) < (cooldownSec * 1000L)) {
                long remaining = Math.max(1, (cooldownSec * 1000L - (now - lastTime)) / 1000L);
                event.setCancelled(true);
                String cdMsg = plugin.getFileManager().getConfig().getString("cooldowns.cooldown-chat-message", "")
                        .replace("<seconds>", String.valueOf(remaining));
                player.sendMessage(ColorUtils.parse(prefix + cdMsg));
                return;
            }
        }

        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        // 2. Duplicate Anti-Spam Check
        if (plugin.getFileManager().getConfig().getBoolean("anti-spam.block-duplicates", true) &&
            !player.hasPermission("chadplus.bypass.cooldown.chat")) {
            String lastMsg = lastMessages.get(player.getUniqueId());
            if (lastMsg != null && lastMsg.equalsIgnoreCase(rawMessage)) {
                event.setCancelled(true);
                String dupMsg = plugin.getFileManager().getConfig().getString("anti-spam.duplicate-message", "");
                player.sendMessage(ColorUtils.parse(prefix + dupMsg));
                return;
            }
        }

        // 3. Bad Word Filtering Engine
        if (!player.hasPermission("chadplus.bypass.filter")) {
            FilterResult result = processFilter(rawMessage);
            if (result.blocked()) {
                event.setCancelled(true);
                String blockMsg = plugin.getFileManager().getBadwords().getString("filter.block-message", "");
                player.sendMessage(ColorUtils.parse(prefix + blockMsg));
                return;
            } else if (result.modified()) {
                event.message(Component.text(result.filteredText()));
            }
        }

        // Update tracks
        chatCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        lastMessages.put(player.getUniqueId(), rawMessage);
    }

    private FilterResult processFilter(String rawText) {
        FileConfiguration badConfig = plugin.getFileManager().getBadwords();
        String mode = badConfig.getString("filter.mode", "CENSOR").toUpperCase();
        String replacementChar = badConfig.getString("filter.replacement-char", "*");
        boolean normLeet = badConfig.getBoolean("filter.normalize-leetspeak", true);
        boolean normUnicode = badConfig.getBoolean("filter.normalize-unicode", true);
        boolean reduceRepeats = badConfig.getBoolean("filter.reduce-repeats", true);

        List<String> badWords = badConfig.getStringList("prohibited-words");
        List<String> whitelist = badConfig.getStringList("whitelisted-words");

        String[] tokens = rawText.split("\\s+");
        StringBuilder finalMessageBuilder = new StringBuilder();
        boolean hasCensored = false;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String cleanToken = token.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

            boolean isWhitelisted = false;
            for (String w : whitelist) {
                if (cleanToken.equalsIgnoreCase(w.toLowerCase())) {
                    isWhitelisted = true;
                    break;
                }
            }

            if (isWhitelisted) {
                finalMessageBuilder.append(token);
                if (i < tokens.length - 1) finalMessageBuilder.append(" ");
                continue;
            }

            String normalized = cleanToken;
            if (normUnicode) {
                normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            }
            if (normLeet) {
                normalized = normalizeLeetspeak(normalized);
            }
            if (reduceRepeats) {
                normalized = normalized.replaceAll("(.)\\1{2,}", "$1");
            }

            boolean containsBadWord = false;
            for (String bad : badWords) {
                if (normalized.contains(bad.toLowerCase())) {
                    containsBadWord = true;
                    break;
                }
            }

            if (containsBadWord) {
                if (mode.equals("BLOCK")) {
                    return new FilterResult(true, false, rawText);
                } else {
                    hasCensored = true;
                    finalMessageBuilder.append(replacementChar.repeat(token.length()));
                }
            } else {
                finalMessageBuilder.append(token);
            }

            if (i < tokens.length - 1) finalMessageBuilder.append(" ");
        }

        return new FilterResult(false, hasCensored, finalMessageBuilder.toString());
    }

    private String normalizeLeetspeak(String input) {
        return input.replace("@", "a")
                .replace("4", "a")
                .replace("3", "e")
                .replace("1", "i")
                .replace("!", "i")
                .replace("0", "o")
                .replace("$", "s")
                .replace("5", "s")
                .replace("7", "t")
                .replace("8", "b");
    }

    private record FilterResult(boolean blocked, boolean modified, String filteredText) {}
}
