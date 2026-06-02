package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

/**
 * FoodOrderTab.java - Place room-service food orders
 * Demonstrates: Week 2 Enum (FoodCategory), Week 8 HashMap
 */
public class FoodOrderTab {

    private final DataStore store = DataStore.getInstance();

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(
            StyleUtil.pageHeader("🍽", "Food & Room Service", "Order food for your room")
        );

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // ── Order Form ───────────────────────────────────────────────────
        VBox formCard = StyleUtil.createCard("🛎  Place Food Order");

        TextField bookingIdField = StyleUtil.styledField("Enter active Booking ID");

        // Food item selection
        ComboBox<String> catFilter = StyleUtil.styledCombo();
        catFilter.getItems().add("ALL");
        for (FoodItem.FoodCategory cat : FoodItem.FoodCategory.values())
            catFilter.getItems().add(cat.name());
        catFilter.setValue("ALL");
        catFilter.setPrefWidth(160);

        ComboBox<FoodItem> itemCombo = StyleUtil.styledCombo();
        itemCombo.setPrefWidth(260);
        itemCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(FoodItem f) {
                return f == null ? "" : f.getName() + " - ₹" + f.getPrice();
            }
            @Override public FoodItem fromString(String s) { return null; }
        });
        itemCombo.getItems().addAll(store.getMenuItems());

        catFilter.setOnAction(e -> {
            String sel = catFilter.getValue();
            itemCombo.getItems().clear();
            for (FoodItem f : store.getMenuItems()) {
                if ("ALL".equals(sel) || f.getCategory().name().equals(sel))
                    itemCombo.getItems().add(f);
            }
        });

        TextField qtyField = StyleUtil.styledField("Qty");
        qtyField.setPrefWidth(80);

        // Cart display
        TextArea cartArea = StyleUtil.styledTextArea();
        cartArea.setPrefHeight(150);
        cartArea.setPromptText("Cart is empty...");

        // Internal order builder
        FoodOrder[] pendingOrder = {null};

        Button initOrderBtn = StyleUtil.infoButton("🆕 Start Order");
        initOrderBtn.setOnAction(e -> {
            try {
                int bid = Integer.parseInt(bookingIdField.getText().trim());
                Booking b = store.getBookingById(bid);
                if (b == null || b.getStatus() != Booking.BookingStatus.ACTIVE) {
                    StyleUtil.showError("Invalid", "Active booking not found for ID " + bid);
                    return;
                }
                pendingOrder[0] = new FoodOrder(store.nextOrderId(), bid, b.getRoomNumber());
                cartArea.setText("Order started for Room " + b.getRoomNumber() + "\n─────────────────\n");
            } catch (NumberFormatException ex) {
                StyleUtil.showError("Invalid", "Enter numeric booking ID.");
            }
        });

        Button addItemBtn = StyleUtil.infoButton("➕ Add to Cart");
        addItemBtn.setOnAction(e -> {
            if (pendingOrder[0] == null) {
                StyleUtil.showError("No Order", "Click 'Start Order' first.");
                return;
            }
            FoodItem item = itemCombo.getValue();
            if (item == null) { StyleUtil.showError("No Item", "Select a food item."); return; }
            int qty;
            try { qty = Integer.parseInt(qtyField.getText().trim()); }
            catch (Exception ex) { StyleUtil.showError("Invalid Qty", "Enter numeric quantity."); return; }
            pendingOrder[0].addItem(item, qty);
            StringBuilder sb = new StringBuilder("Order for Room " + pendingOrder[0].getRoomNumber() + "\n");
            sb.append("─────────────────────────────────\n");
            for (Map.Entry<FoodItem, Integer> entry : pendingOrder[0].getItems().entrySet()) {
                sb.append(String.format("  %-28s x%d  ₹%.2f%n",
                        entry.getKey().getName(), entry.getValue(),
                        entry.getKey().getPrice() * entry.getValue()));
            }
            sb.append("─────────────────────────────────\n");
            sb.append(String.format("  TOTAL: ₹%.2f%n", pendingOrder[0].getTotalCost()));
            cartArea.setText(sb.toString());
            qtyField.clear();
        });

        Button placeOrderBtn = StyleUtil.primaryButton("✅ Place Order");
        placeOrderBtn.setOnAction(e -> {
            if (pendingOrder[0] == null || pendingOrder[0].getItems().isEmpty()) {
                StyleUtil.showError("Empty Cart", "Add items before placing the order.");
                return;
            }
            store.addFoodOrder(pendingOrder[0]);
            StyleUtil.showInfo("Order Placed! 🍽",
                "Order #" + pendingOrder[0].getOrderId()
              + " placed for Room " + pendingOrder[0].getRoomNumber()
              + "\nTotal: ₹" + String.format("%.2f", pendingOrder[0].getTotalCost()));
            pendingOrder[0] = null;
            cartArea.clear();
            bookingIdField.clear();
        });

        HBox filterRow = new HBox(12,
            StyleUtil.fieldLabel("Category:"), catFilter,
            StyleUtil.fieldLabel("Item:"), itemCombo,
            StyleUtil.fieldLabel("Qty:"), qtyField, addItemBtn);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        HBox actionRow = new HBox(12, bookingIdField, initOrderBtn, placeOrderBtn);
        formCard.getChildren().addAll(actionRow, filterRow, cartArea);

        // ── Food Menu Card ───────────────────────────────────────────────
        VBox menuCard = StyleUtil.createCard("📖  Food Menu");
        TableView<FoodItem> menuTable = StyleUtil.styledTable();
        menuTable.setPrefHeight(300);
        menuTable.setItems(FXCollections.observableArrayList(store.getMenuItems()));

        TableColumn<FoodItem, Number> idCol   = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getItemId()));
        idCol.setPrefWidth(50);
        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        TableColumn<FoodItem, String> catCol  = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory().name()));
        TableColumn<FoodItem, Number> priceCol= new TableColumn<>("Price ₹");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()));
        priceCol.setPrefWidth(90);
        TableColumn<FoodItem, String> vegCol  = new TableColumn<>("Type");
        vegCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isVegetarian() ? "🌿 Veg" : "🍗 Non-Veg"));
        vegCol.setPrefWidth(90);

        menuTable.getColumns().addAll(idCol, nameCol, catCol, priceCol, vegCol);
        for (TableColumn<?, ?> col : menuTable.getColumns()) {
    col.setStyle("-fx-text-fill: white;");
}
        menuCard.getChildren().add(menuTable);
        Platform.runLater(() -> {

            // Header background (premium navy)
            menuTable.lookupAll(".column-header").forEach(node ->
                node.setStyle(
                    "-fx-background-color: linear-gradient(to right, #0f2027, #203a43, #2c5364);" +
                    "-fx-border-color: rgba(212,175,55,0.3);" +
                    "-fx-border-width: 0 1 1 0;"
                )
            );

            // Header text (GOLD)
            menuTable.lookupAll(".column-header .label").forEach(node ->
                node.setStyle(
                    "-fx-text-fill: #d4af37;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;"
                )
            );
        });

        body.getChildren().addAll(formCard, menuCard);
        root.getChildren().add(body);
        return root;
    }
}