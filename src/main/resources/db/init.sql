-- Create projects table
CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    client TEXT,
    status TEXT,
    start_date DATE,
    deadline DATE,
    budget REAL,
    hourly_rate REAL DEFAULT 0,
    description TEXT
);

-- Create time_entries table
CREATE TABLE IF NOT EXISTS time_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    start_time DATETIME,
    end_time DATETIME,
    description TEXT,
    FOREIGN KEY (project_id) REFERENCES projects(id)
); 