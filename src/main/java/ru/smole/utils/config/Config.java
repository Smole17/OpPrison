package ru.smole.utils.config;

import lombok.Getter;
import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.smole.OpPrison;

@Getter
public class Config {

    private OpPrison main;
    private String fileName;
    private String defaultConfig;
    private FileConfiguration configuration;

    public Config(String fileName, String defaultConfig) {
        main = OpPrison.getInstance();
        this.fileName = fileName;
        this.defaultConfig = defaultConfig;
        init();
    }

    private void init() {
        if (!main.getDataFolder().exists())
            main.getDataFolder().mkdirs();
        File file = getConfigurationFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
            }
        }
        reload();
    }

    private void reload() {
        configuration = YamlConfiguration.loadConfiguration(getConfigurationFile());
    }

    public void save() {
        try {
            configuration.save(getConfigurationFile());
        } catch (IOException ex) {
        }
    }

    private File getConfigurationFile() {
        return new File(main.getDataFolder(), fileName);
    }
}
