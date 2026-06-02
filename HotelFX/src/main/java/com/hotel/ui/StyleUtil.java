package com.hotel.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * StyleUtil.java - Centralised inline CSS. All colours and style helpers.
 */
public class StyleUtil {

    public static final String GOLD      = "#C9A84C";
    public static final String DARK_BG   = "#0E0E1A";
    public static final String CARD_BG   = "#14213D";
    public static final String PANEL_BG  = "#0F3460";
    public static final String ACCENT    = "#E94560";
    public static final String TEXT_MAIN = "#E8E8F0";
    public static final String TEXT_DIM  = "#8888A8";
    public static final String SUCCESS   = "#2ECC71";
    public static final String WARNING   = "#F39C12";
    public static final String DANGER    = "#E74C3C";
    public static final String INFO      = "#3498DB";
    public static final String ROW_EVEN  = "#111827";
    public static final String ROW_ODD   = "#0D1525";

    public static void styleRoot(Pane pane) {
        pane.setStyle("-fx-background-color: " + DARK_BG + ";");
    }

    // ── Card ──────────────────────────────────────────────────────────────
    public static VBox createCard(String title) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + CARD_BG + ";"
          + "-fx-background-radius: 12;"
          + "-fx-border-color: " + GOLD + "44;"
          + "-fx-border-radius: 12;"
          + "-fx-border-width: 1;"
          + "-fx-effect: dropshadow(gaussian, #00000088, 14, 0, 0, 5);"
        );
        if (title != null && !title.isEmpty()) {
            Label lbl = new Label(title);
            lbl.setStyle(
                "-fx-text-fill: " + GOLD + ";"
              + "-fx-font-size: 15px;"
              + "-fx-font-weight: bold;"
              + "-fx-font-family: 'Georgia';"
            );
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: " + GOLD + "55; -fx-opacity: 0.6;");
            card.getChildren().addAll(lbl, sep);
        }
        return card;
    }

    // ── Buttons ───────────────────────────────────────────────────────────
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        String base =
            "-fx-background-color: linear-gradient(to right, " + GOLD + ", #A07830);"
          + "-fx-text-fill: #1A1A2E; -fx-font-weight: bold; -fx-font-size: 13px;"
          + "-fx-padding: 10 24; -fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-effect: dropshadow(gaussian, " + GOLD + "66, 8, 0, 0, 2);";
        String hover =
            "-fx-background-color: linear-gradient(to right, #E8C060, " + GOLD + ");"
          + "-fx-text-fill: #0A0A18; -fx-font-weight: bold; -fx-font-size: 13px;"
          + "-fx-padding: 10 24; -fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-effect: dropshadow(gaussian, " + GOLD + "AA, 14, 0, 0, 3);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    public static Button dangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + DANGER + "; -fx-text-fill: white;"
          + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 24;"
          + "-fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    public static Button infoButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + INFO + "; -fx-text-fill: white;"
          + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 22;"
          + "-fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    public static Button successButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + SUCCESS + "; -fx-text-fill: #0A1A0A;"
          + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 22;"
          + "-fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    // ── Labels ────────────────────────────────────────────────────────────
    public static Label heading(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        return lbl;
    }

    public static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        lbl.setMinWidth(130);
        return lbl;
    }

    public static Label statusBadge(String text, boolean positive) {
        Label lbl = new Label("  " + text + "  ");
        String color = positive ? SUCCESS : DANGER;
        lbl.setStyle(
            "-fx-background-color: " + color + "22;"
          + "-fx-border-color: " + color + ";"
          + "-fx-border-radius: 20; -fx-background-radius: 20;"
          + "-fx-text-fill: " + color + ";"
          + "-fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;"
        );
        return lbl;
    }

    // ── Fields ────────────────────────────────────────────────────────────
    public static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        applyFieldStyle(tf);
        return tf;
    }

    public static void applyFieldStyle(TextField tf) {
        tf.setStyle(
            "-fx-background-color: #0A1828;"
          + "-fx-text-fill: " + TEXT_MAIN + ";"
          + "-fx-prompt-text-fill: #44446A;"
          + "-fx-border-color: #1A3A5A;"
          + "-fx-border-radius: 7; -fx-background-radius: 7;"
          + "-fx-padding: 9 13; -fx-font-size: 13px;"
        );
    }

    public static <T> ComboBox<T> styledCombo() {
        ComboBox<T> cb = new ComboBox<>();
        cb.setStyle(
            "-fx-background-color: #0A1828;"
          + "-fx-text-fill: " + TEXT_MAIN + ";"
          + "-fx-border-color: #1A3A5A;"
          + "-fx-border-radius: 7; -fx-background-radius: 7;"
          + "-fx-font-size: 13px;"
        );
        return cb;
    }

    public static TextArea styledTextArea() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setStyle(
            "-fx-control-inner-background: #070F1A;"
          + "-fx-text-fill: #90EE90;"
          + "-fx-font-family: 'Courier New';"
          + "-fx-font-size: 12px;"
          + "-fx-background-color: #070F1A;"
          + "-fx-border-color: " + GOLD + "44;"
          + "-fx-border-radius: 7;"
        );
        return ta;
    }

    // ── Table with alternating rows ───────────────────────────────────────
    public static <T> TableView<T> styledTable() {
        TableView<T> table = new TableView<>();
        table.setStyle(
            "-fx-background-color: " + ROW_EVEN + ";"
          + "-fx-border-color: " + GOLD + "44;"
          + "-fx-border-radius: 8;"
          + "-fx-font-size: 15px;"
          + "-fx-table-cell-border-color: #1A2A3A;"
          + "-fx-text-fill: " + TEXT_MAIN + ";"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Alternating row colours
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else if (getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: " + ROW_EVEN + ";");
                } else {
                    setStyle("-fx-background-color: " + ROW_ODD + ";");
                }
            }
        });
        return table;
    }

    // ── Page header ───────────────────────────────────────────────────────
    public static HBox pageHeader(String icon, String title, String subtitle) {
        HBox header = new HBox(16);
        header.setPadding(new Insets(20, 28, 20, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, " + PANEL_BG + "CC, " + CARD_BG + ");"
          + "-fx-border-color: transparent transparent " + GOLD + "66 transparent;"
          + "-fx-border-width: 0 0 1 0;"
        );
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 30px;");
        VBox tb = new VBox(3);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 15px;");
        tb.getChildren().addAll(titleLbl, subLbl);
        header.getChildren().addAll(iconLbl, tb);
        return header;
    }

    // ── Alerts ────────────────────────────────────────────────────────────
    public static void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        styleAlert(a); a.showAndWait();
    }

    public static void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        styleAlert(a); a.showAndWait();
    }

    public static boolean showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        styleAlert(a);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void styleAlert(Alert a) {
        a.getDialogPane().setStyle("-fx-background-color: " + CARD_BG + ";");
        try {
            a.getDialogPane().lookup(".content.label")
             .setStyle("-fx-text-fill: " + TEXT_MAIN + "; -fx-font-size: 13px;");
        } catch (Exception ignored) {}
    }

    // ── Form row ──────────────────────────────────────────────────────────
    public static HBox formRow(String labelText, javafx.scene.Node field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = fieldLabel(labelText);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(lbl, field);
        return row;
    }
}