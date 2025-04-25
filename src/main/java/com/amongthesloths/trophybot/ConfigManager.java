package com.amongthesloths.trophybot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
	private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	private JSONObject config = null;

	public ConfigManager(URL configUrl) {
		try {
			// Try to load the config from the provided URL
			try (InputStream is = configUrl.openStream();
				 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				
				StringBuilder content = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					content.append(line);
				}
				
				String configContent = content.toString();
				if (!configContent.isBlank()) {
					this.config = new JSONObject(configContent);
					logger.info("Configuration loaded successfully");
				} else {
					logger.warn("Config file was empty, creating default configuration");
					this.config = createDefaultConfig();
				}
			}
		} catch (IOException e) {
			logger.warn("Could not load config file, creating default configuration", e);
			this.config = createDefaultConfig();
		}
		
		if (this.config == null) {
			throw new RuntimeException("Failed to initialize configuration");
		}
	}

	private JSONObject createDefaultConfig() {
		JSONObject defaultConfig = new JSONObject();
		defaultConfig.put(
				"database",
				new JSONObject()
					.put("url", "jdbc:sqlite:trophies.db")
					.put("user", "")
					.put("password", ""));

		defaultConfig.put(
				"permissions",
				new JSONObject()
					.put("admin_roles", new JSONArray())
					.put("trophy_manager_roles", new JSONArray()));

		return defaultConfig;
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
