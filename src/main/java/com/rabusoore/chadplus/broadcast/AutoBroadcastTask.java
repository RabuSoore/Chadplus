package com.rabusoore.chadplus.broadcast;

import com.rabusoore.chadplus.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoBroadcastTask extends BukkitRunnable {

    private final Main plugin;
    private final BroadcastManager manager;
    private final Random random = new Random();
    private int currentIndex = 0;

    public AutoBroadcastTask(Main plugin, BroadcastManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void run() {
        List<BroadcastManager.BroadcastEntry> entries = new ArrayList<>(manager.getLoadedBroadcasts().values());
        if (entries.isEmpty()) return;

        String mode = plugin.getFileManager().getBroadcasts().getString("settings.mode", "SEQUENTIAL").toUpperCase();
        BroadcastManager.BroadcastEntry selected;

        if (mode.equals("RANDOM")) {
            selected = entries.get(random.nextInt(entries.size()));
        } else {
            if (currentIndex >= entries.size()) {
                currentIndex = 0;
            }
            selected = entries.get(currentIndex);
            currentIndex++;
        }

        manager.sendBroadcast(selected);
    }
}
