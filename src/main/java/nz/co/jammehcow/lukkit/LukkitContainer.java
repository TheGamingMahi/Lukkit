package nz.co.jammehcow.lukkit;

import nz.co.jammehcow.lukkit.environment.LuaEnvironment;
import nz.co.jammehcow.lukkit.environment.plugin.InternalLuaPlugin;
import nz.co.jammehcow.lukkit.environment.plugin.LukkitPluginFile;
import nz.co.jammehcow.lukkit.environment.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LukkitContainer extends JavaPlugin {
    private static LukkitContainer instance;
    private Map<String, InternalLuaPlugin> loadedPlugins = new HashMap<>();
    private static final int CFG_VERSION = 3;

    @Override
    public void onLoad() {
        instance = this;
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
        this.checkConfig();
        LuaEnvironment.init(getConfig().getBoolean("lua-debug"));
        getLogger().info("Lukkit container initializing...");
    }

    @Override
    public void onEnable() {
        loadInternalPlugins();
        this.getCommand("lukkit").setExecutor(new LukkitCommandExecutor(this));
        this.getCommand("lukkit").setTabCompleter(new TabCompleter());
        
        if (getConfig().getBoolean("update-checker", true)) {
            UpdateChecker.checkForUpdates(getDescription().getVersion());
        }
        getLogger().info("Lukkit container enabled - " + loadedPlugins.size() + " Lua plugins loaded");
    }

    @Override
    public void onDisable() {
        for (InternalLuaPlugin plugin : loadedPlugins.values()) {
            if (plugin.isEnabled()) plugin.disable();
        }
        getLogger().info("Lukkit container disabled");
    }

    private void loadInternalPlugins() {
        File pluginsDir = getDataFolder().getParentFile();
        File[] files = pluginsDir.listFiles();
        int loadedCount = 0;
        
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".lkt")) {
                    try {
                        LukkitPluginFile pluginFile = new LukkitPluginFile(file);
                        PluginDescriptionFile desc = new PluginDescriptionFile(pluginFile.getPluginYML());
                        getLogger().info("Loading Lua plugin: " + desc.getName());
                        
                        InternalLuaPlugin plugin = new InternalLuaPlugin(pluginFile);
                        loadedPlugins.put(plugin.getName(), plugin);
                        plugin.enable();
                        loadedCount++;
                    } catch (Exception e) {
                        getLogger().severe("Failed to load Lua plugin: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        getLogger().info("Successfully loaded " + loadedCount + " Lua plugins");
    }

    private void checkConfig() {
        File cfg = new File(this.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
        if (!cfg.exists()) this.saveDefaultConfig();

        if (this.getConfig().getInt("cfg-version", 0) != CFG_VERSION) {
            this.getLogger().info("Your config is out of date. Replacing with default...");
            File bkpCfg = new File(this.getDataFolder().getAbsolutePath() + File.separator + "config.old.yml");
            try {
                Files.copy(cfg.toPath(), bkpCfg.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(cfg.toPath());
                this.saveDefaultConfig();
            } catch (IOException e) {
                this.getLogger().severe("Config replacement failed");
                e.printStackTrace();
            }
        }
    }

    public static LukkitContainer getInstance() { return instance; }
    public Map<String, InternalLuaPlugin> getLoadedPlugins() { return loadedPlugins; }
}
