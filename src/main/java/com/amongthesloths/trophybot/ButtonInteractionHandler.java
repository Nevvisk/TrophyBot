package com.amongthesloths.trophybot;

import com.amongthesloths.trophybot.models.UserTrophy;
import java.awt.Color;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import com.amongthesloths.trophybot.models.Trophy;

public class ButtonInteractionHandler extends ListenerAdapter {
	private final DatabaseManager dbManager;

	public ButtonInteractionHandler(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		String[] data = event.getComponentId().split(":");

		if (data.length < 2)
			return;

		String action = data[0];

		switch (action) {
			case "trophy_detail":
				showTrophyDetail(event, Integer.parseInt(data[1]));
				break;
			case "trophy_winners":
				showTrophyWinners(event, Integer.parseInt(data[1]));
				break;
			case "trophy_list":
				handleTrophyListPagination(event, Integer.parseInt(data[1]));
				break;
		}
	}

	private void showTrophyDetail(ButtonInteractionEvent event, int trophyId) {
		try {
			Trophy trophy = dbManager.getTrophyById(trophyId);
			if (trophy == null) {
				event.reply("Trophäe nicht gefunden!").setEphemeral(true).queue();
				return;
			}

			event
					.replyEmbeds(trophy.createEmbed())
					.addActionRow(
							Button.primary("trophy_detail:" + trophyId, "Details"),
							Button.secondary("trophy_winners:" + trophyId, "Gewinner anzeigen"))
					.queue();
		} catch (SQLException e) {
			event.reply("Fehler beim Abrufen der Trophäe: " + e.getMessage()).setEphemeral(true).queue();
		}
	}

	private void showTrophyWinners(ButtonInteractionEvent event, int trophyId) {
		try {
			Trophy trophy = dbManager.getTrophyById(trophyId);
			if (trophy == null) {
				event.reply("Trophäe nicht gefunden!").setEphemeral(true).queue();
				return;
			}

			List<UserTrophy> winners = dbManager.getUsersWithTrophy(trophyId);
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Gewinner der Trophäe: " + trophy.getName())
					.setColor(Color.YELLOW)
					.setThumbnail(trophy.getEmoji())
					.setTimestamp(Instant.now());

			if (winners.isEmpty()) {
				eb.setDescription("Diese Trophäe wurde noch an niemanden vergeben.");
			} else {
				StringBuilder winnersList = new StringBuilder();
				for (UserTrophy winner : winners) {
					winnersList.append("<@").append(winner.getUserId()).append(">\n");
				}
				eb.setDescription("Folgende Spieler haben diese Trophäe erhalten:\n\n" + winnersList);
			}

			event
					.replyEmbeds(eb.build())
					.addActionRow(Button.primary("trophy_detail:" + trophyId, "Zurück zu Details"))
					.queue();
		} catch (SQLException e) {
			event.reply("Fehler beim Abrufen der Gewinner: " + e.getMessage()).setEphemeral(true).queue();
		}
	}

	private void handleTrophyListPagination(ButtonInteractionEvent event, int page) {
		try {
			List<Trophy> trophies = dbManager.getAllTrophies();
			if (trophies.isEmpty()) {
				event.reply("Es gibt noch keine Trophäen!").queue();
				return;
			}

			int itemsPerPage = 10;
			int totalPages = (int) Math.ceil((double) trophies.size() / itemsPerPage);
			int startIndex = (page - 1) * itemsPerPage;
			int endIndex = Math.min(startIndex + itemsPerPage, trophies.size());

			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Verfügbare Trophäen (Seite " + page + "/" + totalPages + ")")
					.setColor(Color.YELLOW)
					.setTimestamp(Instant.now());

			for (int i = startIndex; i < endIndex; i++) {
				Trophy trophy = trophies.get(i);
				eb.addField(trophy.getName(), trophy.getDescription(), false);
			}

			event
					.editMessageEmbeds(eb.build())
					.setActionRow(
							Button.primary("trophy_list:" + (page - 1), "◀️ Zurück").withDisabled(page <= 1),
							Button.primary("trophy_list:" + (page + 1), "Weiter ▶️")
									.withDisabled(page >= totalPages))
					.queue();
		} catch (SQLException e) {
			event.reply("Fehler beim Abrufen der Trophäen: " + e.getMessage()).setEphemeral(true).queue();
		}
	}
}
