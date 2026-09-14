package com.rabusoore.chadplus.broadcast;

import com.rabusoore.chadplus.Main;
import com.rabusoore.chadplus.util.ColorUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;

public class BroadcastManager {

    private final Main plugin;
    private final Map<String, BroadcastEntry> loadedBroadcasts = new LinkedHashMap<>();
    private BukkitTask broadcastTask;
    private boolean autoBroadcastEnabled = true;

    public BroadcastManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadBroadcasts() {
        cancelTask();
        loadedBroadcasts.clear();

        FileConfiguration config = plugin.getFileManager().getBroadcasts();
        autoBroadcastEnabled = config.getBoolean("settings.enabled", true);

        ConfigurationSection section = config.getConfigurationSection("broadcasts");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection entrySec = section.getConfigurationSection(id);
                if (entrySec == null) continue;

                String display = entrySec.getString("display", "CHAT").toUpperCase();
                List<String> messages = entrySec.getStringList("message");
                String title = entrySec.getString("title", "");
                String subtitle = entrySec.getString("subtitle", "");

                boolean soundEnabled = entrySec.getBoolean("sound.enabled", false);
                String soundName = entrySec.getString("sound.name", "entity.player.levelup");
                float volume = (float) entrySec.getDouble("sound.volume", 1.0);
                float pitch = (float) entrySec.getDouble("sound.pitch", 1.0);

                BroadcastEntry entry = new BroadcastEntry(id, display, messages, title, subtitle, soundEnabled, soundName, volume, pitch);
                loadedBroadcasts.put(id, entry);
            }
        }

        if (autoBroadcastEnabled && !loadedBroadcasts.isEmpty()) {
            startTask();
        }
    }

    public void startTask() {
        cancelTask();
        long intervalSeconds = plugin.getFileManager().getBroadcasts().getLong("settings.global-interval", 60);
        AutoBroadcastTask task = new AutoBroadcastTask(plugin, this);
        this.broadcastTask = task.runTaskTimer(plugin, 20L * intervalSeconds, 20L * intervalSeconds);
    }

    public void cancelTask() {
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    public boolean toggleAutoBroadcast() {
        autoBroadcastEnabled = !autoBroadcastEnabled;
        if (autoBroadcastEnabled) {
            startTask();
        } else {
            cancelTask();
        }
        return autoBroadcastEnabled;
    }

    public boolean triggerBroadcast(String id) {
        BroadcastEntry entry = loadedBroadcasts.get(id);
        if (entry == null) return false;
        sendBroadcast(entry);
        return true;
    }

    public void sendBroadcast(BroadcastEntry entry) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playBroadcastToPlayer(entry, player);
        }
        // Log to console if CHAT mode
        if (entry.display().equals("CHAT")) {
            for (String msg : entry.messages()) {
                Bukkit.getConsoleSender().sendMessage(ColorUtils.parse(msg));
            }
        }
    }

    private void playBroadcastToPlayer(BroadcastEntry entry, Player player) {
        if (entry.soundEnabled() && entry.soundName() != null && !entry.soundName().isEmpty()) {
            try {
                Key key = Key.key(entry.soundName().toLowerCase().replace("_", "."));
                Sound sound = Sound.sound(key, Sound.Source.MASTER, entry.soundVolume(), entry.soundPitch());
                player.playSound(sound);
            } catch (Exception ignored) {}
        }

        switch (entry.display()) {
            case "ACTION_BAR" -> {
                if (!entry.messages().isEmpty()) {
                    player.sendActionBar(ColorUtils.parse(entry.messages().getFirst()));
                }
            }
            case "TITLE" -> {
                Component titleComp = ColorUtils.parse(entry.title());
                Component subComp = ColorUtils.parse(entry.subtitle());
                Title title = Title.title(
                        titleComp,
                        subComp,
                        Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
                );
                player.showTitle(title);
            }
            case "CHAT" -> {
                for (String line : entry.messages()) {
                    player.sendMessage(ColorUtils.parse(line));
                }
            }
        }
    }

    public Map<String, BroadcastEntry> getLoadedBroadcasts() {
        return loadedBroadcasts;
    }

    public record BroadcastEntry(
            String id,
            String display,
            List<String> messages,
            String title,
            String subtitle,
            boolean soundEnabled,
            String soundName,
            float soundVolume,
            float soundPitch
    ) {}
}
