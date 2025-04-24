package com.amongthesloths.trophybot;

import com.amongthesloths.trophybot.models.Trophy;
import com.amongthesloths.trophybot.models.UserTrophy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final Connection connection;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        logger.info("Initializing database connection to: {}", url);
        try {
            this.connection = DriverManager.getConnection(url, user, password);
            initializeDatabase();
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw e;
        }
    }

    private void initializeDatabase() throws SQLException {
        logger.debug("Creating database tables if they don't exist");
        try (Statement statement = connection.createStatement()) {
            // Create trophies table
            statement.execute("CREATE TABLE IF NOT EXISTS trophies (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "icon_url TEXT" +
                    ")");
            
            // Create user_trophies table
            statement.execute("CREATE TABLE IF NOT EXISTS user_trophies (" +
                    "user_id TEXT NOT NULL, " +
                    "trophy_id INTEGER NOT NULL, " +
                    "award_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (user_id, trophy_id), " +
                    "FOREIGN KEY (trophy_id) REFERENCES trophies(id)" +
                    ")");
            logger.debug("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Failed to create database tables", e);
            throw e;
        }
    }

    public Trophy createTrophy(String name, String description, String iconUrl) throws SQLException {
        logger.info("Creating new trophy: {}", name);
        String sql = "INSERT INTO trophies (name, description, icon_url, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, iconUrl);
            Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
            statement.setTimestamp(4, createdAt);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    Trophy trophy = new Trophy(id, name, description, iconUrl, createdAt.toLocalDateTime());
                    logger.info("Trophy created successfully: {}", trophy);
                    return trophy;
                } else {
                    throw new SQLException("Creating trophy failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create trophy: {}", name, e);
            throw e;
        }
    }

    public void awardTrophy(String userId, int trophyId) throws SQLException {
        logger.info("Awarding trophy {} to user {}", trophyId, userId);
        String sql = "INSERT INTO user_trophies (user_id, trophy_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setInt(2, trophyId);
            statement.executeUpdate();
            logger.info("Trophy awarded successfully");
        } catch (SQLException e) {
            logger.error("Failed to award trophy {} to user {}", trophyId, userId, e);
            throw e;
        }
    }

    public List<Trophy> getAllTrophies() throws SQLException {
        logger.debug("Fetching all trophies");
        List<Trophy> trophies = new ArrayList<>();
        String sql = "SELECT * FROM trophies ORDER BY id";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                trophies.add(new Trophy(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("icon_url"),
                    resultSet.getTimestamp("create_date").toLocalDateTime()
                ));
            }
            logger.debug("Retrieved {} trophies", trophies.size());
            return trophies;
        } catch (SQLException e) {
            logger.error("Failed to fetch trophies", e);
            throw e;
        }
    }

    public List<UserTrophy> getUserTrophies(String userId) throws SQLException {
        logger.debug("Fetching trophies for user {}", userId);
        List<UserTrophy> userTrophies = new ArrayList<>();
        String sql = "SELECT t.*, ut.award_date FROM trophies t " +
                    "JOIN user_trophies ut ON t.id = ut.trophy_id " +
                    "WHERE ut.user_id = ? " +
                    "ORDER BY ut.award_date DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Trophy trophy = new Trophy(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("icon_url"),
                        resultSet.getTimestamp("create_date").toLocalDateTime()
                    );
                    UserTrophy userTrophy = new UserTrophy(trophy, resultSet.getTimestamp("award_date"));
                    userTrophies.add(userTrophy);
                }
            }
            logger.debug("Retrieved {} trophies for user {}", userTrophies.size(), userId);
            return userTrophies;
        } catch (SQLException e) {
            logger.error("Failed to fetch trophies for user {}", userId, e);
            throw e;
        }
    }

    public Trophy getTrophyById(int trophyId) throws SQLException {
        logger.debug("Fetching trophy with ID: {}", trophyId);
        String sql = "SELECT * FROM trophies WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trophyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Trophy(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("icon_url"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                } else {
                    logger.warn("No trophy found with ID: {}", trophyId);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch trophy with ID: {}", trophyId, e);
            throw e;
        }
    }

    public List<UserTrophy> getUsersWithTrophy(int trophyId) throws SQLException {
        logger.debug("Fetching users with trophy ID: {}", trophyId);
        List<UserTrophy> userTrophies = new ArrayList<>();
        String sql = "SELECT ut.user_id, ut.award_date, t.* FROM user_trophies ut " +
                     "JOIN trophies t ON ut.trophy_id = t.id WHERE ut.trophy_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trophyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Trophy trophy = new Trophy(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("icon_url"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                    UserTrophy userTrophy = new UserTrophy(trophy, resultSet.getTimestamp("award_date"));
                    userTrophies.add(userTrophy);
                }
            }
            logger.debug("Retrieved {} users with trophy ID: {}", userTrophies.size(), trophyId);
            return userTrophies;
        } catch (SQLException e) {
            logger.error("Failed to fetch users with trophy ID: {}", trophyId, e);
            throw e;
        }
    }

    public void close() {
        logger.info("Closing database connection");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
        }
    }

    public static class UserTrophyCount {
        private final long userId;
        private final int count;

        public UserTrophyCount(long userId, int count) {
            this.userId = userId;
            this.count = count;
        }

        public long getUserId() {
            return userId;
        }

        public int getCount() {
            return count;
        }
    }

    public List<UserTrophyCount> getLeaderboard(int limit) throws SQLException {
        logger.debug("Fetching leaderboard with limit: {}", limit);
        List<UserTrophyCount> leaderboard = new ArrayList<>();
        String sql = "SELECT user_id, COUNT(*) as count FROM user_trophies GROUP BY user_id ORDER BY count DESC LIMIT ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    leaderboard.add(new UserTrophyCount(
                        resultSet.getLong("user_id"),
                        resultSet.getInt("count")
                    ));
                }
            }
            logger.debug("Retrieved leaderboard with {} entries", leaderboard.size());
            return leaderboard;
        } catch (SQLException e) {
            logger.error("Failed to fetch leaderboard", e);
            throw e;
        }
    }
} 