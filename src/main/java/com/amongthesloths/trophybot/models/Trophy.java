package com.amongthesloths.trophybot.models;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import java.awt.Color;
import java.time.LocalDateTime;

public class Trophy {
    private int id;
    private String name;
    private String description;
    private String iconUrl;
    private LocalDateTime createdAt;

    public Trophy(int id, String name, String description, String iconUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public MessageEmbed createEmbed() {
        return new EmbedBuilder()
            .setTitle(name)
            .setDescription(description)
            .setThumbnail(iconUrl)
            .setColor(Color.YELLOW)
            .setFooter("Trophy ID: " + id)
            .build();
    }
} 