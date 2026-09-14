package com.rabusoore.chadplus;

import com.rabusoore.chadplus.broadcast.BroadcastManager;
import com.rabusoore.chadplus.command.BroadcastCommand;
import com.rabusoore.chadplus.command.ChatCommand;
import com.rabusoore.chadplus.config.FileManager;
import com.rabusoore.chadplus.listener.ChatListener;
import com.rabusoore.chadplus.listener.CommandListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private FileManager fileManager;
    private BroadcastManager broadcastManager;
    private boolean chatEnabled = true;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Load Configurations
        this.fileManager = new FileManager(this);
        this.fileManager.init();

        this.chatEnabled = this.fileManager.getConfig().getBoolean("chat.enabled", true);

        // 2. Initialize Broadcast Systems
        this.broadcastManager = new BroadcastManager(this);
        this.broadcastManager.loadBroadcasts();

        // 3. Register Event Listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        // 4. Register Commands & Tab Completers
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

        getLogger().info("ChadPLUS v" + getDescription().getVersion() + " by rabusoore has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (broadcastManager != null) {
            broadcastManager.cancelTask();
        }
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

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
        this.fileManager.getConfig().set("chat.enabled", chatEnabled);
    }
}
