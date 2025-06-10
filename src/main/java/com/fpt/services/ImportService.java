package com.fpt.services;

import com.fpt.models.Project;
import com.fpt.models.ProjectStatus;
import com.fpt.models.TimeEntry;
import com.fpt.utils.DatabaseManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ImportService {
    private final ProjectService projectService;
    private final TimeEntryService timeEntryService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ImportService() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        this.projectService = new ProjectService(dbManager);
        this.timeEntryService = new TimeEntryService(dbManager);
    }

    public void importFromFile(File file, String platform, boolean importCompleted,
                             boolean updateExisting, boolean importTimeEntries) throws Exception {
        String extension = getFileExtension(file);
        List<Project> projects;

        switch (extension.toLowerCase()) {
            case "csv":
                projects = importFromCsv(file, platform);
                break;
            case "xlsx":
            case "xls":
                projects = importFromExcel(file, platform);
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый формат файла: " + extension);
        }

        saveProjects(projects, importCompleted, updateExisting, importTimeEntries);
    }

    public void importFromApi(String platform, String apiKey, boolean importCompleted,
                            boolean updateExisting, boolean importTimeEntries) throws Exception {
        // TODO: Реализовать импорт через API для каждой платформы
        throw new UnsupportedOperationException("Импорт через API находится в разработке");
    }

    private List<Project> importFromCsv(File file, String platform) throws Exception {
        List<Project> projects = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Пропускаем заголовок
                }
                
                String[] fields = line.split(",");
                Project project = parseProjectFromCsv(fields, platform);
                if (project != null) {
                    projects.add(project);
                }
            }
        }
        
        return projects;
    }

    private List<Project> importFromExcel(File file, String platform) throws Exception {
        List<Project> projects = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;
            
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // Пропускаем заголовок
                }
                
                Project project = parseProjectFromExcel(row, platform);
                if (project != null) {
                    projects.add(project);
                }
            }
        }
        
        return projects;
    }

    private Project parseProjectFromCsv(String[] fields, String platform) {
        try {
            Project project = new Project();
            project.setName(fields[0]);
            project.setDescription(fields[1]);
            project.setClient(fields[2]);
            project.setBudget(Double.parseDouble(fields[3]));
            project.setHourlyRate(Double.parseDouble(fields[4]));
            project.setStatus(ProjectStatus.fromString(fields[5]));
            return project;
        } catch (Exception e) {
            System.err.println("Ошибка при разборе строки CSV: " + String.join(",", fields));
            return null;
        }
    }

    private Project parseProjectFromExcel(Row row, String platform) {
        try {
            Project project = new Project();
            project.setName(getCellValue(row.getCell(0)));
            project.setDescription(getCellValue(row.getCell(1)));
            project.setClient(getCellValue(row.getCell(2)));
            project.setBudget(Double.parseDouble(getCellValue(row.getCell(3))));
            project.setHourlyRate(Double.parseDouble(getCellValue(row.getCell(4))));
            project.setStatus(ProjectStatus.fromString(getCellValue(row.getCell(5))));
            return project;
        } catch (Exception e) {
            System.err.println("Ошибка при разборе строки Excel: " + row.getRowNum());
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMATTER.format(cell.getLocalDateTimeCellValue());
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private void saveProjects(List<Project> projects, boolean importCompleted,
                            boolean updateExisting, boolean importTimeEntries) {
        for (Project project : projects) {
            // Пропускаем завершенные проекты, если не указано их импортировать
            if (!importCompleted && project.getStatus() == ProjectStatus.COMPLETED) {
                continue;
            }

            try {
                // Проверяем существование проекта по имени и клиенту
                Project existingProject = projectService.findProjectByNameAndClient(
                    project.getName(), project.getClient());

                if (existingProject != null) {
                    if (updateExisting) {
                        project.setId(existingProject.getId());
                        projectService.saveProject(project);
                    }
                } else {
                    projectService.saveProject(project);
                }

                // Импортируем записи времени, если указано
                if (importTimeEntries && project.getTimeEntries() != null) {
                    for (TimeEntry entry : project.getTimeEntries()) {
                        entry.setProject(project);
                        timeEntryService.saveTimeEntry(entry);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка при сохранении проекта: " + project.getName());
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
} 