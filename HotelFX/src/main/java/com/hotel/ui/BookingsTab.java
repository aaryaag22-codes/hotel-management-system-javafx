package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;

/**
 * BookingsTab.java - Book a room, view bookings, checkout
 * Demonstrates: Week 1, Week 2 Enum, Week 7 Generics (Pair), Week 8 Collections
 */
public class BookingsTab {

    private final DataStore store = DataStore.getInstance();
    private ObservableList<Booking> bookingList;
    private TableView<Booking> table;

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(
            StyleUtil.pageHeader("📋", "Booking Management", "Book rooms, view & checkout")
        );

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // ── Booking Form ─────────────────────────────────────────────────
        VBox formCard = StyleUtil.createCard("➕  New Room Booking");

        TextField custIdField  = StyleUtil.styledField("Customer ID (from Customers tab)");
        ComboBox<String> roomCombo = StyleUtil.styledCombo();
        refreshRoomCombo(roomCombo);
        roomCombo.setPromptText("Select available room");
        roomCombo.setPrefWidth(220);

        // ── Real-time refresh: whenever a room is added in RoomsTab, update this combo ──
        store.addRoomChangeListener(() ->
            javafx.application.Platform.runLater(() -> refreshRoomCombo(roomCombo))
        );

        DatePicker checkInPicker  = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        styleDP(checkInPicker); styleDP(checkOutPicker);

        TextField guestsField  = StyleUtil.styledField("Number of guests");
        TextField voucherField = StyleUtil.styledField("Voucher code (optional)");

        // Discount preview
        Label discountPreview = new Label("Discount: —");
        discountPreview.setStyle("-fx-text-fill: " + StyleUtil.GOLD + "; -fx-font-size: 13px;");

        // Auto-calculate discount when fields filled
        Runnable calcDiscount = () -> {
            try {
                int cid = Integer.parseInt(custIdField.getText().trim());
                Customer c = store.getCustomerById(cid);
                if (c == null) { discountPreview.setText("Customer not found"); return; }
                int guests = Integer.parseInt(guestsField.getText().trim());
                DataStore.Pair<Double, String> disc = store.calculateDiscount(
                        c, c.getSpecialOccasion(), guests, voucherField.getText().trim());
                discountPreview.setText("Discount: " + disc.getFirst() + "% → " + disc.getSecond());
            } catch (Exception ignore) {
                discountPreview.setText("Discount: fill all fields to preview");
            }
        };
        custIdField.setOnKeyReleased(e -> calcDiscount.run());
        guestsField.setOnKeyReleased(e -> calcDiscount.run());
        voucherField.setOnKeyReleased(e -> calcDiscount.run());

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(10);
        grid.add(StyleUtil.fieldLabel("Customer ID:"), 0, 0); grid.add(custIdField,   1, 0);
        grid.add(StyleUtil.fieldLabel("Room:"),        2, 0); grid.add(roomCombo,     3, 0);
        grid.add(StyleUtil.fieldLabel("Check-In:"),    0, 1); grid.add(checkInPicker, 1, 1);
        grid.add(StyleUtil.fieldLabel("Check-Out:"),   2, 1); grid.add(checkOutPicker,3, 1);
        grid.add(StyleUtil.fieldLabel("Guests:"),      0, 2); grid.add(guestsField,   1, 2);
        grid.add(StyleUtil.fieldLabel("Voucher Code:"),2, 2); grid.add(voucherField,  3, 2);

        Button bookBtn = StyleUtil.primaryButton("🛎 Confirm Booking");
        bookBtn.setOnAction(e -> handleBooking(custIdField, roomCombo, checkInPicker,
                checkOutPicker, guestsField, voucherField, discountPreview));

        formCard.getChildren().addAll(grid, discountPreview, bookBtn);

        // ── Checkout ─────────────────────────────────────────────────────
        VBox checkoutCard = StyleUtil.createCard("🚪  Checkout");
        TextField checkoutIdField = StyleUtil.styledField("Enter Booking ID to checkout");
        Button checkoutBtn = StyleUtil.dangerButton("🚪 Checkout");
        checkoutBtn.setOnAction(e -> {
            try {
                int bid = Integer.parseInt(checkoutIdField.getText().trim());
                Booking b = store.getBookingById(bid);
                if (b == null) { StyleUtil.showError("Not Found", "Booking #" + bid + " not found."); return; }
                if (b.getStatus() != Booking.BookingStatus.ACTIVE) {
                    StyleUtil.showError("Already Done", "Booking is not active."); return;
                }
                if (!StyleUtil.showConfirm("Checkout", "Checkout Booking #" + bid + "?")) return;
                store.checkoutBooking(bid);

                // Issue loyalty voucher if eligible
                Customer c = store.getCustomerById(b.getCustomerId());
                GiftVoucher v = store.maybeIssueVoucher(c);
                String msg = "Checkout successful!";
                if (v != null) msg += "\n\n🎁 Loyalty Voucher Issued!\nCode: " + v.getVoucherCode()
                        + "\n" + v.getDiscountPercent() + "% off | Valid till " + v.getExpiryDate();
                StyleUtil.showInfo("Checked Out", msg);
                refreshTable();
                refreshRoomCombo(roomCombo);
                checkoutIdField.clear();
            } catch (NumberFormatException ex) {
                StyleUtil.showError("Invalid", "Enter a numeric booking ID.");
            }
        });
        checkoutCard.getChildren().add(new HBox(12, checkoutIdField, checkoutBtn));

