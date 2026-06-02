package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.*;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * DashboardTab.java - Overview stats + recent bookings + Contact Us
 */
public class DashboardTab {

    private final DataStore store = DataStore.getInstance();

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(StyleUtil.pageHeader("🏠", "Dashboard", "Hotel overview at a glance"));

        VBox body = new VBox(22);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // ── Stat cards ───────────────────────────────────────────────────
        long totalRooms    = store.getRooms().size();
        long availRooms    = store.getAvailableRooms().size();
        long occupiedRooms = totalRooms - availRooms;
        long totalCust     = store.getCustomers().size();
        long activeBook    = store.getBookings().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.ACTIVE).count();
        double totalRevenue = store.getBookings().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_OUT)
                .mapToDouble(Booking::getTotalAmount).sum();

        HBox stats = new HBox(14);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
            statCard("🛏", "Total Rooms",     String.valueOf(totalRooms),    StyleUtil.INFO),
            statCard("🟢", "Available",        String.valueOf(availRooms),    StyleUtil.SUCCESS),
            statCard("🔴", "Occupied",         String.valueOf(occupiedRooms), StyleUtil.DANGER),
            statCard("👤", "Guests Registered",String.valueOf(totalCust),     StyleUtil.WARNING),
            statCard("📋", "Active Bookings",  String.valueOf(activeBook),    StyleUtil.GOLD),
            statCard("💰", "Total Revenue",    "Rs." + String.format("%.0f", totalRevenue), "#8E44AD")
        );

        // ── Room status grid ─────────────────────────────────────────────
        VBox roomCard = StyleUtil.createCard("🗺  Room Status Overview");
        FlowPane grid = new FlowPane(12, 12);
        grid.setPadding(new Insets(8, 0, 0, 0));
        for (Room r : store.getRooms()) {
            VBox box = new VBox(5);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(12, 18, 12, 18));
            boolean av = r.isAvailable();
            box.setStyle(
                "-fx-background-color: " + (av ? StyleUtil.SUCCESS + "18" : StyleUtil.DANGER + "18") + ";"
              + "-fx-border-color: " + (av ? StyleUtil.SUCCESS : StyleUtil.DANGER) + ";"
              + "-fx-border-radius: 10; -fx-background-radius: 10;"
            );
            Label num    = new Label("Room " + r.getRoomNumber());
            num.setStyle("-fx-text-fill: " + StyleUtil.TEXT_MAIN + "; -fx-font-weight: bold; -fx-font-size: 13px;");
            Label type   = new Label(r.getRoomType());
            type.setStyle("-fx-text-fill: " + StyleUtil.TEXT_DIM + "; -fx-font-size: 11px;");
            Label status = new Label(av ? "🟢 Free" : "🔴  Occupied");
            status.setStyle("-fx-text-fill: " + (av ? StyleUtil.SUCCESS : StyleUtil.DANGER) + "; -fx-font-size: 12px; -fx-font-weight: bold;");
            Label price  = new Label("Rs." + (int)r.getPricePerDay() + "/night");
            price.setStyle("-fx-text-fill: " + StyleUtil.GOLD + "; -fx-font-size: 11px;");
            box.getChildren().addAll(num, type, status, price);
            grid.getChildren().add(box);
        }
        roomCard.getChildren().add(grid);

        // ── Recent bookings ──────────────────────────────────────────────
        VBox recentCard = StyleUtil.createCard("📋  Recent Bookings");
        TableView<Booking> table = StyleUtil.styledTable();
        table.setPrefHeight(220);

        TableColumn<Booking, Number> idCol   = tc("Booking #", 90);
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBookingId()));
        TableColumn<Booking, Number> roomCol = tc("Room", 70);
        roomCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRoomNumber()));
        TableColumn<Booking, String> guestCol= tc("Guest", 160);
        guestCol.setCellValueFactory(c -> {
            Customer cu = store.getCustomerById(c.getValue().getCustomerId());
            return new javafx.beans.property.SimpleStringProperty(cu != null ? cu.getName() : "—");
        });
        TableColumn<Booking, String> ciCol   = tc("Check-In", 110);
        ciCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckIn().toString()));
        TableColumn<Booking, String> statCol = tc("Status", 120);
        statCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().toString()));
        statCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setText(null);
                if (!empty && item != null) setGraphic(StyleUtil.statusBadge(item, "ACTIVE".equals(item)));
            }
        });
        TableColumn<Booking, Number> amtCol  = tc("Amount (Rs.)", 120);
        amtCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotalAmount()));
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText("Rs. " + String.format("%.2f", item.doubleValue()));
                setStyle("-fx-text-fill: " + StyleUtil.GOLD + "; -fx-font-weight: bold;");
            }
        });

        table.getColumns().addAll(idCol, roomCol, guestCol, ciCol, statCol, amtCol);
        for (TableColumn<?, ?> col : table.getColumns()) {
        col.setStyle("-fx-text-fill: white;");
       }
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
        java.util.List<Booking> recent = new java.util.ArrayList<>(store.getBookings());
        java.util.Collections.reverse(recent);
        table.getItems().addAll(recent.subList(0, Math.min(10, recent.size())));
        recentCard.getChildren().add(table);

        // ── Contact Us section ───────────────────────────────────────────
        VBox contactCard = StyleUtil.createCard("📞  Contact Us");
        contactCard.setStyle(contactCard.getStyle()
            + "-fx-border-color: " + StyleUtil.GOLD + "88;");

        HBox contactRow = new HBox(40);
        contactRow.setPadding(new Insets(10, 0, 4, 0));
        contactRow.setAlignment(Pos.CENTER_LEFT);

        contactRow.getChildren().addAll(
            contactItem("📍", "Address", "123 Palace Road, Udupi - 576101\nKarnataka, India"),
            contactItem("📞", "Phone", "+91-820-1234567\n+91-820-7654321"),
            contactItem("📧", "Email", "grandvista@hotel.com\nsupport@grandvista.com"),
            contactItem("🕐", "Reception Hours", "24 × 7 Always Open\nCheck-in: 12 PM | Out: 11 AM"),
            contactItem("🌐", "Website", "www.grandvista.com\n@GrandVistaHotel")
        );
        contactCard.getChildren().add(contactRow);

        // Copyright footer
        Label footer = new Label("© 2026 Grand Vista Hotel Management System  |  Built with Java & JavaFX  |  All rights reserved.");
        footer.setStyle("-fx-text-fill: " + StyleUtil.TEXT_DIM + "; -fx-font-size: 10px; -fx-font-style: italic;");
        footer.setPadding(new Insets(10, 0, 0, 0));

        body.getChildren().addAll(stats, roomCard, recentCard, contactCard, footer);
        root.getChildren().add(body);
        return root;
    }

    private VBox contactItem(String icon, String label, String value) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(12, 20, 12, 20));
        box.setStyle(
            "-fx-background-color: " + StyleUtil.PANEL_BG + "55;"
          + "-fx-border-color: " + StyleUtil.GOLD + "33;"
          + "-fx-border-radius: 10; -fx-background-radius: 10;"
        );
        Label iconLbl = new Label(icon + "  " + label);
        iconLbl.setStyle("-fx-text-fill: " + StyleUtil.GOLD + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label valLbl  = new Label(value);
        valLbl.setStyle("-fx-text-fill: " + StyleUtil.TEXT_MAIN + "; -fx-font-size: 12px; -fx-line-spacing: 3;");
        box.getChildren().addAll(iconLbl, valLbl);
        return box;
    }

    private VBox statCard(String icon, String label, String value, String color) {
        VBox card = new VBox(7);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(
            "-fx-background-color: " + color + "18;"
          + "-fx-border-color: " + color + ";"
          + "-fx-border-radius: 12; -fx-background-radius: 12;"
          + "-fx-min-width: 120;"
          + "-fx-effect: dropshadow(gaussian, " + color + "44, 10, 0, 0, 3);"
        );
        Label iconL = new Label(icon);   iconL.setStyle("-fx-font-size: 26px;");
        Label valL  = new Label(value);  valL.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label lblL  = new Label(label);  lblL.setStyle("-fx-text-fill: " + StyleUtil.TEXT_DIM + "; -fx-font-size: 11px;");
        card.getChildren().addAll(iconL, valL, lblL);
        return card;
    }

    @SuppressWarnings("unchecked")
    private <S,T> TableColumn<S,T> tc(String title, double w) {
        TableColumn<S,T> c = new TableColumn<>(title);
        c.setPrefWidth(w);
        return c;
    }
}