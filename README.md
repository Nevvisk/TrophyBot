# Discord Trophy Bot

Ein Discord-Bot zum Verwalten und Vergeben von Trophäen an Server-Mitglieder.

## Features

- Erstellen von Trophäen mit Namen, Beschreibung und Icon
- Vergeben von Trophäen an Server-Mitglieder
- Anzeigen von Trophäenprofilen
- Leaderboard für die meisten Trophäen
- Detailansicht von Trophäen mit Gewinnerliste
- Paginierte Trophäenliste
- **Neue Admin-Befehle**: Zurücksetzen von Trophäen und Sichern der Datenbank

## Voraussetzungen

- Java 17 oder höher
- Maven
- Discord Bot Token
- SQLite Datenbank (wird automatisch erstellt)

## Installation

1. Klone das Repository:
```bash
git clone https://github.com/yourusername/trophy-bot.git
cd trophy-bot
```

2. Erstelle eine `config.json` Datei im Hauptverzeichnis:
```json
{
    "database": {
        "url": "jdbc:sqlite:trophies.db",
        "user": "",
        "password": ""
    },
    "permissions": {
        "admin_roles": [],
        "trophy_manager_roles": []
    }
}
```

3. Setze deinen Discord Bot Token als Umgebungsvariable:
```bash
export DISCORD_BOT_TOKEN=your_bot_token_here
```

4. Baue den Bot mit Maven:
```bash
mvn clean package
```

5. Starte den Bot:
```bash
java -jar target/trophy-bot-1.0-SNAPSHOT.jar
```

## Bot-Befehle

### Trophäen-Befehle

- `/trophy create` - Erstelle eine neue Trophäe (Admin)
- `/trophy award` - Vergebe eine Trophäe an einen Spieler (Admin)
- `/trophy list` - Zeige alle verfügbaren Trophäen
- `/trophy show` - Zeige Details einer Trophäe
- `/trophy profile` - Zeige Trophäen eines Spielers
- `/trophy leaderboard` - Zeige das Trophy-Leaderboard

### Admin-Befehle

- `/admin reset` - Setzt alle Trophäen zurück (Admin)
- `/admin backup` - Sichert die Datenbank (Admin)

## Berechtigungen

- Administratoren können Trophäen erstellen und vergeben
- Alle Benutzer können Trophäen anzeigen und Profile einsehen

## Datenbank

Die SQLite-Datenbank wird automatisch erstellt und enthält zwei Tabellen:

1. `trophies` - Speichert alle Trophäen
2. `user_trophies` - Speichert die Verbindung zwischen Benutzern und Trophäen

**Neue Methoden**:
- `getTrophyById(int trophyId)`: Gibt eine Trophäe anhand ihrer ID zurück.
- `getUsersWithTrophy(int trophyId)`: Gibt eine Liste von Benutzern zurück, die eine bestimmte Trophäe besitzen.
- `getLeaderboard(int limit)`: Gibt die Top-Benutzer basierend auf der Anzahl der Trophäen zurück.

## Lizenz

MIT License 