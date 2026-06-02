package com.hotel.service;

import com.hotel.data.DataStore;
import com.hotel.model.*;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PdfService.java - Generates PDF receipts using pure Java (no external library).
 *
 * We write an HTML file and convert it to a styled receipt saved as .html
 * which can be opened in any browser and printed as PDF using Ctrl+P -> Save as PDF.
 *
 * PDF is saved to: <UserHome>/Documents/GrandVista/Receipts/
 * e.g. /Users/macbook1/Documents/GrandVista/Receipts/receipt_1.html
 *
 * Demonstrates: Week 5 FileWriter (CharacterStream)
 */
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private final DataStore store = DataStore.getInstance();

    // PDF is saved here on the user's Mac:
    // /Users/<username>/Documents/GrandVista/Receipts/
    private String getOutputDir() {
        String home = System.getProperty("user.home");
        String dir  = home + File.separator + "Documents"
                           + File.separator + "GrandVista"
                           + File.separator + "Receipts";
        new File(dir).mkdirs();
        return dir;
    }

    /**
     * Generates a beautiful HTML receipt that can be printed as PDF from any browser.
     * Returns the full path to the saved file.
     */
    public String generateReceiptHtml(int bookingId) throws IOException {
        Booking booking = store.getBookingById(bookingId);
        if (booking == null) throw new IOException("Booking #" + bookingId + " not found.");

        Customer customer = store.getCustomerById(booking.getCustomerId());
        Room room         = store.getRoomByNumber(booking.getRoomNumber());
        List<FoodOrder> orders = store.getOrdersForBooking(bookingId);

        double roomRate   = (room != null) ? room.getPricePerDay() : 0;
        double roomTotal  = roomRate * booking.getNumberOfDays();
        double foodTotal  = 0;
        StringBuilder foodRows = new StringBuilder();

        for (FoodOrder order : orders) {
            for (Map.Entry<FoodItem, Integer> entry : order.getItems().entrySet()) {
                double lt = entry.getKey().getPrice() * entry.getValue();
                foodTotal += lt;
                foodRows.append("<tr><td>").append(entry.getKey().getName())
                        .append("</td><td class='center'>x").append(entry.getValue())
                        .append("</td><td class='right'>Rs. ").append(String.format("%.2f", lt))
                        .append("</td></tr>");
            }
        }

        double sub     = roomTotal + foodTotal;
        double disc    = booking.getDiscountApplied();
        double discAmt = sub * disc / 100.0;
        double after   = sub - discAmt;
        double gst     = after * 0.12;
        double total   = after + gst;

        String guestName = (customer != null) ? customer.getName() : "Guest";
        String guestPhone= (customer != null) ? customer.getContactNumber() : "—";
        String roomType  = (room != null) ? room.getRoomType() : "—";

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
            + "<title>Receipt #" + bookingId + " - Grand Vista Hotel</title>"
            + "<style>"
            + "body{font-family:Georgia,serif;background:#f9f6f0;color:#1a1a2e;margin:0;padding:20px;}"
            + ".receipt{max-width:680px;margin:0 auto;background:#fff;border-radius:12px;"
            + "  box-shadow:0 4px 20px rgba(0,0,0,0.15);overflow:hidden;}"
            + ".header{background:linear-gradient(135deg,#0f3460,#1a1a2e);color:white;padding:32px;text-align:center;}"
            + ".header h1{font-size:28px;letter-spacing:3px;color:#C9A84C;margin:0;}"
            + ".header p{color:#A0A0B0;margin:4px 0;font-style:italic;}"
            + ".gold-bar{height:4px;background:linear-gradient(to right,transparent,#C9A84C,transparent);}"
            + ".body{padding:28px 36px;}"
            + ".receipt-title{text-align:center;font-size:18px;font-weight:bold;color:#C9A84C;"
            + "  letter-spacing:2px;border-bottom:1px solid #C9A84C44;padding-bottom:12px;margin-bottom:20px;}"
            + ".info-grid{display:grid;grid-template-columns:1fr 1fr;gap:8px 20px;margin-bottom:20px;}"
            + ".info-item{display:flex;gap:8px;align-items:baseline;}"
            + ".info-label{color:#888;font-size:12px;min-width:100px;}"
            + ".info-value{font-weight:bold;font-size:13px;color:#1a1a2e;}"
            + "table{width:100%;border-collapse:collapse;margin:12px 0;font-size:13px;}"
            + "th{background:#0f3460;color:#C9A84C;padding:10px 12px;text-align:left;}"
            + "td{padding:9px 12px;border-bottom:1px solid #eee;}"
            + "tr:nth-child(even) td{background:#f5f5f5;}"
            + ".right{text-align:right;} .center{text-align:center;}"
            + ".totals{margin-top:16px;}"
            + ".total-row{display:flex;justify-content:space-between;padding:7px 0;"
            + "  border-bottom:1px solid #eee;font-size:13px;}"
            + ".grand-total{background:#0f3460;color:white;border-radius:8px;padding:14px 16px;"
            + "  display:flex;justify-content:space-between;font-size:17px;font-weight:bold;"
            + "  margin-top:12px;}"
            + ".grand-total span{color:#C9A84C;}"
            + ".footer{text-align:center;padding:20px;background:#f5f0e8;color:#888;font-size:11px;}"
            + ".discount{color:#27AE60;font-weight:bold;}"
            + "@media print{body{background:white;}.receipt{box-shadow:none;}}"
            + "</style></head><body>"
            + "<div class='receipt'>"
            + "<div class='header'>"
            + "<h1>🏨 GRAND VISTA HOTEL</h1>"
            + "<p>123 Palace Road, Udupi - 576101, Karnataka</p>"
            + "<p>Tel: +91-820-1234567 | grandvista@hotel.com</p>"
            + "</div>"
            + "<div class='gold-bar'></div>"
            + "<div class='body'>"
            + "<div class='receipt-title'>BILL RECEIPT</div>"
            + "<div class='info-grid'>"
            + infoItem("Booking #",    "#" + bookingId)
            + infoItem("Guest Name",   guestName)
            + infoItem("Contact",      guestPhone)
            + infoItem("Room No.",     String.valueOf(booking.getRoomNumber()))
            + infoItem("Room Type",    roomType)
            + infoItem("No. of Guests",String.valueOf(booking.getNumberOfGuests()))
            + infoItem("Check-In",     booking.getCheckIn().format(DATE_FMT))
            + infoItem("Check-Out",    booking.getCheckOut().format(DATE_FMT))
            + infoItem("No. of Days",  String.valueOf(booking.getNumberOfDays()))
            + infoItem("Status",       booking.getStatus().toString())
            + "</div>"
            + "<table><thead><tr><th>Description</th><th class='center'>Qty</th><th class='right'>Amount</th></tr></thead><tbody>"
            + "<tr><td>Room Charges (Rs." + String.format("%.0f", roomRate)
            +         " x " + booking.getNumberOfDays() + " nights)</td>"
            +         "<td class='center'>1</td>"
            +         "<td class='right'>Rs. " + String.format("%.2f", roomTotal) + "</td></tr>"
            + (foodTotal > 0 ? foodRows.toString() : "")
            + "</tbody></table>"
            + "<div class='totals'>"
            + totalRow("Sub Total", "Rs. " + String.format("%.2f", sub), false)
            + (disc > 0 ? "<div class='total-row'><span>Discount (" + disc + "%) — "
                        + booking.getSpecialNote() + "</span><span class='discount'>- Rs. "
                        + String.format("%.2f", discAmt) + "</span></div>" : "")
            + totalRow("After Discount", "Rs. " + String.format("%.2f", after), false)
            + totalRow("GST (12%)",        "Rs. " + String.format("%.2f", gst),   false)
            + "</div>"
            + "<div class='grand-total'><span>GRAND TOTAL</span><span>Rs. " + String.format("%.2f", total) + "</span></div>"
            + "<p style='text-align:center;color:#888;font-size:11px;margin-top:16px;'>"
            + "To save as PDF: Open this file in your browser → Press Cmd+P → Choose 'Save as PDF'</p>"
            + "</div>"
            + "<div class='footer'>"
            + "Thank you for staying at Grand Vista Hotel! We look forward to welcoming you again.<br>"
            + "© 2026 Grand Vista Hotel | All Rights Reserved"
            + "</div></div></body></html>";

        String filename = getOutputDir() + File.separator + "receipt_" + bookingId + ".html";
        try (java.io.FileWriter fw = new java.io.FileWriter(filename)) {
            fw.write(html);
        }
        return filename;
    }

    /**
     * Generates a printable food menu HTML page.
     * Saved to: ~/Documents/GrandVista/Receipts/food_menu.html
     */
    public String generateFoodMenuHtml() throws IOException {
        StringBuilder rows = new StringBuilder();
        FoodItem.FoodCategory lastCat = null;
        for (FoodItem item : store.getMenuItems()) {
            if (item.getCategory() != lastCat) {
                if (lastCat != null) rows.append("</tbody></table>");
                rows.append("<h3 style='color:#C9A84C;margin:20px 0 8px;'>")
                    .append(item.getCategory()).append("</h3>")
                    .append("<table><thead><tr><th>Item</th><th>Type</th><th class='right'>Price</th></tr></thead><tbody>");
                lastCat = item.getCategory();
            }
            rows.append("<tr><td>").append(item.getName()).append("</td>")
                .append("<td>").append(item.isVegetarian() ? "🌿 Veg" : "🍗 Non-Veg").append("</td>")
                .append("<td class='right'>Rs. ").append(String.format("%.0f", item.getPrice())).append("</td></tr>");
        }
        if (lastCat != null) rows.append("</tbody></table>");

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Food Menu</title>"
            + "<style>body{font-family:Georgia,serif;background:#f9f6f0;padding:20px;}"
            + ".wrap{max-width:700px;margin:0 auto;background:white;border-radius:12px;"
            + "  box-shadow:0 4px 20px rgba(0,0,0,.15);overflow:hidden;}"
            + ".header{background:linear-gradient(135deg,#0f3460,#1a1a2e);color:white;padding:28px;text-align:center;}"
            + ".header h1{color:#C9A84C;margin:0;letter-spacing:2px;}"
            + ".body{padding:24px 32px;}"
            + "table{width:100%;border-collapse:collapse;font-size:13px;margin-bottom:8px;}"
            + "th{background:#0f3460;color:#C9A84C;padding:9px 12px;text-align:left;}"
            + "td{padding:8px 12px;border-bottom:1px solid #eee;}"
            + "tr:nth-child(even) td{background:#f8f8f8;} .right{text-align:right;}"
            + "@media print{body{background:white;}.wrap{box-shadow:none;}}"
            + "</style></head><body><div class='wrap'>"
            + "<div class='header'><h1>🍽 GRAND VISTA HOTEL</h1><p style='color:#A0A0B0'>Food & Beverage Menu</p></div>"
            + "<div class='body'>" + rows + "</div></div></body></html>";

        String filename = getOutputDir() + File.separator + "food_menu.html";
        try (java.io.FileWriter fw = new java.io.FileWriter(filename)) { fw.write(html); }
        return filename;
    }

    private String infoItem(String label, String value) {
        return "<div class='info-item'><span class='info-label'>" + label
             + ":</span><span class='info-value'>" + value + "</span></div>";
    }

    private String totalRow(String label, String value, boolean bold) {
        String style = bold ? "font-weight:bold;" : "";
        return "<div class='total-row' style='" + style + "'><span>" + label
             + "</span><span>" + value + "</span></div>";
    }

    /** Returns the folder path where receipts are saved */
    public String getReceiptsFolder() { return getOutputDir(); }
}