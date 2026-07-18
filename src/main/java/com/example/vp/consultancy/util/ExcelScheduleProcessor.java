package com.example.vp.consultancy.util;

import com.example.vp.consultancy.dto.CreateDayRequestDTO;
import com.example.vp.consultancy.dto.CreateTaskRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class ExcelScheduleProcessor {

    public List<ScheduleDayData> parseScheduleExcel(MultipartFile file) throws IOException {
        List<ScheduleDayData> scheduleDayDataList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 0;
            
            for (Row row : sheet) {
                // Skip header row
                if (rowIndex == 0) {
                    rowIndex++;
                    continue;
                }
                
                // Skip empty rows
                if (isRowEmpty(row)) {
                    continue;
                }
                
                try {
                    String dayNumberStr = getCellValue(row, 0);
                    String title = getCellValue(row, 1);
                    String description = getCellValue(row, 2);
                    String fertilizerName = getCellValue(row, 3);
                    String quantity = getCellValue(row, 4);
                    String proportion = getCellValue(row, 5);
                    String priorityStr = getCellValue(row, 6);
                    String taskType = getCellValue(row, 7);
                    String taskDescription = getCellValue(row, 8);
                    
                    // Validate required fields
                    if (dayNumberStr == null || dayNumberStr.trim().isEmpty()) {
                        throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": day_number is required");
                    }
                    
                    if (title == null || title.trim().isEmpty()) {
                        throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": title is required");
                    }
                    
                    if (quantity == null || quantity.trim().isEmpty()) {
                        throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": quantity is required");
                    }
                    
                    Long dayNumber = Long.parseLong(dayNumberStr.trim());
                    Long priority = priorityStr != null && !priorityStr.trim().isEmpty() 
                        ? Long.parseLong(priorityStr.trim()) 
                        : null;
                    
                    // Check if day already exists in list
                    Optional<ScheduleDayData> existingDay = scheduleDayDataList.stream()
                        .filter(d -> d.getDayNumber().equals(dayNumber))
                        .findFirst();
                    
                    CreateTaskRequestDTO taskDTO = CreateTaskRequestDTO.builder()
                        .fertilizerName(fertilizerName)
                        .quantity(quantity)
                        .proportion(proportion)
                        .priority(priority)
                        .description(taskDescription != null ? taskDescription : "")
                        .taskType(taskType)
                        .build();
                    
                    if (existingDay.isPresent()) {
                        // Add task to existing day
                        existingDay.get().getTasks().add(taskDTO);
                    } else {
                        // Create new day
                        CreateDayRequestDTO dayDTO = CreateDayRequestDTO.builder()
                            .dayNumber(dayNumber)
                            .title(title)
                            .description(description)
                            .displayOrder((long) (scheduleDayDataList.size() + 1))
                            .build();
                        
                        ScheduleDayData dayData = new ScheduleDayData();
                        dayData.setDay(dayDTO);
                        dayData.setTasks(new ArrayList<>());
                        dayData.getTasks().add(taskDTO);
                        dayData.setDayNumber(dayNumber);
                        
                        scheduleDayDataList.add(dayData);
                    }
                    
                    rowIndex++;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": Invalid number format - " + e.getMessage());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }
        }
        
        if (scheduleDayDataList.isEmpty()) {
            throw new IllegalArgumentException("No valid schedule data found in Excel file");
        }
        
        return scheduleDayDataList;
    }
    
    private String getCellValue(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null) {
            return null;
        }
        
        return switch (row.getCell(cellIndex).getCellType()) {
            case STRING -> row.getCell(cellIndex).getStringCellValue();
            case NUMERIC -> String.valueOf((long) row.getCell(cellIndex).getNumericCellValue());
            case BOOLEAN -> String.valueOf(row.getCell(cellIndex).getBooleanCellValue());
            default -> null;
        };
    }
    
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (row.getCell(i) != null && !row.getCell(i).toString().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static class ScheduleDayData {
        private CreateDayRequestDTO day;
        private List<CreateTaskRequestDTO> tasks;
        private Long dayNumber;
        
        // Getters and Setters
        public CreateDayRequestDTO getDay() {
            return day;
        }
        
        public void setDay(CreateDayRequestDTO day) {
            this.day = day;
        }
        
        public List<CreateTaskRequestDTO> getTasks() {
            return tasks;
        }
        
        public void setTasks(List<CreateTaskRequestDTO> tasks) {
            this.tasks = tasks;
        }
        
        public Long getDayNumber() {
            return dayNumber;
        }
        
        public void setDayNumber(Long dayNumber) {
            this.dayNumber = dayNumber;
        }
    }
}
