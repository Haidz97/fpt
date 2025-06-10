package com.fpt.services;

import com.fpt.models.TimeEntry;
import com.fpt.models.Project;
import com.fpt.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeEntryService {
    private final DatabaseManager databaseManager;
    private final ProjectService projectService;

    public TimeEntryService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.projectService = new ProjectService(databaseManager);
    }

    public void saveTimeEntry(TimeEntry timeEntry) {
        String sql = timeEntry.getId() == null ?
                "INSERT INTO time_entries (project_id, start_time, end_time, description) VALUES (?, ?, ?, ?)" :
                "UPDATE time_entries SET project_id = ?, start_time = ?, end_time = ?, description = ? WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, timeEntry.getProject().getId());
            pstmt.setTimestamp(2, Timestamp.valueOf(timeEntry.getStartTime()));
            pstmt.setTimestamp(3, timeEntry.getEndTime() != null ? Timestamp.valueOf(timeEntry.getEndTime()) : null);
            pstmt.setString(4, timeEntry.getDescription());

            if (timeEntry.getId() != null) {
                pstmt.setLong(5, timeEntry.getId());
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating/updating time entry failed, no rows affected.");
            }

            if (timeEntry.getId() == null) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        timeEntry.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating time entry failed, no ID obtained.");
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save time entry", e);
        }
    }

    public List<TimeEntry> getTimeEntriesForProject(Long projectId) {
        List<TimeEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM time_entries WHERE project_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeEntry entry = new TimeEntry();
                    entry.setId(rs.getLong("id"));
                    
                    Project project = projectService.getProjectById(projectId);
                    if (project == null) {
                        throw new RuntimeException("Project not found with ID: " + projectId);
                    }
                    entry.setProject(project);
                    
                    Timestamp startTime = rs.getTimestamp("start_time");
                    if (startTime != null) {
                        entry.setStartTime(startTime.toLocalDateTime());
                    }

                    Timestamp endTime = rs.getTimestamp("end_time");
                    if (endTime != null) {
                        entry.setEndTime(endTime.toLocalDateTime());
                    }

                    entry.setDescription(rs.getString("description"));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get time entries", e);
        }

        return entries;
    }

    public List<TimeEntry> getTimeEntriesForDate(LocalDateTime date) {
        List<TimeEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM time_entries WHERE DATE(start_time) = DATE(?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeEntry entry = new TimeEntry();
                    entry.setId(rs.getLong("id"));
                    
                    Long projectId = rs.getLong("project_id");
                    Project project = projectService.getProjectById(projectId);
                    if (project == null) {
                        throw new RuntimeException("Project not found with ID: " + projectId);
                    }
                    entry.setProject(project);
                    
                    Timestamp startTime = rs.getTimestamp("start_time");
                    if (startTime != null) {
                        entry.setStartTime(startTime.toLocalDateTime());
                    }

                    Timestamp endTime = rs.getTimestamp("end_time");
                    if (endTime != null) {
                        entry.setEndTime(endTime.toLocalDateTime());
                    }

                    entry.setDescription(rs.getString("description"));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get time entries", e);
        }

        return entries;
    }

    public void deleteTimeEntry(Long id) {
        String sql = "DELETE FROM time_entries WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete time entry", e);
        }
    }

    public void closeOpenTimeEntries(Long projectId) {
        String sql = "UPDATE time_entries SET end_time = ? WHERE project_id = ? AND end_time IS NULL";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(now));
            stmt.setLong(2, projectId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close open time entries", e);
        }
    }

    public List<TimeEntry> getAllTimeEntries() {
        List<TimeEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM time_entries ORDER BY start_time DESC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeEntry entry = new TimeEntry();
                    entry.setId(rs.getLong("id"));
                    
                    Long projectId = rs.getLong("project_id");
                    Project project = projectService.getProjectById(projectId);
                    if (project == null) {
                        throw new RuntimeException("Project not found with ID: " + projectId);
                    }
                    entry.setProject(project);
                    
                    Timestamp startTime = rs.getTimestamp("start_time");
                    if (startTime != null) {
                        entry.setStartTime(startTime.toLocalDateTime());
                    }

                    Timestamp endTime = rs.getTimestamp("end_time");
                    if (endTime != null) {
                        entry.setEndTime(endTime.toLocalDateTime());
                    }

                    entry.setDescription(rs.getString("description"));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all time entries", e);
        }

        return entries;
    }
} 