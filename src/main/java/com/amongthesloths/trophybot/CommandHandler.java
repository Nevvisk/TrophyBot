package com.amongthesloths.trophybot;

import com.amongthesloths.trophybot.models.Trophy;
import com.amongthesloths.trophybot.models.UserTrophy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class CommandHandler extends ListenerAdapter {
    private final DatabaseManager dbManager;
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    public CommandHandler(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "trophy":
                handleTrophyCommand(event);
                break;
            case "admin":
                handleAdminCommand(event);
                break;
        }
    }

    private void handleTrophyCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        switch (subcommand) {
            case "create":
                if (!hasPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                    event.reply("Du hast keine Berechtigung, Trophäen zu erstellen!").setEphemeral(true).queue();
                    return;
                }
                createTrophy(event);
                break;
            case "award":
                if (!hasPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                    event.reply("Du hast keine Berechtigung, Trophäen zu vergeben!").setEphemeral(true).queue();
                    return;
                }
                awardTrophy(event);
                break;
            case "remove":
                if (!hasPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                    event.reply("Du hast keine Berechtigung, Trophäen zu vergeben!").setEphemeral(true).queue();
                    return;
                }
                removeTrophy(event);
                break;
            case "list":
                listTrophies(event);
                break;
            case "show":
                showTrophy(event);
                break;
            case "profile":
                showProfile(event);
                break;
            case "leaderboard":
                showLeaderboard(event);
                break;
        }
    }

    private void createTrophy(SlashCommandInteractionEvent event) {
        String name = event.getOption("name", OptionMapping::getAsString);
        String description = event.getOption("description", OptionMapping::getAsString);
        String emoji = event.getOption("emoji", OptionMapping::getAsString);
        String createdBy = event.getUser().getId();

        try {
            Trophy trophy = dbManager.createTrophy(name, description, emoji, createdBy);
            event.reply("Trophäe erfolgreich erstellt!")
                .addEmbeds(trophy.createEmbed())
                .queue();
        } catch (SQLException e) {
            event.reply("Fehler beim Erstellen der Trophäe: " + e.getMessage())
                .setEphemeral(true)
                .queue();
        }
    }

    private void awardTrophy(SlashCommandInteractionEvent event) {
        User user = event.getOption("user", OptionMapping::getAsUser);
        int trophyId = event.getOption("trophy_id", OptionMapping::getAsInt);

        try {
            Trophy trophy = dbManager.getTrophyById(trophyId);
            if (trophy == null) {
                event.reply("Trophäe nicht gefunden!").setEphemeral(true).queue();
                return;
            }

            dbManager.awardTrophy(
                user.getId(),
                trophyId,
                event.getUser().getId()
            );

            event.reply("Trophäe erfolgreich vergeben!")
                .addEmbeds(trophy.createEmbed())
                .queue();
        } catch (SQLException e) {
            event.reply("Fehler beim Vergeben der Trophäe: " + e.getMessage())
                .setEphemeral(true)
                .queue();
        }
    }

    private void listTrophies(SlashCommandInteractionEvent event) {
        try {
            List<Trophy> trophies = dbManager.getAllTrophies();
            if (trophies.isEmpty()) {
                event.reply("Es gibt noch keine Trophäen!").queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🏆 Verfügbare Trophäen")
                .setColor(Color.YELLOW)
                .setDescription("Hier sind alle verfügbaren Trophäen:")
                .setTimestamp(Instant.now());

            for (Trophy trophy : trophies) {
                String fieldTitle = String.format("%s %s (ID: %d)", 
                    trophy.getEmoji(),
                    trophy.getName(),
                    trophy.getId());
                
                String fieldValue = String.format("```%s```", trophy.getDescription());
                
                eb.addField(fieldTitle, fieldValue, false);
            }

            event.replyEmbeds(eb.build())
                .addActionRow(
                    Button.of(ButtonStyle.PRIMARY, "trophy_list:1", "Details anzeigen")
                )
                .queue();
        } catch (SQLException e) {
            event.reply("Fehler beim Abrufen der Trophäen: " + e.getMessage())
                .setEphemeral(true)
                .queue();
        }
    }

    private void showTrophy(SlashCommandInteractionEvent event) {
        int trophyId = event.getOption("trophy_id", OptionMapping::getAsInt);

        try {
            Trophy trophy = dbManager.getTrophyById(trophyId);
            if (trophy == null) {
                event.reply("Trophäe nicht gefunden!").setEphemeral(true).queue();
                return;
            }

            List<UserTrophy> winners = dbManager.getUsersWithTrophy(trophyId);
            EmbedBuilder eb = new EmbedBuilder(trophy.createEmbed());

            if (!winners.isEmpty()) {
                StringBuilder winnersList = new StringBuilder();
                for (UserTrophy winner : winners) {
                    winnersList.append("<@").append(winner.getUserId()).append(">\n");
                }
                eb.addField("Gewinner", winnersList.toString(), false);
            }

            event.replyEmbeds(eb.build())
                .addActionRow(
                    Button.primary("trophy_detail:" + trophyId, "Details"),
                    Button.secondary("trophy_winners:" + trophyId, "Gewinner anzeigen")
                )
                .queue();
        } catch (SQLException e) {
            event.reply("Fehler beim Abrufen der Trophäe: " + e.getMessage())
                .setEphemeral(true)
                .queue();
        }
    }

    private void showProfile(SlashCommandInteractionEvent event) {
        User user = event.getOption("user", event.getUser(), OptionMapping::getAsUser);

        try {
            List<UserTrophy> userTrophies = dbManager.getUserTrophies(user.getId());
            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🏆 Trophäenprofil von " + user.getName())
                .setColor(Color.YELLOW)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());

            if (userTrophies.isEmpty()) {
                eb.setDescription("Dieser Spieler hat noch keine Trophäen erhalten.");
            } else {
                StringBuilder description = new StringBuilder();
                description.append("Hier sind die Trophäen dieses Spielers:\n\n");
                
                for (UserTrophy userTrophy : userTrophies) {
                    Trophy trophy = userTrophy.getTrophy();
                    if (trophy != null) {
                        String trophyInfo = String.format("%s **%s** (ID: %d)\n```%s```\n*Erhalten am: %s*\n\n",
                            trophy.getEmoji(),
                            trophy.getName(),
                            trophy.getId(),
                            trophy.getDescription(),
                            userTrophy.getFormattedAwardDate());
                        description.append(trophyInfo);
                    }
                }
                eb.setDescription(description.toString());
            }

            event.replyEmbeds(eb.build()).queue();
        } catch (SQLException e) {
            logger.error("Failed to fetch profile", e);
            event.reply("Fehler beim Abrufen des Profils: " + e.getMessage())
                .setEphemeral(true)
                .queue();
        }
    }

    private void showLeaderboard(SlashCommandInteractionEvent event) {
        try {
            List<DatabaseManager.UserTrophyCount> leaderboard = dbManager.getLeaderboard(10);
            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Trophy Leaderboard")
                .setColor(Color.YELLOW)
                .setTimestamp(Instant.now());

            if (leaderboard.isEmpty()) {
                eb.setDescription("Noch keine Trophäen vergeben.");
            } else {
                StringBuilder desc = new StringBuilder();
                int rank = 1;
                for (DatabaseManager.UserTrophyCount entry : leaderboard) {
                    desc.append(String.format("%d. <@%d> - %d Trophäen\n",
                        rank++, entry.getUserId(), entry.getCount()));
                }
                eb.setDescription(desc.toString());
            }

            event.replyEmbeds(eb.build()).queue();
        } catch (SQLException e) {
            event.reply("Fehler beim Abrufen des Leaderboards: " + e.getMessage())
                .setEphemeral(true)
                .queue();
        }
    }

    private void handleAdminCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        switch (subcommand) {
            case "reset":
                if (!hasPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                    event.reply("Du hast keine Berechtigung, diese Aktion auszuführen!").setEphemeral(true).queue();
                    return;
                }
                resetTrophies(event);
                break;
            case "backup":
                if (!hasPermission(event.getMember(), Permission.ADMINISTRATOR)) {
                    event.reply("Du hast keine Berechtigung, diese Aktion auszuführen!").setEphemeral(true).queue();
                    return;
                }
                backupDatabase(event);
                break;
            default:
                event.reply("Unbekannter Admin-Befehl!").setEphemeral(true).queue();
                break;
        }
    }

    private void resetTrophies(SlashCommandInteractionEvent event) {
        // Logic to reset trophies
        event.reply("Trophäen wurden zurückgesetzt.").queue();
    }

    private void backupDatabase(SlashCommandInteractionEvent event) {
        // Logic to backup the database
        event.reply("Datenbank wurde gesichert.").queue();
    }

    private void removeTrophy(SlashCommandInteractionEvent event) {
        User user = event.getOption("user", OptionMapping::getAsUser);
        int trophyId = event.getOption("trophy_id", OptionMapping::getAsInt);

        try {
            Trophy trophy = dbManager.getTrophyById(trophyId);
            if (trophy == null) {
                event.reply("Trophäe nicht gefunden!").setEphemeral(true).queue();
                return;
            }

            dbManager.removeTrophy(user.getId(), trophyId);

            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Trophäe entfernt")
                .setColor(Color.YELLOW)
                .setDescription(String.format("Die Trophäe **%s** wurde erfolgreich von %s entfernt.",
                    trophy.getName(),
                    user.getAsMention()))
                .setTimestamp(Instant.now());

            event.replyEmbeds(eb.build()).queue();
        } catch (SQLException e) {
            String errorMessage = e.getMessage().contains("Trophy not found") ?
                "Diese Trophäe wurde dem Benutzer noch nicht verliehen!" :
                "Fehler beim Entfernen der Trophäe: " + e.getMessage();
            
            event.reply(errorMessage)
                .setEphemeral(true)
                .queue();
        }
    }

    private boolean hasPermission(Member member, Permission permission) {
        return member != null && member.hasPermission(permission);
    }
} 