        // ── Table ─────────────────────────────────────────────────────────
        VBox tableCard = StyleUtil.createCard("📋  All Bookings");
        table = StyleUtil.styledTable();
        table.setPrefHeight(380);
        bookingList = FXCollections.observableArrayList(store.getBookings());
        table.setItems(bookingList);

        TableColumn<Booking, Number> idCol   = tc("Booking #", 80);
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBookingId()));
        TableColumn<Booking, Number> rCol    = tc("Room", 70);
        rCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRoomNumber()));
        TableColumn<Booking, String> guestCol= tc("Guest", 150);
        guestCol.setCellValueFactory(c -> {
            Customer cu = store.getCustomerById(c.getValue().getCustomerId());
            return new javafx.beans.property.SimpleStringProperty(cu != null ? cu.getName() : "—");
        });
        TableColumn<Booking, String> ciCol   = tc("Check-In", 110);
        ciCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckIn().toString()));
        TableColumn<Booking, String> coCol   = tc("Check-Out", 110);
        coCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckOut().toString()));
        TableColumn<Booking, Number> daysCol = tc("Days", 60);
        daysCol.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getNumberOfDays()));
        TableColumn<Booking, Number> amtCol  = tc("Total ₹", 100);
        amtCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotalAmount()));
        TableColumn<Booking, Number> discCol = tc("Disc%", 60);
        discCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getDiscountApplied()));
        TableColumn<Booking, String> statCol = tc("Status", 110);
        statCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().toString()));
        statCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle("ACTIVE".equals(item)
                    ? "-fx-text-fill: " + StyleUtil.SUCCESS + "; -fx-font-weight: bold;"
                    : "-fx-text-fill: " + StyleUtil.TEXT_DIM + ";");
            }
        });

        table.getColumns().addAll(idCol, rCol, guestCol, ciCol, coCol, daysCol, amtCol, discCol, statCol);
        for (TableColumn<?, ?> col : table.getColumns()) {
    col.setStyle("-fx-text-fill: white;");
}
        tableCard.getChildren().add(table);
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

        body.getChildren().addAll(formCard, checkoutCard, tableCard);
        root.getChildren().add(body);
        return root;
    }

    private void handleBooking(TextField custIdField, ComboBox<String> roomCombo,
            DatePicker checkInPicker, DatePicker checkOutPicker,
            TextField guestsField, TextField voucherField, Label discountPreview) {
        try {
            int cid = Integer.parseInt(custIdField.getText().trim());
            Customer c = store.getCustomerById(cid);
            if (c == null) { StyleUtil.showError("Not Found", "Customer ID " + cid + " not found."); return; }

            String roomSel = roomCombo.getValue();
            if (roomSel == null) { StyleUtil.showError("No Room", "Please select a room."); return; }
            int roomNum = Integer.parseInt(roomSel.split(" ")[1]);
            Room room   = store.getRoomByNumber(roomNum);
            if (room == null || !room.isAvailable()) {
                StyleUtil.showError("Unavailable", "Room " + roomNum + " is not available."); return;
            }

            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (!co.isAfter(ci)) { StyleUtil.showError("Date Error", "Check-out must be after check-in."); return; }

            int guests = Integer.parseInt(guestsField.getText().trim());

            DataStore.Pair<Double, String> disc = store.calculateDiscount(
                    c, c.getSpecialOccasion(), guests, voucherField.getText().trim());
            double discPct = disc.getFirst();
            String note    = disc.getSecond();

            int bid = store.nextBookingId();
            Booking booking = new Booking(bid, cid, roomNum, ci, co,
                    guests, room.getPricePerDay(), discPct, note);
            store.addBooking(booking);

            refreshTable();
            refreshRoomCombo(roomCombo);
            custIdField.clear(); guestsField.clear(); voucherField.clear();
            discountPreview.setText("Discount: —");

            StyleUtil.showInfo("Booking Confirmed! 🎉",
                "Booking #" + bid + " created!\n"
              + "Guest: " + c.getName() + "\n"
              + "Room: " + roomNum + " (" + room.getRoomType() + ")\n"
              + "Check-In: " + ci + " → " + co + "\n"
              + "Discount: " + discPct + "% | " + note + "\n"
              + "Total: ₹" + String.format("%.2f", booking.getTotalAmount()));
        } catch (NumberFormatException ex) {
            StyleUtil.showError("Invalid Input", "Customer ID and Guests must be numbers.");
        }
    }

    private void refreshTable() {
        bookingList.setAll(store.getBookings());
    }

    private void refreshRoomCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        for (Room r : store.getAvailableRooms())
            combo.getItems().add("Room " + r.getRoomNumber() + " - " + r.getRoomType()
                    + " (₹" + (int)r.getPricePerDay() + ")");
    }

    private void styleDP(DatePicker dp) {
        dp.setStyle("-fx-background-color: #0D2137; -fx-text-fill: " + StyleUtil.TEXT_MAIN + ";");
    }

    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> tc(String title, double w) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setPrefWidth(w);
        return c;
    }
}