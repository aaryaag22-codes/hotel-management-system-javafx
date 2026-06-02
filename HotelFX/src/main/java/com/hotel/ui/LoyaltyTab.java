package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoyaltyTab {
    private final DataStore store = DataStore.getInstance();

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(
            StyleUtil.pageHeader("🎁", "Loyalty & Discounts", "Vouchers, rewards & customer engagement")
        );
        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // Voucher list
        VBox vCard = StyleUtil.createCard("🎁  Gift Vouchers");
        TableView<GiftVoucher> vTable = StyleUtil.styledTable();
        vTable.setPrefHeight(220);
        vTable.getItems().setAll(store.getVouchers());

        TableColumn<GiftVoucher, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getVoucherCode()));
        TableColumn<GiftVoucher, Number> cidCol  = new TableColumn<>("Customer ID");
        cidCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCustomerId()));
        TableColumn<GiftVoucher, Number> pctCol  = new TableColumn<>("Discount %");
        pctCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getDiscountPercent()));
        TableColumn<GiftVoucher, String> expCol  = new TableColumn<>("Expiry");
        expCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getExpiryDate().toString()));
        TableColumn<GiftVoucher, String> usedCol = new TableColumn<>("Status");
        usedCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().isUsed() ? "USED" : c.getValue().isValid() ? "✅ VALID" : "EXPIRED"));

        vTable.getColumns().addAll(codeCol, cidCol, pctCol, expCol, usedCol);
        vCard.getChildren().add(vTable);

        // Discount rules info card
        VBox infoCard = StyleUtil.createCard("📊  Discount Rules");
        String rules =
            "🏅 Loyalty Discounts:\n"
          + "   • 3–4 bookings  →  5% off\n"
          + "   • 5–9 bookings  →  10% off  +  Gift Voucher\n"
          + "   • 10+ bookings  →  20% off  +  Premium Voucher\n\n"
          + "👨‍👩‍👧‍👦 Group Discounts:\n"
          + "   • 5–9 guests  →  8% off\n"
          + "   • 10+ guests →  15% off\n\n"
          + "🎉 Occasion Discounts:\n"
          + "   • Birthday   →  10% off\n"
          + "   • Anniversary→  8% off\n"
          + "   • Wedding    →  15% off\n\n"
          + "⚡ Max total discount:  40%\n\n"
          + "🎁 Gift Vouchers are auto-issued on checkout\n"
          + "   at your 5th, 10th booking milestones.";

        Label infoLabel = new Label(rules);
        infoLabel.setStyle("-fx-text-fill: " + StyleUtil.TEXT_MAIN + "; -fx-font-size: 13px; -fx-line-spacing: 2;");
        infoCard.getChildren().add(infoLabel);

        // Top loyal customers
        VBox loyalCard = StyleUtil.createCard("🏆  Most Loyal Customers");
        TableView<Customer> lTable = StyleUtil.styledTable();
        lTable.setPrefHeight(200);
        java.util.List<Customer> sorted = new java.util.ArrayList<>(store.getCustomers());
        sorted.sort((a, b) -> b.getTotalBookings() - a.getTotalBookings());
        lTable.getItems().addAll(sorted.subList(0, Math.min(5, sorted.size())));

        TableColumn<Customer, Number> lIdCol = new TableColumn<>("#");
        lIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCustomerId()));
        lIdCol.setPrefWidth(50);
        TableColumn<Customer, String> lNameCol = new TableColumn<>("Name");
        lNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        TableColumn<Customer, Number> lBkCol = new TableColumn<>("Bookings");
        lBkCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalBookings()));
        TableColumn<Customer, Number> lGstCol = new TableColumn<>("Total Guests");
        lGstCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalGuests()));

        lTable.getColumns().addAll(lIdCol, lNameCol, lBkCol, lGstCol);
        for (TableColumn<?, ?> col : lTable.getColumns()) {
    col.setStyle("-fx-text-fill: white;");
}
        loyalCard.getChildren().add(lTable);
        Platform.runLater(() -> {

            // Header background (premium navy)
            lTable.lookupAll(".column-header").forEach(node ->
                node.setStyle(
                    "-fx-background-color: linear-gradient(to right, #0f2027, #203a43, #2c5364);" +
                    "-fx-border-color: rgba(212,175,55,0.3);" +
                    "-fx-border-width: 0 1 1 0;"
                )
            );

            // Header text (GOLD)
            lTable.lookupAll(".column-header .label").forEach(node ->
                node.setStyle(
                    "-fx-text-fill: #d4af37;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;"
                )
            );
        });

        body.getChildren().addAll(vCard, infoCard, loyalCard);
        root.getChildren().add(body);
        return root;
    }
}