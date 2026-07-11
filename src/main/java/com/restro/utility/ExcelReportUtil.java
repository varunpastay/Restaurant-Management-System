package com.restro.utility;

import com.restro.dto.DailySalesDTO;
import com.restro.dto.FoodSalesDTO;
import com.restro.dto.RestaurantDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Renders the sales report as a multi-sheet .xlsx workbook (Apache POI) - "Export Sales to Excel". */
public final class ExcelReportUtil {

    private ExcelReportUtil() {
    }

    public static void generate(OutputStream out, RestaurantDTO restaurant, String periodLabel,
                                 BigDecimal totalRevenue, int orderCount, BigDecimal avgOrderValue,
                                 List<FoodSalesDTO> topSelling, List<FoodSalesDTO> leastSelling,
                                 List<DailySalesDTO> dailySales) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            writeSummarySheet(workbook, headerStyle, restaurant, periodLabel, totalRevenue, orderCount, avgOrderValue);
            writeFoodSalesSheet(workbook, headerStyle, "Top Selling Items", topSelling);
            writeFoodSalesSheet(workbook, headerStyle, "Least Selling Items", leastSelling);
            writeDailySalesSheet(workbook, headerStyle, dailySales);

            workbook.write(out);
        }
    }

    private static void writeSummarySheet(XSSFWorkbook workbook, CellStyle headerStyle, RestaurantDTO restaurant,
                                           String periodLabel, BigDecimal totalRevenue, int orderCount, BigDecimal avgOrderValue) {
        Sheet sheet = workbook.createSheet("Summary");
        int rowNum = 0;
        writeRow(sheet, rowNum++, headerStyle, restaurant.getName() + " - Sales Report");
        writeRow(sheet, rowNum++, null, "Period", periodLabel);
        rowNum++;
        writeRow(sheet, rowNum++, headerStyle, "Metric", "Value");
        writeRow(sheet, rowNum++, null, "Total Revenue", totalRevenue.toPlainString());
        writeRow(sheet, rowNum++, null, "Completed Orders", String.valueOf(orderCount));
        writeRow(sheet, rowNum, null, "Average Order Value", avgOrderValue.toPlainString());
        autoSize(sheet, 2);
    }

    private static void writeFoodSalesSheet(XSSFWorkbook workbook, CellStyle headerStyle, String sheetName, List<FoodSalesDTO> rows) {
        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;
        writeRow(sheet, rowNum++, headerStyle, "Item", "Quantity Sold", "Revenue");
        for (FoodSalesDTO row : rows) {
            writeRow(sheet, rowNum++, null, row.getName(), String.valueOf(row.getTotalQuantity()), row.getTotalRevenue().toPlainString());
        }
        autoSize(sheet, 3);
    }

    private static void writeDailySalesSheet(XSSFWorkbook workbook, CellStyle headerStyle, List<DailySalesDTO> rows) {
        Sheet sheet = workbook.createSheet("Daily Sales");
        int rowNum = 0;
        writeRow(sheet, rowNum++, headerStyle, "Date", "Orders", "Revenue");
        for (DailySalesDTO row : rows) {
            writeRow(sheet, rowNum++, null, String.valueOf(row.getDate()), String.valueOf(row.getOrderCount()), row.getRevenue().toPlainString());
        }
        autoSize(sheet, 3);
    }

    private static void writeRow(Sheet sheet, int rowNum, CellStyle style, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i]);
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    private static void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
