package com.amongthesloths.trophybot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {
    private final JSONObject config;
    
    public ConfigManager(String configPath) {
        // Konfigurationsdatei laden
        try {
            String content = new String(Files.readAllBytes(Paths.get(configPath)));
            this.config = new JSONObject(content);
        } catch (IOException e) {
            // Standardkonfiguration erstellen, wenn keine vorhanden ist
            this.config = createDefaultConfig();
            saveConfig(configPath);
        }
    }
    
    private JSONObject createDefaultConfig() {
        JSONObject defaultConfig = new JSONObject();
        defaultConfig.put("database", new JSONObject()
            .put("url", "jdbc:sqlite:trophies.db")
            .put("user", "")
            .put("password", ""));
        
        defaultConfig.put("permissions", new JSONObject()
            .put("admin_roles", new JSONArray())
            .put("trophy_manager_roles", new JSONArray()));
            
        return defaultConfig;
    }
    
    private void saveConfig(String configPath) {
        try {
            Files.write(Paths.get(configPath), config.toString(4).getBytes());
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Konfiguration: " + e.getMessage());
        }
    }
    
    public String getDatabaseUrl() {
        return config.getJSONObject("database").getString("url");
    }
    
    public String getDatabaseUser() {
        return config.getJSONObject("database").getString("user");
    }
    
    public String getDatabasePassword() {
        return config.getJSONObject("database").getString("password");
    }

    public JSONArray getAdminRoles() {
        return config.getJSONObject("permissions").getJSONArray("admin_roles");
    }

    public JSONArray getTrophyManagerRoles() {
        return config.getJSONObject("permissions").getJSONArray("trophy_manager_roles");
    }
} 