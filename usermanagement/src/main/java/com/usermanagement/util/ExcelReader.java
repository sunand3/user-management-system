package com.usermanagement.util;

import com.usermanagement.model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelReader {

    public static List<User> readUsersFromExcel(InputStream inputStream) throws Exception {
        List<User> users = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Skip empty rows
                if (isRowEmpty(row)) {
                    continue;
                }

                try {
                    User user = new User();

                    // Name (Column 0)
                    Cell nameCell = row.getCell(0);
                    if (nameCell != null) {
                        user.setName(getCellValueAsString(nameCell));
                    }

                    // DOB (Column 1)
                    Cell dobCell = row.getCell(1);
                    if (dobCell != null) {
                        Date dob = getCellValueAsDate(dobCell, dateFormat);
                        user.setDob(dob);
                    }

                    // Email (Column 2)
                    Cell emailCell = row.getCell(2);
                    if (emailCell != null) {
                        user.setEmail(getCellValueAsString(emailCell));
                    }

                    // Password (Column 3)
                    Cell passwordCell = row.getCell(3);
                    if (passwordCell != null) {
                        user.setPassword(getCellValueAsString(passwordCell));
                    }

                    // Phone (Column 4)
                    Cell phoneCell = row.getCell(4);
                    if (phoneCell != null) {
                        user.setPhone(getCellValueAsString(phoneCell));
                    }

                    // Gender (Column 5)
                    Cell genderCell = row.getCell(5);
                    if (genderCell != null) {
                        user.setGender(getCellValueAsString(genderCell));
                    }

                    // Address (Column 6)
                    Cell addressCell = row.getCell(6);
                    if (addressCell != null) {
                        user.setAddress(getCellValueAsString(addressCell));
                    }

                    // Validate user before adding
                    if (isValidUser(user)) {
                        users.add(user);
                    }

                } catch (Exception e) {
                    System.err.println("Error processing row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
        }

        return users;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static Date getCellValueAsDate(Cell cell, SimpleDateFormat dateFormat) throws Exception {
        if (cell == null) {
            return new Date();
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue().trim();
            return dateFormat.parse(dateStr);
        }

        return new Date();
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidUser(User user) {
        return user.getName() != null && !user.getName().isEmpty() &&
                user.getEmail() != null && !user.getEmail().isEmpty() &&
                user.getPassword() != null && !user.getPassword().isEmpty();
    }
}