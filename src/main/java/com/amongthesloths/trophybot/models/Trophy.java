package com.amongthesloths.trophybot.models;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import java.awt.Color;
import java.time.LocalDateTime;

public class Trophy {
    private int id;
    private String name;
    private String description;
    private String emoji;
    private LocalDateTime createdAt;
    private String createdBy;

    public Trophy(int id, String name, String description, String emoji, LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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
        return emoji;
    }

    public void setIconUrl(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public MessageEmbed createEmbed() {
        return new EmbedBuilder()
            .setTitle(emoji + " " + name)
            .setDescription(description)
            .setColor(Color.YELLOW)
            .setFooter("Trophy ID: " + id + " | Created by: " + createdBy)
            .setTimestamp(createdAt)
            .build();
    }
} 