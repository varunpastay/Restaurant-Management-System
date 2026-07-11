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
import com.restro.dto.OrderDTO;
import com.restro.dto.OrderItemDTO;
import com.restro.dto.PaymentDTO;
import com.restro.dto.RestaurantDTO;
import com.restro.dto.TaxDTO;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Renders a printable PDF bill/invoice for one order (OpenPDF). Works for both a not-yet-paid bill preview and a paid tax invoice. */
public final class PdfInvoiceUtil {

    private PdfInvoiceUtil() {
    }

    public static void generate(OutputStream out, RestaurantDTO restaurant, OrderDTO order,
                                 List<TaxDTO> taxes, PaymentDTO payment) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC);

        addCentered(document, restaurant.getName(), titleFont);
        if (ValidationUtil.isNotBlank(restaurant.getAddress())) {
            addCentered(document, restaurant.getAddress(), mutedFont);
        }
        String contactLine = buildContactLine(restaurant);
        if (ValidationUtil.isNotBlank(contactLine)) {
            addCentered(document, contactLine, mutedFont);
        }

        document.add(new Paragraph(" "));
        addCentered(document, payment != null ? "TAX INVOICE" : "BILL (Unpaid)", headingFont);
        document.add(new Paragraph(" "));

        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        addMetaRow(metaTable, "Order No", order.getOrderNo(), normalFont);
        addMetaRow(metaTable, "Table", order.getTableNo(), normalFont);
        addMetaRow(metaTable, "Date", String.valueOf(order.getCreatedAt()), normalFont);
        if (payment != null) {
            addMetaRow(metaTable, "Invoice No", payment.getInvoiceNo(), normalFont);
            addMetaRow(metaTable, "Payment Method", payment.getMethod().name(), normalFont);
        }
        document.add(metaTable);
        document.add(new Paragraph(" "));

        PdfPTable itemsTable = new PdfPTable(new float[]{4, 1, 2, 2});
        itemsTable.setWidthPercentage(100);
        addHeaderCell(itemsTable, "Item", headingFont);
        addHeaderCell(itemsTable, "Qty", headingFont);
        addHeaderCell(itemsTable, "Unit Price", headingFont);
        addHeaderCell(itemsTable, "Amount", headingFont);
        for (OrderItemDTO item : order.getItems()) {
            String label = item.getFoodNameSnapshot()
                    + (ValidationUtil.isNotBlank(item.getSpecialInstructions())
                    ? "\n(" + item.getSpecialInstructions() + ")" : "");
            itemsTable.addCell(new Phrase(label, normalFont));
            itemsTable.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
            itemsTable.addCell(new Phrase(formatMoney(restaurant, item.getUnitPrice()), normalFont));
            itemsTable.addCell(new Phrase(formatMoney(restaurant, item.getLineTotal()), normalFont));
        }
        document.add(itemsTable);
        document.add(new Paragraph(" "));

        if (ValidationUtil.isNotBlank(order.getCustomerNote())) {
            document.add(new Paragraph("Order note: " + order.getCustomerNote(), normalFont));
            document.add(new Paragraph(" "));
        }

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        addTotalRow(totalsTable, "Subtotal", formatMoney(restaurant, order.getSubtotal()), normalFont);
        if (order.getTaxAmount() != null && order.getTaxAmount().signum() > 0) {
            addTotalRow(totalsTable, buildTaxLabel(taxes), formatMoney(restaurant, order.getTaxAmount()), normalFont);
        }
        if (order.getServiceChargeAmount() != null && order.getServiceChargeAmount().signum() > 0) {
            addTotalRow(totalsTable, "Service Charge", formatMoney(restaurant, order.getServiceChargeAmount()), normalFont);
        }
        if (order.getDiscountAmount() != null && order.getDiscountAmount().signum() > 0) {
            addTotalRow(totalsTable, "Discount", "-" + formatMoney(restaurant, order.getDiscountAmount()), normalFont);
        }
        addTotalRow(totalsTable, "Grand Total", formatMoney(restaurant, order.getGrandTotal()), headingFont);
        document.add(totalsTable);

        document.add(new Paragraph(" "));
        addCentered(document, "Thank you for dining with us!", mutedFont);

        document.close();
    }

    private static String buildContactLine(RestaurantDTO restaurant) {
        StringBuilder line = new StringBuilder();
        if (ValidationUtil.isNotBlank(restaurant.getPhone())) {
            line.append(restaurant.getPhone());
        }
        if (ValidationUtil.isNotBlank(restaurant.getGstin())) {
            if (line.length() > 0) {
                line.append("  |  ");
            }
            line.append("GSTIN: ").append(restaurant.getGstin());
        }
        return line.toString();
    }

    private static String buildTaxLabel(List<TaxDTO> taxes) {
        if (taxes == null || taxes.isEmpty()) {
            return "Tax";
        }
        StringBuilder label = new StringBuilder();
        for (TaxDTO tax : taxes) {
            if (label.length() > 0) {
                label.append(" + ");
            }
            label.append(tax.getName()).append(" ").append(tax.getPercent()).append("%");
        }
        return label.toString();
    }

    private static void addCentered(Document document, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    private static void addMetaRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(0);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "" : value, font));
        valueCell.setBorder(0);
        table.addCell(valueCell);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(240, 240, 240));
        table.addCell(cell);
    }

    private static void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(0);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(0);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private static String formatMoney(RestaurantDTO restaurant, BigDecimal amount) {
        return restaurant.getCurrencySymbol() + amount.toPlainString();
    }
}
