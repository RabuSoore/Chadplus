package com.rabusoore.chadplus.config;

import com.rabusoore.chadplus.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private final Main plugin;
    private File configFile, broadcastsFile, badwordsFile, commandsFile;
    private FileConfiguration configYaml, broadcastsYaml, badwordsYaml, commandsYaml;

    public FileManager(Main plugin) {
        this.plugin = plugin;
    }

    public void init() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        broadcastsFile = new File(plugin.getDataFolder(), "broadcasts.yml");
        badwordsFile = new File(plugin.getDataFolder(), "badwords.yml");
        commandsFile = new File(plugin.getDataFolder(), "commands.yml");

        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        if (!broadcastsFile.exists()) plugin.saveResource("broadcasts.yml", false);
        if (!badwordsFile.exists()) plugin.saveResource("badwords.yml", false);
        if (!commandsFile.exists()) plugin.saveResource("commands.yml", false);

        reloadAll();
    }

    public void reloadAll() {
        configYaml = YamlConfiguration.loadConfiguration(configFile);
        broadcastsYaml = YamlConfiguration.loadConfiguration(broadcastsFile);
        badwordsYaml = YamlConfiguration.loadConfiguration(badwordsFile);
        commandsYaml = YamlConfiguration.loadConfiguration(commandsFile);
    }

    public FileConfiguration getConfig() { return configYaml; }
    public FileConfiguration getBroadcasts() { return broadcastsYaml; }
    public FileConfiguration getBadwords() { return badwordsYaml; }
    public FileConfiguration getCommands() { return commandsYaml; }
}
