package com.rabusoore.chadplus;

import com.rabusoore.chadplus.broadcast.BroadcastManager;
import com.rabusoore.chadplus.command.BroadcastCommand;
import com.rabusoore.chadplus.command.ChatCommand;
import com.rabusoore.chadplus.config.FileManager;
import com.rabusoore.chadplus.listener.ChatListener;
import com.rabusoore.chadplus.listener.CommandListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JavaPlugin {

    private static Main instance;
    private FileManager fileManager;
    private BroadcastManager broadcastManager;
    
    // Set untuk menyimpan UUID player yang mematikan chat pribadi
    private final Set<UUID> disabledChatPlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        instance = this;

        this.fileManager = new FileManager(this);
        this.fileManager.init();

        this.broadcastManager = new BroadcastManager(this);
        this.broadcastManager.loadBroadcasts();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        ChatCommand chatCmd = new ChatCommand(this);
        if (getCommand("chat") != null) {
            getCommand("chat").setExecutor(chatCmd);
            getCommand("chat").setTabCompleter(chatCmd);
        }

        BroadcastCommand bcCmd = new BroadcastCommand(this);
        if (getCommand("broadcast") != null) {
            getCommand("broadcast").setExecutor(bcCmd);
            getCommand("broadcast").setTabCompleter(bcCmd);
        }
        if (getCommand("autobroadcast") != null) {
            getCommand("autobroadcast").setExecutor(bcCmd);
            getCommand("autobroadcast").setTabCompleter(bcCmd);
        }

        getLogger().info("ChadPLUS v" + getDescription().getVersion() + " by rabusoore has been enabled.");
    }

    @Override
    public void onDisable() {
        if (broadcastManager != null) {
            broadcastManager.cancelTask();
        }
        disabledChatPlayers.clear();
        getLogger().info("ChadPLUS has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    public boolean isChatDisabledForPlayer(UUID uuid) {
        return disabledChatPlayers.contains(uuid);
    }

    public void setPlayerChat(UUID uuid, boolean enableChat) {
        if (enableChat) {
            disabledChatPlayers.remove(uuid);
        } else {
            disabledChatPlayers.add(uuid);
        }
    }

    public boolean togglePlayerChat(UUID uuid) {
        if (disabledChatPlayers.contains(uuid)) {
            disabledChatPlayers.remove(uuid);
            return true; // Chat diaktifkan
        } else {
            disabledChatPlayers.add(uuid);
            return false; // Chat dimatikan
        }
    }
}
