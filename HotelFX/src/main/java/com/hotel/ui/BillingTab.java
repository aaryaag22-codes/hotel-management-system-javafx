package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.*;
import com.hotel.service.PrintService;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

/**
 * BillingTab.java - View and generate bills with food charges
 * Demonstrates: Week 5 File I/O (receipt written to file), Week 8 Collections
 */
public class BillingTab {

    private final DataStore    store        = DataStore.getInstance();
    private final PrintService printService = new PrintService();

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(
            StyleUtil.pageHeader("💰", "Billing & Invoicing", "Generate and view guest bills")
        );

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // ── Bill Generator ───────────────────────────────────────────────
        VBox formCard = StyleUtil.createCard("🧾  Generate Bill");

        TextField bookingIdField = StyleUtil.styledField("Enter Booking ID");
        Button generateBtn = StyleUtil.primaryButton("🖩 Calculate Bill");
        Button printBtn    = StyleUtil.successButton("🖨 Save Receipt to File");

        TextArea billArea = StyleUtil.styledTextArea();
        billArea.setPrefHeight(460);

        generateBtn.setOnAction(e -> {
            try {
                int bid = Integer.parseInt(bookingIdField.getText().trim());
                billArea.setText(buildBillPreview(bid));
            } catch (NumberFormatException ex) {
                StyleUtil.showError("Invalid", "Enter a numeric booking ID.");
            }
        });

        printBtn.setOnAction(e -> {
            try {
                int bid = Integer.parseInt(bookingIdField.getText().trim());
                String receipt = printService.generateReceipt(bid);
                billArea.setText(receipt);
                StyleUtil.showInfo("Saved!", "Receipt saved to data/prints/receipt_" + bid + ".txt");
            } catch (NumberFormatException ex) {
                StyleUtil.showError("Invalid", "Enter a numeric booking ID.");
            }
        });

        HBox row = new HBox(12, bookingIdField, generateBtn, printBtn);
        formCard.getChildren().addAll(row, billArea);

        // ── Summary table ────────────────────────────────────────────────
        VBox summCard = StyleUtil.createCard("📊  Billing Summary (All Bookings)");
        TableView<Booking> table = StyleUtil.styledTable();
        table.setPrefHeight(280);
        table.getItems().addAll(store.getBookings());

        TableColumn<Booking, Number> bidCol  = tc("Booking #", 80);
        bidCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBookingId()));
        TableColumn<Booking, String> guestCol= tc("Guest", 150);
        guestCol.setCellValueFactory(c -> {
            Customer cu = store.getCustomerById(c.getValue().getCustomerId());
            return new javafx.beans.property.SimpleStringProperty(cu != null ? cu.getName() : "—");
        });
        TableColumn<Booking, Number> roomCol = tc("Room", 70);
        roomCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRoomNumber()));
        TableColumn<Booking, Number> amtCol  = tc("Room Charges ₹", 130);
        amtCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotalAmount()));
        TableColumn<Booking, Number> discCol = tc("Disc %", 70);
        discCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getDiscountApplied()));
        TableColumn<Booking, String> noteCol = tc("Discount Reason", 250);
        noteCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialNote()));
        TableColumn<Booking, String> statCol = tc("Status", 100);
        statCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().toString()));

        table.getColumns().addAll(bidCol, guestCol, roomCol, amtCol, discCol, noteCol, statCol);
        for (TableColumn<?, ?> col : table.getColumns()) {
    col.setStyle("-fx-text-fill: white;");
}
        summCard.getChildren().add(table);
        Platform.runLater(() -> {

            // Header background (premium navy)
            table.lookupAll(".column-header").forEach(node ->
                node.setStyle(
                    "-fx-background-color: linear-gradient(to right, #0f2027, #203a43, #2c5364);" +
                    "-fx-border-color: rgba(212,175,55,0.3);" +
                    "-fx-border-width: 0 1 1 0;"
                )
            );

            // Header text (GOLD)
            table.lookupAll(".column-header .label").forEach(node ->
                node.setStyle(
                    "-fx-text-fill: #d4af37;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;"
                )
            );
        });

        body.getChildren().addAll(formCard, summCard);
        root.getChildren().add(body);
        return root;
    }

    private String buildBillPreview(int bookingId) {
        Booking booking = store.getBookingById(bookingId);
        if (booking == null) return "❌ Booking #" + bookingId + " not found.";

        Customer c = store.getCustomerById(booking.getCustomerId());
        Room r     = store.getRoomByNumber(booking.getRoomNumber());
        List<FoodOrder> orders = store.getOrdersForBooking(bookingId);

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════════════\n");
        sb.append("         🏨  GRAND VISTA HOTEL — BILL PREVIEW\n");
        sb.append("══════════════════════════════════════════════════\n");
        if (c != null) {
            sb.append("Guest    : ").append(c.getName()).append("\n");
            sb.append("Contact  : ").append(c.getContactNumber()).append("\n");
        }
        sb.append("Booking  : #").append(bookingId).append("\n");
        sb.append("Room     : ").append(booking.getRoomNumber());
        if (r != null) sb.append(" (").append(r.getRoomType()).append(")");
        sb.append("\n");
        sb.append("Check-In : ").append(booking.getCheckIn()).append("\n");
        sb.append("Check-Out: ").append(booking.getCheckOut()).append("\n");
        sb.append("Days     : ").append(booking.getNumberOfDays()).append("\n");
        sb.append("──────────────────────────────────────────────────\n");

        double roomRate  = (r != null) ? r.getPricePerDay() : 0;
        double roomTotal = roomRate * booking.getNumberOfDays();
        sb.append(String.format("Room Charges  (₹%.0f x %d days): ₹%.2f%n",
                roomRate, booking.getNumberOfDays(), roomTotal));

        double foodTotal = 0;
        if (!orders.isEmpty()) {
            sb.append("\n  Room Service Orders:\n");
            for (FoodOrder order : orders) {
                for (Map.Entry<FoodItem, Integer> entry : order.getItems().entrySet()) {
                    double lt = entry.getKey().getPrice() * entry.getValue();
                    foodTotal += lt;
                    sb.append(String.format("  %-28s x%d  ₹%.2f%n",
                            entry.getKey().getName(), entry.getValue(), lt));
                }
            }
            sb.append(String.format("  Food Total: ₹%.2f%n", foodTotal));
        }

        double sub    = roomTotal + foodTotal;
        double disc   = booking.getDiscountApplied();
        double discAmt= sub * disc / 100.0;
        double after  = sub - discAmt;
        double gst    = after * 0.12;
        double total  = after + gst;

        sb.append("──────────────────────────────────────────────────\n");
        sb.append(String.format("Sub-Total    : ₹%.2f%n", sub));
        if (disc > 0) sb.append(String.format("Discount (%.0f%%): -₹%.2f  [%s]%n",
                disc, discAmt, booking.getSpecialNote()));
        sb.append(String.format("After Disc.  : ₹%.2f%n", after));
        sb.append(String.format("GST (12%%)    : ₹%.2f%n", gst));
        sb.append("══════════════════════════════════════════════════\n");
        sb.append(String.format("GRAND TOTAL  : ₹%.2f%n", total));
        sb.append("══════════════════════════════════════════════════\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> tc(String t, double w) {
        TableColumn<S, T> c = new TableColumn<>(t);
        c.setPrefWidth(w);
        return c;
    }
}