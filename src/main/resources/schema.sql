-- Trophies table
CREATE TABLE IF NOT EXISTS trophies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    emoji TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL
);

-- Trophy awards table
CREATE TABLE IF NOT EXISTS trophy_awards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trophy_id INTEGER NOT NULL,
    user_id TEXT NOT NULL,
    awarded_by TEXT NOT NULL,
    awarded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trophy_id) REFERENCES trophies(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_trophy_awards_user ON trophy_awards(user_id);
CREATE INDEX IF NOT EXISTS idx_trophy_awards_trophy ON trophy_awards(trophy_id); 