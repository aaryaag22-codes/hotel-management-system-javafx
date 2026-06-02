package com.hotel.ui;

import com.hotel.data.DataStore;
import com.hotel.service.PdfService;
import com.hotel.service.PrintService;
import com.hotel.service.ReminderService;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.io.File;

/**
 * PrintTab.java - Print receipts as HTML (opens in browser → Save as PDF),
 * brochure, food menu, customer details, SMS reminders.
 *
 * Files are saved to: ~/Documents/GrandVista/Receipts/
 */
public class PrintTab {

    private final DataStore    store        = DataStore.getInstance();
    private final PrintService printService = new PrintService();
    private final PdfService   pdfService   = new PdfService();
    private final ReminderService reminder  = new ReminderService(msg -> {});

    public VBox getContent() {
        VBox root = new VBox(0);
        StyleUtil.styleRoot(root);
        root.getChildren().add(StyleUtil.pageHeader("🖨", "Print & PDF Center",
                "Generate receipts, menus & reports — saved to your Documents folder"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        StyleUtil.styleRoot(body);

        TextArea output = StyleUtil.styledTextArea();
        output.setPrefHeight(380);

        // ── PDF / Print Options ──────────────────────────────────────────
        VBox pdfCard = StyleUtil.createCard("📄  PDF Receipts  (saved to your Mac)");

        // Info label showing save path
        Label pathLabel = new Label("📁  Save location:  " + pdfService.getReceiptsFolder());
        pathLabel.setStyle("-fx-text-fill: " + StyleUtil.GOLD
                + "; -fx-font-size: 12px; -fx-font-style: italic;");

        TextField bookingIdField = StyleUtil.styledField("Enter Booking ID");
        bookingIdField.setPrefWidth(200);

        Button pdfReceiptBtn = StyleUtil.primaryButton("🧾  Generate Receipt PDF");
        pdfReceiptBtn.setOnAction(e -> {
            try {
                int bid = Integer.parseInt(bookingIdField.getText().trim());
                String path = pdfService.generateReceiptHtml(bid);
                output.setText("✅ Receipt saved to:\n" + path
                    + "\n\nTo convert to PDF:\n"
                    + "1. Open the file in Safari or Chrome\n"
                    + "2. Press Cmd + P\n"
                    + "3. Choose 'Save as PDF' in the bottom-left\n"
                    + "4. Choose your save location and click Save");
                // Open in browser automatically
                try {
                    Desktop.getDesktop().open(new File(path));
                } catch (Exception ex) {
                    output.appendText("\n\nFile opened. If browser didn't open, go to:\n" + path);
                }
            } catch (NumberFormatException ex) {
                StyleUtil.showError("Invalid", "Enter a numeric booking ID.");
            } catch (Exception ex) {
                StyleUtil.showError("Error", ex.getMessage());
            }
        });

        Button foodMenuPdfBtn = StyleUtil.infoButton("🍽  Generate Food Menu PDF");
        foodMenuPdfBtn.setOnAction(e -> {
            try {
                String path = pdfService.generateFoodMenuHtml();
                output.setText("✅ Food Menu saved to:\n" + path);
                try { Desktop.getDesktop().open(new File(path)); } catch (Exception ignored) {}
            } catch (Exception ex) {
                StyleUtil.showError("Error", ex.getMessage());
            }
        });

        Button openFolderBtn = StyleUtil.successButton("📂  Open Receipts Folder");
        openFolderBtn.setOnAction(e -> {
            try {
                Desktop.getDesktop().open(new File(pdfService.getReceiptsFolder()));
            } catch (Exception ex) {
                StyleUtil.showError("Error", "Could not open folder: " + ex.getMessage());
            }
        });

        HBox pdfRow = new HBox(12, bookingIdField, pdfReceiptBtn, foodMenuPdfBtn, openFolderBtn);
        pdfRow.setAlignment(Pos.CENTER_LEFT);
        pdfCard.getChildren().addAll(pathLabel, pdfRow);

        // ── Text Print Options ───────────────────────────────────────────
        VBox printCard = StyleUtil.createCard("🖨  Other Print Options");

        TextField custIdField = StyleUtil.styledField("Customer ID");
        custIdField.setPrefWidth(180);
        Button custBtn = StyleUtil.primaryButton("👤  Customer Details");
        custBtn.setOnAction(e -> {
            try {
                int cid = Integer.parseInt(custIdField.getText().trim());
                output.setText(printService.printCustomerDetails(cid));
            } catch (Exception ex) { StyleUtil.showError("Error", "Enter valid Customer ID."); }
        });

        Button brochureBtn = StyleUtil.infoButton("📄  Hotel Brochure");
        brochureBtn.setOnAction(e -> output.setText(printService.printBrochure()));

        Button menuBtn = StyleUtil.infoButton("🍽  Food Menu (Text)");
        menuBtn.setOnAction(e -> output.setText(printService.printFoodMenu()));

        Button remindBtn = StyleUtil.successButton("📱  SMS Reminders");
        remindBtn.setOnAction(e -> output.setText(reminder.checkNow()));

        Button allBtn = StyleUtil.infoButton("📋  All Booking Summaries");
        allBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            store.getBookings().forEach(b -> sb.append(printService.generateReceipt(b.getBookingId())).append("\n\n"));
            output.setText(sb.isEmpty() ? "No bookings yet." : sb.toString());
        });

        HBox printRow1 = new HBox(12, custIdField, custBtn, brochureBtn);
        HBox printRow2 = new HBox(12, menuBtn, remindBtn, allBtn);
        printRow1.setAlignment(Pos.CENTER_LEFT);
        printRow2.setAlignment(Pos.CENTER_LEFT);
        printCard.getChildren().addAll(printRow1, printRow2);

        // ── Output ───────────────────────────────────────────────────────
        VBox outCard = StyleUtil.createCard("📄  Preview / Output");
        outCard.getChildren().add(output);

        body.getChildren().addAll(pdfCard, printCard, outCard);
        root.getChildren().add(body);
        return root;
    }
}