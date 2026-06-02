package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.model.Customer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class CustomersTab {

    private final DataStore store = DataStore.getInstance();
    private ObservableList<Customer> custList;
    private TableView<Customer> table;

    public VBox getContent() {

        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);

        root.getChildren().add(
            StyleUtil.pageHeader("👤", "Customer Management", "Register and manage hotel guests")
        );

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        // ── FORM ─────────────────────────
        VBox formCard = StyleUtil.createCard("📝  Register New Customer");

        TextField nameField = StyleUtil.styledField("Full name");
        TextField phoneField = StyleUtil.styledField("+91-XXXXXXXXXX");
        TextField emailField = StyleUtil.styledField("email@example.com");

        ComboBox<String> occCombo = StyleUtil.styledCombo();
        occCombo.getItems().addAll("None", "Birthday", "Anniversary", "Wedding");
        occCombo.setValue("None");
        occCombo.setPrefWidth(200);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);

        grid.add(StyleUtil.fieldLabel("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(StyleUtil.fieldLabel("Contact No.:"), 2, 0);
        grid.add(phoneField, 3, 0);

        grid.add(StyleUtil.fieldLabel("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(StyleUtil.fieldLabel("Occasion:"), 2, 1);
        grid.add(occCombo, 3, 1);

        Button registerBtn = StyleUtil.primaryButton("📝 Register Customer");

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                StyleUtil.showError("Missing Info", "Name and contact are required.");
                return;
            }

            Customer c = new Customer(
                store.nextCustomerId(), name, phone, email, occCombo.getValue()
            );

            store.addCustomer(c);
            refreshTable();

            nameField.clear();
            phoneField.clear();
            emailField.clear();
            occCombo.setValue("None");

            StyleUtil.showInfo("Registered",
                "Customer '" + name + "' registered! ID: " + c.getCustomerId());
        });

        formCard.getChildren().addAll(grid, registerBtn);

        // ── SEARCH ─────────────────────────
        VBox searchCard = StyleUtil.createCard("🔍  Search Customer");

        TextField searchField = StyleUtil.styledField("Enter customer name or ID");
        Button searchBtn = StyleUtil.infoButton("Search");

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim().toLowerCase();
            custList.clear();

            for (Customer c : store.getCustomers()) {
                if (c.getName().toLowerCase().contains(q) ||
                    String.valueOf(c.getCustomerId()).equals(q)) {
                    custList.add(c);
                }
            }
        });

        Button showAllBtn = StyleUtil.successButton("Show All");
        showAllBtn.setOnAction(e -> refreshTable());

        searchCard.getChildren().add(new HBox(12, searchField, searchBtn, showAllBtn));

        // ── TABLE ─────────────────────────
        VBox tableCard = StyleUtil.createCard("👥  Registered Customers");

        table = StyleUtil.styledTable();
        table.setPrefHeight(380);

        custList = FXCollections.observableArrayList(store.getCustomers());
        table.setItems(custList);

        // Columns
        TableColumn<Customer, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCustomerId()));

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Customer, String> phCol = new TableColumn<>("Contact");
        phCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getContactNumber()));

        TableColumn<Customer, String> emCol = new TableColumn<>("Email");
        emCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<Customer, String> occCol = new TableColumn<>("Occasion");
        occCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialOccasion()));

        TableColumn<Customer, Number> bkCol = new TableColumn<>("Bookings");
        bkCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalBookings()));

        table.getColumns().addAll(idCol, nameCol, phCol, emCol, occCol, bkCol);
        for (TableColumn<?, ?> col : table.getColumns()) {
        col.setStyle("-fx-text-fill: white;");
       }

        tableCard.getChildren().add(table);

        // ✅ FINAL FIX: HEADER STYLING
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
        

        body.getChildren().addAll(formCard, searchCard, tableCard);
        root.getChildren().add(body);

        return root;
    }

    private void refreshTable() {
        custList.setAll(store.getCustomers());
    }
}