package com.febreze.autosprintplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("auto-sprint-plus.json");
    private static ModConfig config = new ModConfig();

    private ConfigManager() {}
    public static ModConfig getConfig() { return config; }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_FILE)) {
                save();
                return;
            }
            String json = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);
            config = loaded != null ? loaded : new ModConfig();
            config.ensureValid();
        } catch (Exception e) {
            System.err.println("[Auto-SprintPlus] Could not load config; using defaults: " + e.getMessage());
            config = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[Auto-SprintPlus] Could not save config: " + e.getMessage());
        }
    }

    public static void reset() {
        config = new ModConfig();
        save();
    }
}
