package com.amongthesloths.trophybot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class TrophyBot {
    private static final Logger logger = LoggerFactory.getLogger(TrophyBot.class);
    private final JDA jda;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public TrophyBot(String token) {
        logger.info("Initializing Trophy Bot...");
        
        // Bot-Konfiguration laden
        this.configManager = new ConfigManager("config.json");
        logger.info("Configuration loaded successfully");
        
        try {
            // Datenbankverbindung aufbauen
            this.databaseManager = new DatabaseManager(
                configManager.getDatabaseUrl(),
                configManager.getDatabaseUser(),
                configManager.getDatabasePassword()
            );
            logger.info("Database connection established successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
        
        try {
            // JDA-Instance initialisieren
            this.jda = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("Trophäen vergeben"))
                    .addEventListeners(
                        new CommandHandler(databaseManager),
                        new ButtonInteractionHandler(databaseManager)
                    )
                    .build();
            logger.info("JDA instance initialized successfully");
                    
            // Slash-Commands registrieren
            registerCommands();
            logger.info("Slash commands registered successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize JDA", e);
            throw new RuntimeException("Failed to initialize JDA", e);
        }
    }

    private void registerCommands() {
        logger.debug("Registering slash commands...");
        jda.updateCommands().addCommands(
            Commands.slash("trophy", "Trophäen-bezogene Befehle")
                .addSubcommands(
                    new SubcommandData("create", "Erstelle eine neue Trophäe"),
                    new SubcommandData("award", "Vergebe eine Trophäe an einen Spieler"),
                    new SubcommandData("list", "Zeige alle verfügbaren Trophäen"),
                    new SubcommandData("show", "Zeige Details einer Trophäe"),
                    new SubcommandData("profile", "Zeige Trophäen eines Spielers"),
                    new SubcommandData("leaderboard", "Zeige das Trophy-Leaderboard")
                ),
            Commands.slash("admin", "Administrative Befehle (nur für Admins)")
        ).queue(
            success -> logger.info("Successfully registered slash commands"),
            failure -> logger.error("Failed to register slash commands", failure)
        );
    }

    public static void main(String[] args) {
        logger.info("Starting Trophy Bot...");
        String token = System.getenv("DISCORD_BOT_TOKEN");
        if (token == null) {
            logger.error("DISCORD_BOT_TOKEN environment variable not set");
            System.err.println("Bitte setze die Umgebungsvariable DISCORD_BOT_TOKEN");
            return;
        }
        
        try {
            new TrophyBot(token);
            logger.info("Trophy Bot started successfully!");
        } catch (Exception e) {
            logger.error("Failed to start Trophy Bot", e);
            System.exit(1);
        }
    }
} 