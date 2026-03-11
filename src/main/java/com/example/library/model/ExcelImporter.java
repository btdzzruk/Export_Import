package com.example.library.model;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ExcelImporter {

    public static <T> List<T> importFromExcel(InputStream inputStream, Class<T> clazz) {

        List<T> list = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            Field[] fields = clazz.getDeclaredFields();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                T obj = clazz.getDeclaredConstructor().newInstance();

                for (Field field : fields) {

                    field.setAccessible(true);

                    if (field.isAnnotationPresent(ExcelColumn.class)) {

                        ExcelColumn column = field.getAnnotation(ExcelColumn.class);

                        Cell cell = row.getCell(column.col());

                        Object value = getCellValue(cell, column.type());

                        Object convertedValue = convertValue(value, field.getType());

                        field.set(obj, convertedValue);
                    }
                }

                list.add(obj);
            }

        } catch (Exception e) {
            throw new RuntimeException("Import Excel failed: " + e.getMessage());
        }

        return list;
    }

    private static Object getCellValue(Cell cell, ColCellType type) {

        if (cell == null) return null;

        switch (type) {

            case _INTEGER:
                return cell.getNumericCellValue();

            case _DOUBLE:
            case _DOLLARS:
                return cell.getNumericCellValue();

            case _STRING:
            default:
                return cell.toString();
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {

        if (value == null) return null;

        if (targetType == Integer.class) {
            return ((Number) value).intValue();
        }

        if (targetType == Long.class) {
            return ((Number) value).longValue();
        }

        if (targetType == Double.class) {
            return ((Number) value).doubleValue();
        }

        if (targetType == String.class) {
            return value.toString();
        }

        return value;
    }

    public static <T> void importStreaming(
            InputStream inputStream,
            Class<T> clazz,
            int batchSize,
            java.util.function.Consumer<List<T>> batchConsumer) {

        List<T> batch = new ArrayList<>(batchSize);

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                T obj = clazz.getDeclaredConstructor().newInstance();

                for (Field field : fields) {

                    field.setAccessible(true);

                    if (field.isAnnotationPresent(ExcelColumn.class)) {

                        ExcelColumn column = field.getAnnotation(ExcelColumn.class);
                        Cell cell = row.getCell(column.col());

                        Object value = getCellValue(cell, column.type());
                        Object convertedValue = convertValue(value, field.getType());

                        field.set(obj, convertedValue);
                    }
                }

                batch.add(obj);

                if (batch.size() >= batchSize) {
                    batchConsumer.accept(new ArrayList<>(batch));
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                batchConsumer.accept(batch);
            }

        } catch (Exception e) {
            throw new RuntimeException("Import streaming failed: " + e.getMessage(), e);
        }
    }
}