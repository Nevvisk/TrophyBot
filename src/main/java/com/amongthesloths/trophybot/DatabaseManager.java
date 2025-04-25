package com.amongthesloths.trophybot;

import com.amongthesloths.trophybot.models.Trophy;
import com.amongthesloths.trophybot.models.UserTrophy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final Connection connection;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        logger.info("Initializing database connection to: {}", url);
        this.connection = DriverManager.getConnection(url, user, password);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Read schema.sql from resources
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
                if (is == null) {
                    throw new RuntimeException("Could not find schema.sql in resources");
                }

                String schema = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));

                // Split the schema into individual statements
                String[] statements = schema.split(";");

                // Execute each statement
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute(statement);
                        }
                    }
                }
                logger.info("Database schema initialized successfully");
            }
        } catch (IOException | SQLException e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    public Trophy createTrophy(String name, String description, String emoji, String createdBy) throws SQLException {
        logger.info("Creating new trophy: {}", name);
        String sql = "INSERT INTO trophies (name, description, emoji, created_at, created_by) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, emoji);
            Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
            statement.setTimestamp(4, createdAt);
            statement.setString(5, createdBy);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    Trophy trophy = new Trophy(id, name, description, emoji, createdAt.toLocalDateTime(), createdBy);
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

    public void awardTrophy(String userId, int trophyId, String awardedBy) throws SQLException {
        logger.info("Awarding trophy {} to user {} by {}", trophyId, userId, awardedBy);
        String sql = "INSERT INTO trophy_awards (user_id, trophy_id, awarded_by, awarded_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setInt(2, trophyId);
            statement.setString(3, awardedBy);
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
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
                    resultSet.getString("emoji"),
                    resultSet.getTimestamp("created_at").toLocalDateTime(),
                    resultSet.getString("created_by")
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
        String sql = "SELECT t.*, ta.awarded_at FROM trophies t " +
                    "JOIN trophy_awards ta ON t.id = ta.trophy_id " +
                    "WHERE ta.user_id = ? " +
                    "ORDER BY ta.awarded_at DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Trophy trophy = new Trophy(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("emoji"),
                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                        resultSet.getString("created_by")
                    );
                    UserTrophy userTrophy = new UserTrophy(trophy, resultSet.getTimestamp("awarded_at"));
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
                        resultSet.getString("emoji"),
                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                        resultSet.getString("created_by")
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
                        resultSet.getString("emoji"),
                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                        resultSet.getString("created_by")
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
        String sql = "SELECT user_id, COUNT(*) as count FROM trophy_awards GROUP BY user_id ORDER BY count DESC LIMIT ?";
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