package com.fpt.services;

import com.fpt.models.Project;
import com.fpt.models.TimeEntry;
import com.fpt.utils.DatabaseManager;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TimeEntryService timeEntryService;

    public PdfExportService() {
        this.timeEntryService = new TimeEntryService(DatabaseManager.getInstance());
    }

    public void exportProject(Project project) {
        String fileName = "project_" + project.getId() + "_report.pdf";
        
        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Project Details
            document.add(new Paragraph("Project Report")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Project Details")
                    .setFontSize(16)
                    .setBold());

            Table projectDetails = new Table(2);
            projectDetails.setWidth(UnitValue.createPercentValue(100));

            addTableRow(projectDetails, "Name:", project.getName());
            addTableRow(projectDetails, "Client:", project.getClient());
            addTableRow(projectDetails, "Status:", project.getStatus().toString());
            addTableRow(projectDetails, "Start Date:", 
                project.getStartDate() != null ? project.getStartDate().format(DATE_FORMATTER) : "Not set");
            addTableRow(projectDetails, "Deadline:", 
                project.getDeadline() != null ? project.getDeadline().format(DATE_FORMATTER) : "Not set");
            addTableRow(projectDetails, "Budget:", String.format("%.2f ₽", project.getBudget()));
            addTableRow(projectDetails, "Description:", project.getDescription());

            document.add(projectDetails);

            // Time Entries
            List<TimeEntry> timeEntries = timeEntryService.getTimeEntriesForProject(project.getId());

            if (!timeEntries.isEmpty()) {
                document.add(new Paragraph("\nTime Entries")
                        .setFontSize(16)
                        .setBold());

                Table timeEntriesTable = new Table(4);
                timeEntriesTable.setWidth(UnitValue.createPercentValue(100));

                // Header
                timeEntriesTable.addHeaderCell(new Cell().add(new Paragraph("Start Time")));
                timeEntriesTable.addHeaderCell(new Cell().add(new Paragraph("End Time")));
                timeEntriesTable.addHeaderCell(new Cell().add(new Paragraph("Duration (hours)")));
                timeEntriesTable.addHeaderCell(new Cell().add(new Paragraph("Description")));

                double totalHours = 0;

                // Data
                for (TimeEntry entry : timeEntries) {
                    timeEntriesTable.addCell(new Cell().add(new Paragraph(
                            entry.getStartTime().format(TIME_FORMATTER))));
                    
                    String endTime = entry.getEndTime() != null ? 
                            entry.getEndTime().format(TIME_FORMATTER) : "In Progress";
                    timeEntriesTable.addCell(new Cell().add(new Paragraph(endTime)));

                    double hours = 0;
                    if (entry.getEndTime() != null) {
                        hours = entry.getDurationInHours();
                        totalHours += hours;
                    }
                    timeEntriesTable.addCell(new Cell().add(new Paragraph(
                            String.format("%.2f", hours))));

                    timeEntriesTable.addCell(new Cell().add(new Paragraph(
                            entry.getDescription())));
                }

                document.add(timeEntriesTable);

                // Total hours and earnings
                document.add(new Paragraph(String.format("\nTotal Hours: %.2f", totalHours))
                        .setFontSize(14)
                        .setBold());
                
                double totalEarnings = totalHours * project.getHourlyRate();
                document.add(new Paragraph(String.format("Total Earnings: %.2f ₽", totalEarnings))
                        .setFontSize(14)
                        .setBold());
                
                double remainingBudget = project.getBudget() - totalEarnings;
                document.add(new Paragraph(String.format("Remaining Budget: %.2f ₽", remainingBudget))
                        .setFontSize(14)
                        .setBold());
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")));
    }
}