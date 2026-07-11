package com.restro.utility;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restro.dto.DailySalesDTO;
import com.restro.dto.FoodSalesDTO;
import com.restro.dto.RestaurantDTO;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Renders the sales report as a printable PDF (OpenPDF) - "Export Sales to PDF". */
public final class PdfReportUtil {

    private PdfReportUtil() {
    }

    public static void generate(OutputStream out, RestaurantDTO restaurant, String periodLabel,
                                 BigDecimal totalRevenue, int orderCount, BigDecimal avgOrderValue,
                                 List<FoodSalesDTO> topSelling, List<FoodSalesDTO> leastSelling,
                                 List<DailySalesDTO> dailySales) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        addCentered(document, restaurant.getName() + " - Sales Report", titleFont);
        addCentered(document, "Period: " + periodLabel, normalFont);
        document.add(new Paragraph(" "));

        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(60);
        addSummaryRow(summary, "Total Revenue", restaurant.getCurrencySymbol() + totalRevenue.toPlainString(), normalFont);
        addSummaryRow(summary, "Completed Orders", String.valueOf(orderCount), normalFont);
        addSummaryRow(summary, "Average Order Value", restaurant.getCurrencySymbol() + avgOrderValue.toPlainString(), normalFont);
        document.add(summary);
        document.add(new Paragraph(" "));

        addHeading(document, "Top Selling Items", headingFont);
        document.add(buildFoodSalesTable(topSelling, normalFont, headingFont));
        document.add(new Paragraph(" "));

        addHeading(document, "Least Selling Items", headingFont);
        document.add(buildFoodSalesTable(leastSelling, normalFont, headingFont));
        document.add(new Paragraph(" "));

        addHeading(document, "Daily Sales", headingFont);
        PdfPTable dailyTable = new PdfPTable(new float[]{3, 2, 3});
        dailyTable.setWidthPercentage(100);
        addHeaderCell(dailyTable, "Date", headingFont);
        addHeaderCell(dailyTable, "Orders", headingFont);
        addHeaderCell(dailyTable, "Revenue", headingFont);
        for (DailySalesDTO row : dailySales) {
            dailyTable.addCell(new Phrase(String.valueOf(row.getDate()), normalFont));
            dailyTable.addCell(new Phrase(String.valueOf(row.getOrderCount()), normalFont));
            dailyTable.addCell(new Phrase(restaurant.getCurrencySymbol() + row.getRevenue().toPlainString(), normalFont));
        }
        document.add(dailyTable);

        document.close();
    }

    private static PdfPTable buildFoodSalesTable(List<FoodSalesDTO> rows, Font normalFont, Font headingFont) {
        PdfPTable table = new PdfPTable(new float[]{4, 2, 2});
        table.setWidthPercentage(100);
        addHeaderCell(table, "Item", headingFont);
        addHeaderCell(table, "Qty Sold", headingFont);
        addHeaderCell(table, "Revenue", headingFont);
        for (FoodSalesDTO row : rows) {
            table.addCell(new Phrase(row.getName(), normalFont));
            table.addCell(new Phrase(String.valueOf(row.getTotalQuantity()), normalFont));
            table.addCell(new Phrase(row.getTotalRevenue().toPlainString(), normalFont));
        }
        return table;
    }

    private static void addCentered(Document document, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    private static void addHeading(Document document, String text, Font font) throws DocumentException {
        document.add(new Paragraph(text, font));
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(0);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(0);
        table.addCell(valueCell);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(240, 240, 240));
        table.addCell(cell);
    }
}
