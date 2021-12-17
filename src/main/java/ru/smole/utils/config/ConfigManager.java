package ru.smole.utils.config;

import lombok.Getter;

@Getter
public class ConfigManager {

    private Config config;
    private Config regionConfig;
    private Config miscConfig;

    public ConfigManager() {
        init();
    }

    private void init() {
        config = new Config("config.yml", "config.yml");
        regionConfig = new Config("regions.yml", "regions.yml");
        miscConfig = new Config("misc.yml", "misc.yml");
    }
}
