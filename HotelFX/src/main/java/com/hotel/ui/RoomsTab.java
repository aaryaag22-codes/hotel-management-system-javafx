package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.Room;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * RoomsTab.java - Fixed Show Available / Show All + improved table styling
 */
public class RoomsTab {

    private final DataStore store = DataStore.getInstance();
    private ObservableList<Room> roomList;
    private TableView<Room> table;

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(StyleUtil.pageHeader("🛏", "Room Management", "Add and manage hotel rooms"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // Add Room Form
        VBox formCard = StyleUtil.createCard("➕  Add New Room");
        TextField roomNumField = StyleUtil.styledField("e.g. 103");
        ComboBox<String> typeCombo = StyleUtil.styledCombo();
        typeCombo.getItems().addAll("Single", "Double", "Deluxe", "Suite");
        typeCombo.setPromptText("Select type");
        typeCombo.setPrefWidth(200);
        TextField priceField = StyleUtil.styledField("e.g. 2500");
        TextField amenField  = StyleUtil.styledField("e.g. WiFi, AC, TV");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.add(StyleUtil.fieldLabel("Room Number:"), 0, 0); grid.add(roomNumField, 1, 0);
        grid.add(StyleUtil.fieldLabel("Room Type:"),   2, 0); grid.add(typeCombo,    3, 0);
        grid.add(StyleUtil.fieldLabel("Price/Night:"), 0, 1); grid.add(priceField,   1, 1);
        grid.add(StyleUtil.fieldLabel("Amenities:"),   2, 1); grid.add(amenField,    3, 1);

        Button addBtn     = StyleUtil.primaryButton("➕  Add Room");
        Button filterBtn  = StyleUtil.infoButton("🔍  Available Only");
        Button showAllBtn = StyleUtil.successButton("📋  Show All Rooms");

        addBtn.setOnAction(e -> {
            try {
                int num     = Integer.parseInt(roomNumField.getText().trim());
                String type = typeCombo.getValue();
                double price= Double.parseDouble(priceField.getText().trim());
                String amen = amenField.getText().trim();
                if (type == null || amen.isEmpty()) throw new IllegalArgumentException("Fill all fields");
                if (store.getRoomByNumber(num) != null) {
                    StyleUtil.showError("Duplicate", "Room " + num + " already exists!"); return;
                }
                store.addRoom(new Room(num, type, price, amen));
                refreshTable();
                roomNumField.clear(); priceField.clear(); amenField.clear();
                typeCombo.getSelectionModel().clearSelection();
                StyleUtil.showInfo("Success", "Room " + num + " added successfully!");
            } catch (NumberFormatException ex) {
                StyleUtil.showError("Invalid Input", "Room number and price must be numbers.");
            } catch (IllegalArgumentException ex) {
                StyleUtil.showError("Incomplete", ex.getMessage());
            }
        });

        // FIX: these now correctly update roomList which is bound to the table
        filterBtn.setOnAction(e -> {
            roomList.setAll(store.getAvailableRooms());
        });
        showAllBtn.setOnAction(e -> refreshTable());

        HBox btnRow = new HBox(12, addBtn, filterBtn, showAllBtn);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        formCard.getChildren().addAll(grid, btnRow);

        // Room Table - much better styling
        VBox tableCard = StyleUtil.createCard("📋  All Rooms");
        table = buildRoomTable();
        tableCard.getChildren().add(table);

        body.getChildren().addAll(formCard, tableCard);
        root.getChildren().add(body);
        return root;
    }

    private TableView<Room> buildRoomTable() {
        TableView<Room> tv = new TableView<>();
        tv.setPrefHeight(420);
        tv.setStyle(
            "-fx-background-color: #0D2137;"
          + "-fx-border-color: " + StyleUtil.GOLD + "44;"
          + "-fx-border-radius: 8;"
          + "-fx-font-size: 13px;"
        );
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        roomList = FXCollections.observableArrayList(store.getRooms());
        tv.setItems(roomList);

        TableColumn<Room, Integer> numCol = new TableColumn<>("Room No.");
        numCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRoomNumber()).asObject());

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRoomType()));

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price / Night");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPricePerDay()).asObject());
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText("Rs. " + String.format("%.0f", item));
                setStyle("-fx-text-fill: " + StyleUtil.GOLD + "; -fx-font-weight: bold;");
            }
        });

        TableColumn<Room, String> amenCol = new TableColumn<>("Amenities");
        amenCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAmenities()));

        TableColumn<Room, String> statCol = new TableColumn<>("Status");
        statCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isAvailable() ? "Available" : "Occupied"));
        statCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = StyleUtil.statusBadge(item, "Available".equals(item));
                setGraphic(badge);
                setText(null);
            }
        });

        styleColumn(numCol); styleColumn(typeCol); styleColumn(amenCol);
        tv.getColumns().addAll(numCol, typeCol, priceCol, amenCol, statCol);
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

        // Alternating row colours
        tv.setRowFactory(t -> new TableRow<>() {
            @Override protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else if (getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #0D2137;");
                } else {
                    setStyle("-fx-background-color: #0A1A2E;");
                }
            }
        });

        return tv;
    }

    private <S,T> void styleColumn(TableColumn<S,T> col) {
        col.setStyle("-fx-alignment: CENTER; -fx-text-fill: " + StyleUtil.TEXT_MAIN + ";");
    }

    private void refreshTable() {
        roomList.setAll(store.getRooms());
    }
}