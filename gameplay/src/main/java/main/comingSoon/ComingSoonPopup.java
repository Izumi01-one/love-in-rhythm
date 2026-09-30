package main.comingSoon;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class ComingSoonPopup {

    private static final double CARD_WIDTH = 480;
    private static final double CARD_HEIGHT = 100;

    private final StackPane root;
    private Pane overlay;
    private boolean closing = false;

    public ComingSoonPopup(StackPane root) {
        this.root = root;
    }

    public void show() {
        if (overlay != null) return;

        closing = false;

        overlay = new Pane();
        overlay.setPickOnBounds(true);
        overlay.setCursor(Cursor.NONE);
        overlay.setOpacity(0);

        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        Rectangle dim = new Rectangle();
        dim.widthProperty().bind(root.widthProperty());
        dim.heightProperty().bind(root.heightProperty());
        dim.setFill(Color.rgb(10, 7, 18, 0.62));

        StackPane card = createCard();
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setLayoutX((root.getWidth() - CARD_WIDTH) / 2);
        card.setLayoutY((root.getHeight() - CARD_HEIGHT) / 2);

        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            card.setLayoutX((newVal.doubleValue() - CARD_WIDTH) / 2);
        });

        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            card.setLayoutY((newVal.doubleValue() - CARD_HEIGHT) / 2);
        });

        dim.setOnMouseClicked(e -> close());

        card.setOnMouseClicked(e -> e.consume());

        overlay.setFocusTraversable(true);
        overlay.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER) {
                close();
            }
        });

        overlay.getChildren().addAll(dim, card);
        root.getChildren().add(overlay);

        playShowAnimation(card);

        Platform.runLater(() -> overlay.requestFocus());
    }

    private StackPane createCard() {
        StackPane card = new StackPane();
        card.setCursor(Cursor.NONE);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        card.setBackground(new Background(new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 1,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(66, 48, 92, 0.96)),
                        new Stop(0.45, Color.rgb(132, 78, 145, 0.95)),
                        new Stop(1, Color.rgb(43, 38, 68, 0.98))
                ),
                new CornerRadii(28),
                Insets.EMPTY
        )));

        card.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 220, 255, 0.75),
                BorderStrokeStyle.SOLID,
                new CornerRadii(28),
                new BorderWidths(2)
        )));

        card.setEffect(new DropShadow(38, Color.rgb(255, 90, 190, 0.32)));

        Rectangle shine = new Rectangle(CARD_WIDTH - 34, 64);
        shine.setArcWidth(28);
        shine.setArcHeight(28);
        shine.setMouseTransparent(true);
        shine.setFill(new LinearGradient(
                0, 0, 1, 0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 255, 0.18)),
                new Stop(0.5, Color.rgb(255, 255, 255, 0.07)),
                new Stop(1, Color.rgb(255, 255, 255, 0.02))
        ));

        StackPane.setAlignment(shine, Pos.TOP_CENTER);
        StackPane.setMargin(shine, new Insets(16, 0, 0, 0));

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));
        content.setPrefWidth(CARD_WIDTH);
        content.setMaxWidth(CARD_WIDTH);
        content.setMouseTransparent(false);
        //content.setTranslateY(-20);

        Label title = new Label("Coming Soon");
        title.setFont(Font.font("Arial", 36));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(12, Color.rgb(255, 130, 210, 0.45)));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setMouseTransparent(true);

        Label message = new Label("This feature will appear in the beta version.");
        message.setFont(Font.font("Arial", 20));
        message.setTextFill(Color.rgb(255, 245, 255, 0.92));
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        message.setMouseTransparent(true);

        StackPane okButton = createOkButton();
        VBox.setMargin(okButton, new Insets(2, 0, 0, 0));

        content.getChildren().addAll(title, message, okButton);

        card.getChildren().addAll(shine, content);
        StackPane.setAlignment(content, Pos.CENTER);

        return card;
    }

    private StackPane createOkButton() {
        StackPane button = new StackPane();
        button.setCursor(Cursor.NONE);
        button.setPrefSize(150, 46);
        button.setMaxSize(150, 46);

        Rectangle bg = new Rectangle(150, 46);
        bg.setArcWidth(22);
        bg.setArcHeight(22);
        bg.setFill(new LinearGradient(
                0, 0, 1, 0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff5fb8")),
                new Stop(1, Color.web("#8b5cff"))
        ));

        bg.setStroke(Color.rgb(255, 255, 255, 0.72));
        bg.setStrokeWidth(1.4);

        Label text = new Label("OK");
        text.setFont(Font.font("Arial", 18));
        text.setTextFill(Color.WHITE);
        text.setMouseTransparent(true);

        DropShadow normalShadow = new DropShadow(12, Color.rgb(0, 0, 0, 0.28));
        DropShadow hoverShadow = new DropShadow(22, Color.rgb(255, 120, 210, 0.45));
        bg.setEffect(normalShadow);

        button.setOnMouseEntered(e -> {
            bg.setEffect(hoverShadow);
            button.setScaleX(1.06);
            button.setScaleY(1.06);
        });

        button.setOnMouseExited(e -> {
            bg.setEffect(normalShadow);
            button.setScaleX(1);
            button.setScaleY(1);
        });

        button.setOnMouseClicked(e -> {
            close();
            e.consume();
        });

        button.getChildren().addAll(bg, text);
        return button;
    }

    private void playShowAnimation(Node card) {
        card.setScaleX(0.82);
        card.setScaleY(0.82);
        card.setTranslateY(22);

        FadeTransition fade = new FadeTransition(Duration.millis(180), overlay);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), card);
        scale.setFromX(0.82);
        scale.setFromY(0.82);
        scale.setToX(1);
        scale.setToY(1);

        TranslateTransition move = new TranslateTransition(Duration.millis(220), card);
        move.setFromY(22);
        move.setToY(0);

        new ParallelTransition(fade, scale, move).play();
    }

    private void close() {
        if (overlay == null || closing) return;

        closing = true;

        FadeTransition fade = new FadeTransition(Duration.millis(150), overlay);
        fade.setFromValue(overlay.getOpacity());
        fade.setToValue(0);

        fade.setOnFinished(e -> {
            root.getChildren().remove(overlay);
            overlay = null;
            closing = false;
        });

        fade.play();
    }
}
