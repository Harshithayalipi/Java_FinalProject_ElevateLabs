package com.harshitha.pdfreport.generator;

import com.harshitha.pdfreport.model.Employee;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enhanced PDF Report Generator with improved table formatting and 4-sided borders
 */
public class PDFReportGenerator {

    private static final float MARGIN = 50;
    private static final float ROW_HEIGHT = 25;
    private static final float HEADER_HEIGHT = 30;
    private static final float CELL_PADDING = 8;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Adjusted column widths to fit within page margins (total ~495 points)
    private static final float[] COLUMN_WIDTHS = {50, 90, 80, 110, 70, 80};
    private static final String[] HEADERS = {"ID", "Name", "Department", "Position", "Salary", "Hire Date"};

    /**
     * Generate a comprehensive employee report
     */
    public void generateEmployeeReport(List<Employee> employees, String outputPath) throws IOException {
        String filename = outputPath + "/Employee_Report_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            // Header
            yPosition = drawHeader(contentStream, yPosition, "Employee Report", employees.size(), page);
            yPosition -= 40;

            // Summary Statistics
            yPosition = drawSummarySection(contentStream, yPosition, employees);
            yPosition -= 40;

            // Employee Table with enhanced formatting
            drawEnhancedEmployeeTable(contentStream, yPosition, employees, document);

            contentStream.close();
            document.save(filename);
            System.out.println("Employee report generated: " + filename);
        }
    }

    /**
     * Generate department-wise report
     */
    public void generateDepartmentReport(List<Employee> employees, String department, String outputPath) throws IOException {
        String filename = outputPath + "/Department_Report_" + department.replaceAll("\\s+", "_") + "_" +
                LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            // Header
            yPosition = drawHeader(contentStream, yPosition, department + " Department Report", employees.size(), page);
            yPosition -= 40;

            // Department Statistics
            yPosition = drawDepartmentStats(contentStream, yPosition, employees, department);
            yPosition -= 40;

            // Employee Table with enhanced formatting
            drawEnhancedEmployeeTable(contentStream, yPosition, employees, document);

            contentStream.close();
            document.save(filename);
            System.out.println("Department report generated: " + filename);
        }
    }

    /**
     * Generate salary analysis report
     */
    public void generateSalaryReport(List<Employee> employees, String outputPath) throws IOException {
        String filename = outputPath + "/Salary_Analysis_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            // Header
            yPosition = drawHeader(contentStream, yPosition, "Salary Analysis Report", employees.size(), page);
            yPosition -= 40;

            // Salary Statistics
            yPosition = drawSalaryAnalysis(contentStream, yPosition, employees);
            yPosition -= 40;

            // High Earners Table
            List<Employee> highEarners = employees.stream()
                    .filter(emp -> emp.getSalary() > getAverageSalary(employees))
                    .sorted((e1, e2) -> Double.compare(e2.getSalary(), e1.getSalary()))
                    .toList();

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Above Average Earners");
            contentStream.endText();
            yPosition -= 30;

            drawEnhancedEmployeeTable(contentStream, yPosition, highEarners, document);

            contentStream.close();
            document.save(filename);
            System.out.println("Salary analysis report generated: " + filename);
        }
    }

    /**
     * Draw enhanced employee table with complete 4-sided borders and professional formatting
     */
    private void drawEnhancedEmployeeTable(PDPageContentStream contentStream, float yPosition, List<Employee> employees, PDDocument document) throws IOException {
        float tableWidth = getTotalWidth(COLUMN_WIDTHS);
        float tableStartX = MARGIN;
        float tableStartY = yPosition;

        // Draw table background
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.addRect(tableStartX, yPosition - (employees.size() + 1) * ROW_HEIGHT - 10, tableWidth, (employees.size() + 1) * ROW_HEIGHT + 10);
        contentStream.fill();

        // Draw header row
        drawTableHeader(contentStream, tableStartX, yPosition);
        yPosition -= HEADER_HEIGHT;

        // Draw data rows
        boolean alternateRow = false;
        for (Employee emp : employees) {
            if (yPosition < MARGIN + 50) {
                // Add new page if needed
                contentStream.close();
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = newPage.getMediaBox().getHeight() - MARGIN;

                // Redraw header on new page
                drawTableHeader(contentStream, tableStartX, yPosition);
                yPosition -= HEADER_HEIGHT;
                alternateRow = false;
            }

            drawTableRow(contentStream, tableStartX, yPosition, emp, alternateRow);
            yPosition -= ROW_HEIGHT;
            alternateRow = !alternateRow;
        }

        // Draw complete table border (4 sides)
        drawTableBorder(contentStream, tableStartX, tableStartY, tableWidth, employees.size());
    }

    /**
     * Draw table header with enhanced styling
     */
    private void drawTableHeader(PDPageContentStream contentStream, float startX, float yPosition) throws IOException {
        // Header background
        contentStream.setNonStrokingColor(new Color(70, 130, 180)); // Steel blue
        contentStream.addRect(startX, yPosition - HEADER_HEIGHT, getTotalWidth(COLUMN_WIDTHS), HEADER_HEIGHT);
        contentStream.fill();

        // Header text
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

        float currentX = startX;
        for (int i = 0; i < HEADERS.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(currentX + CELL_PADDING, yPosition - 20);
            contentStream.showText(HEADERS[i]);
            contentStream.endText();
            currentX += COLUMN_WIDTHS[i];
        }

        // Draw header bottom border
        contentStream.setStrokingColor(new Color(50, 100, 150));
        contentStream.setLineWidth(2);
        contentStream.moveTo(startX, yPosition - HEADER_HEIGHT);
        contentStream.lineTo(startX + getTotalWidth(COLUMN_WIDTHS), yPosition - HEADER_HEIGHT);
        contentStream.stroke();
    }

    /**
     * Draw individual table row with data
     */
    private void drawTableRow(PDPageContentStream contentStream, float startX, float yPosition, Employee emp, boolean alternateRow) throws IOException {
        // Row background
        if (alternateRow) {
            contentStream.setNonStrokingColor(new Color(248, 248, 248));
            contentStream.addRect(startX, yPosition - ROW_HEIGHT, getTotalWidth(COLUMN_WIDTHS), ROW_HEIGHT);
            contentStream.fill();
        }

        // Row data
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(PDType1Font.HELVETICA, 10);

        String[] rowData = {
                String.valueOf(emp.getEmployeeId()),
                emp.getFullName(),
                emp.getDepartment(),
                emp.getPosition(),
                "$" + String.format("%,d", (int)emp.getSalary()),
                emp.getHireDate().toString()
        };

        float currentX = startX;
        for (int i = 0; i < rowData.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(currentX + CELL_PADDING, yPosition - 16);

            String text = rowData[i];
            // Truncate text if too long
            if (text.length() > getMaxCharsForColumn(COLUMN_WIDTHS[i])) {
                text = text.substring(0, getMaxCharsForColumn(COLUMN_WIDTHS[i]) - 3) + "...";
            }
            contentStream.showText(text);
            contentStream.endText();
            currentX += COLUMN_WIDTHS[i];
        }

        // Draw row separator line
        contentStream.setStrokingColor(new Color(220, 220, 220));
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(startX, yPosition - ROW_HEIGHT);
        contentStream.lineTo(startX + getTotalWidth(COLUMN_WIDTHS), yPosition - ROW_HEIGHT);
        contentStream.stroke();
    }

    /**
     * Draw complete table border on all 4 sides
     */
    private void drawTableBorder(PDPageContentStream contentStream, float startX, float startY, float tableWidth, int rowCount) throws IOException {
        float tableHeight = (rowCount + 1) * ROW_HEIGHT + (HEADER_HEIGHT - ROW_HEIGHT);

        contentStream.setStrokingColor(new Color(70, 130, 180));
        contentStream.setLineWidth(2);

        // Top border
        contentStream.moveTo(startX, startY);
        contentStream.lineTo(startX + tableWidth, startY);
        contentStream.stroke();

        // Bottom border
        contentStream.moveTo(startX, startY - tableHeight);
        contentStream.lineTo(startX + tableWidth, startY - tableHeight);
        contentStream.stroke();

        // Left border
        contentStream.moveTo(startX, startY);
        contentStream.lineTo(startX, startY - tableHeight);
        contentStream.stroke();

        // Right border
        contentStream.moveTo(startX + tableWidth, startY);
        contentStream.lineTo(startX + tableWidth, startY - tableHeight);
        contentStream.stroke();

        // Vertical column separators
        contentStream.setLineWidth(1);
        contentStream.setStrokingColor(new Color(150, 150, 150));
        float currentX = startX;
        for (int i = 0; i < COLUMN_WIDTHS.length - 1; i++) {
            currentX += COLUMN_WIDTHS[i];
            contentStream.moveTo(currentX, startY);
            contentStream.lineTo(currentX, startY - tableHeight);
            contentStream.stroke();
        }
    }

    /**
     * Draw report header with enhanced styling - Fixed with page parameter
     */
    private float drawHeader(PDPageContentStream contentStream, float yPosition, String title, int employeeCount, PDPage page) throws IOException {
        // Title with enhanced styling
        contentStream.setNonStrokingColor(new Color(70, 130, 180));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= 30;

        // Subtitle information
        contentStream.setNonStrokingColor(new Color(100, 100, 100));
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        contentStream.endText();
        yPosition -= 18;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Total Employees: " + employeeCount);
        contentStream.endText();
        yPosition -= 18;

        // Enhanced separator line
        contentStream.setStrokingColor(new Color(70, 130, 180));
        contentStream.setLineWidth(3);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
        contentStream.stroke();

        return yPosition - 15;
    }

    /**
     * Draw summary statistics section
     */
    private float drawSummarySection(PDPageContentStream contentStream, float yPosition, List<Employee> employees) throws IOException {
        contentStream.setNonStrokingColor(new Color(70, 130, 180));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Summary Statistics");
        contentStream.endText();
        yPosition -= 25;

        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(PDType1Font.HELVETICA, 11);

        // Calculate statistics
        double avgSalary = getAverageSalary(employees);
        double minSalary = employees.stream().mapToDouble(Employee::getSalary).min().orElse(0);
        double maxSalary = employees.stream().mapToDouble(Employee::getSalary).max().orElse(0);
        long departmentCount = employees.stream().map(Employee::getDepartment).distinct().count();

        String[] stats = {
                "Average Salary: $" + String.format("%,.2f", avgSalary),
                "Salary Range: $" + String.format("%,.0f", minSalary) + " - $" + String.format("%,.0f", maxSalary),
                "Departments: " + departmentCount,
                "Latest Hire: " + employees.stream().map(Employee::getHireDate).max(LocalDate::compareTo).orElse(null)
        };

        for (String stat : stats) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(stat);
            contentStream.endText();
            yPosition -= 16;
        }

        return yPosition;
    }

    /**
     * Draw department-specific statistics
     */
    private float drawDepartmentStats(PDPageContentStream contentStream, float yPosition, List<Employee> employees, String department) throws IOException {
        contentStream.setNonStrokingColor(new Color(70, 130, 180));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Department Statistics");
        contentStream.endText();
        yPosition -= 25;

        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(PDType1Font.HELVETICA, 11);

        double avgSalary = getAverageSalary(employees);
        long positionCount = employees.stream().map(Employee::getPosition).distinct().count();

        String[] stats = {
                "Department: " + department,
                "Employee Count: " + employees.size(),
                "Average Salary: $" + String.format("%,.2f", avgSalary),
                "Unique Positions: " + positionCount
        };

        for (String stat : stats) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(stat);
            contentStream.endText();
            yPosition -= 16;
        }

        return yPosition;
    }

    /**
     * Draw salary analysis section
     */
    private float drawSalaryAnalysis(PDPageContentStream contentStream, float yPosition, List<Employee> employees) throws IOException {
        contentStream.setNonStrokingColor(new Color(70, 130, 180));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Salary Analysis");
        contentStream.endText();
        yPosition -= 25;

        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(PDType1Font.HELVETICA, 11);

        double avgSalary = getAverageSalary(employees);
        long aboveAvg = employees.stream().filter(emp -> emp.getSalary() > avgSalary).count();
        long belowAvg = employees.stream().filter(emp -> emp.getSalary() < avgSalary).count();

        String[] stats = {
                "Average Salary: $" + String.format("%,.2f", avgSalary),
                "Above Average: " + aboveAvg + " employees",
                "Below Average: " + belowAvg + " employees",
                "Highest Paid: " + employees.stream().max((e1, e2) -> Double.compare(e1.getSalary(), e2.getSalary())).map(Employee::getFullName).orElse("N/A")
        };

        for (String stat : stats) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(stat);
            contentStream.endText();
            yPosition -= 16;
        }

        return yPosition;
    }

    /**
     * Calculate average salary
     */
    private double getAverageSalary(List<Employee> employees) {
        return employees.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
    }

    /**
     * Get total width of all columns
     */
    private float getTotalWidth(float[] columnWidths) {
        float total = 0;
        for (float width : columnWidths) {
            total += width;
        }
        return total;
    }

    /**
     * Calculate maximum characters that fit in a column
     */
    private int getMaxCharsForColumn(float columnWidth) {
        return (int) ((columnWidth - 2 * CELL_PADDING) / 6);
    }
}