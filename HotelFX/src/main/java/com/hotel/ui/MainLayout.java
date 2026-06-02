package com.hotel.ui;

import com.hotel.service.ReminderService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * MainLayout.java - Rebuilt with proper tab labels, bigger fonts, better nav bar
 */
public class MainLayout {

    private final BorderPane root = new BorderPane();
    private ReminderService reminderService;

    public MainLayout() {
        buildUI();
        startReminderThread();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: " + StyleUtil.DARK_BG + ";");

        // Top Header Banner
        HBox banner = new HBox(16);
        banner.setPadding(new Insets(16, 28, 16, 28));
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, #080818, " + StyleUtil.PANEL_BG + ", #080818);"
          + "-fx-border-color: transparent transparent " + StyleUtil.GOLD + " transparent;"
          + "-fx-border-width: 0 0 2 0;"
        );

        Label iconLbl = new Label("🏨");
        iconLbl.setStyle("-fx-font-size: 32px;");

        VBox titleBox = new VBox(2);
        Label hotelName = new Label("GRAND VISTA HOTEL MANAGEMENT SYSTEM");
        hotelName.setStyle(
            "-fx-text-fill: " + StyleUtil.GOLD + ";"
          + "-fx-font-size: 20px;"
          + "-fx-font-weight: bold;"
          + "-fx-font-family: 'Georgia';"
        );
        Label tagline = new Label("Where Luxury Meets Warm Hospitality");
        tagline.setStyle("-fx-text-fill: " + StyleUtil.TEXT_DIM + "; -fx-font-size: 11px; -fx-font-style: italic;");
        titleBox.getChildren().addAll(hotelName, tagline);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        Label dateLabel = new Label(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setStyle("-fx-text-fill: " + StyleUtil.GOLD + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        dateBox.getChildren().add(dateLabel);
        banner.getChildren().addAll(iconLbl, titleBox, spacer, dateBox);
        root.setTop(banner);

        // Tab Pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setTabMinWidth(140);
        tabPane.setStyle("-fx-background-color: " + StyleUtil.DARK_BG + "; -fx-tab-min-height: 46px;");

        Tab[] tabs = {
            makeTab("🏠  Dashboard",   new DashboardTab().getContent()),
            makeTab("🛏  Rooms",       new RoomsTab().getContent()),
            makeTab("👤  Customers",   new CustomersTab().getContent()),
            makeTab("📋  Bookings",    new BookingsTab().getContent()),
            makeTab("🍽  Food Orders", new FoodOrderTab().getContent()),
            makeTab("💰  Billing",     new BillingTab().getContent()),
            makeTab("🖨  Print & PDF", new PrintTab().getContent()),
            makeTab("🎁  Loyalty",     new LoyaltyTab().getContent()),
        };
        for (Tab t : tabs) tabPane.getTabs().add(t);
        root.setCenter(tabPane);
    }

    private Tab makeTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab();
        Label lbl = new Label(title);
        lbl.setStyle(
            "-fx-text-fill: " + StyleUtil.TEXT_MAIN + ";"
          + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 4 6;"
        );
        tab.setGraphic(lbl);
        tab.selectedProperty().addListener((obs, o, selected) ->
            lbl.setStyle(
                "-fx-text-fill: " + (selected ? StyleUtil.GOLD : StyleUtil.TEXT_MAIN) + ";"
              + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 4 6;"
            )
        );
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + StyleUtil.DARK_BG + "; -fx-background: " + StyleUtil.DARK_BG + ";");
        tab.setContent(scroll);
        return tab;
    }

    private void startReminderThread() {
        reminderService = new ReminderService(msg -> Platform.runLater(() -> System.out.println("[Reminder] " + msg)));
        Thread t = new Thread(reminderService);
        t.setDaemon(true);
        t.setName("ReminderThread");
        t.start();
    }

    public BorderPane getRoot() { return root; }
}