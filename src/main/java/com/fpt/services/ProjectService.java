package com.fpt.services;

import com.fpt.models.Project;
import com.fpt.models.ProjectStatus;
import com.fpt.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectService {
    private final DatabaseManager dbManager;

    public ProjectService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void saveProject(Project project) {
        if (project.getId() == null) {
            // Вставка нового проекта
            String insertSql = "INSERT INTO projects (name, client, status, start_date, deadline, budget, hourly_rate, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(insertSql)) {
                stmt.setString(1, project.getName());
                stmt.setString(2, project.getClient());
                stmt.setString(3, project.getStatus().name());
                stmt.setDate(4, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
                stmt.setDate(5, project.getDeadline() != null ? Date.valueOf(project.getDeadline()) : null);
                stmt.setDouble(6, project.getBudget());
                stmt.setDouble(7, project.getHourlyRate());
                stmt.setString(8, project.getDescription());
                
                stmt.executeUpdate();
                
                // Получаем ID вставленной записи
                try (Statement idStmt = dbManager.getConnection().createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        project.setId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save project", e);
            }
        } else {
            // Обновление существующего проекта
            String updateSql = "UPDATE projects SET name=?, client=?, status=?, start_date=?, deadline=?, budget=?, hourly_rate=?, description=? WHERE id=?";
            try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(updateSql)) {
                stmt.setString(1, project.getName());
                stmt.setString(2, project.getClient());
                stmt.setString(3, project.getStatus().name());
                stmt.setDate(4, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
                stmt.setDate(5, project.getDeadline() != null ? Date.valueOf(project.getDeadline()) : null);
                stmt.setDouble(6, project.getBudget());
                stmt.setDouble(7, project.getHourlyRate());
                stmt.setString(8, project.getDescription());
                stmt.setLong(9, project.getId());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update project", e);
            }
        }
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";

        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("id"));
                project.setName(rs.getString("name"));
                project.setClient(rs.getString("client"));
                project.setStatus(ProjectStatus.valueOf(rs.getString("status")));
                
                Date startDate = rs.getDate("start_date");
                if (startDate != null) {
                    project.setStartDate(startDate.toLocalDate());
                }
                
                Date deadline = rs.getDate("deadline");
                if (deadline != null) {
                    project.setDeadline(deadline.toLocalDate());
                }
                
                project.setBudget(rs.getDouble("budget"));
                project.setHourlyRate(rs.getDouble("hourly_rate"));
                project.setDescription(rs.getString("description"));
                
                projects.add(project);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch projects", e);
        }

        return projects;
    }

    public void deleteProject(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete project", e);
        }
    }

    public Project getProjectById(Long id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("id"));
                project.setName(rs.getString("name"));
                project.setClient(rs.getString("client"));
                project.setStatus(ProjectStatus.valueOf(rs.getString("status")));
                
                Date startDate = rs.getDate("start_date");
                if (startDate != null) {
                    project.setStartDate(startDate.toLocalDate());
                }
                
                Date deadline = rs.getDate("deadline");
                if (deadline != null) {
                    project.setDeadline(deadline.toLocalDate());
                }
                
                project.setBudget(rs.getDouble("budget"));
                project.setHourlyRate(rs.getDouble("hourly_rate"));
                project.setDescription(rs.getString("description"));
                
                return project;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch project", e);
        }
        return null;
    }

    public Project findProjectByNameAndClient(String name, String client) {
        String sql = "SELECT * FROM projects WHERE name = ? AND client = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, client);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("id"));
                project.setName(rs.getString("name"));
                project.setClient(rs.getString("client"));
                project.setStatus(ProjectStatus.valueOf(rs.getString("status")));
                
                Date startDate = rs.getDate("start_date");
                if (startDate != null) {
                    project.setStartDate(startDate.toLocalDate());
                }
                
                Date deadline = rs.getDate("deadline");
                if (deadline != null) {
                    project.setDeadline(deadline.toLocalDate());
                }
                
                project.setBudget(rs.getDouble("budget"));
                project.setHourlyRate(rs.getDouble("hourly_rate"));
                project.setDescription(rs.getString("description"));
                
                return project;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch project by name and client", e);
        }
        return null;
    }
} 