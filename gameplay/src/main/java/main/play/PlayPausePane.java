package main.play;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PlayPausePane extends Pane implements EventHandler<ActionEvent> {

    private final RoundedButton play;
    private final RoundedButton exit;

    private Image buttonImage;
    @SuppressWarnings("unused")
    private String fileImage;

    private final Runnable onPlay;
    private final Runnable onExit;

    private final Rectangle overlay;

    // thuộc tính chỉnh nút
    private double buttonWidth = 300;
    private double buttonHeight = 70;
    private double buttonRadius = 30;

    // khoảng cách giữa 2 nút
    private double buttonGap = 25;

    public PlayPausePane(Runnable onPlay, Runnable onExit) {
        this.onPlay = onPlay;
        this.onExit = onExit;

        setPrefSize(1400, 800);
        setPickOnBounds(true);

        // nền mờ
        overlay = new Rectangle();
        overlay.setFill(Color.rgb(0, 0, 0, 0.5));
        overlay.widthProperty().bind(widthProperty());
        overlay.heightProperty().bind(heightProperty());

        play = new RoundedButton("PLAY");
        exit = new RoundedButton("EXIT");

        play.setOnAction(this);
        exit.setOnAction(this);

        getChildren().addAll(overlay, play, exit);

        updateButtonLayout();

        // tự căn lại nếu cửa sổ đổi kích thước
        widthProperty().addListener((obs, oldVal, newVal) -> updateButtonLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateButtonLayout());
    }



    public void setButtonImage(String fileImage) {
        this.fileImage = fileImage;

        var url = getClass().getResource(fileImage);
        if (url == null) {
            throw new IllegalArgumentException("Không tìm thấy ảnh: " + fileImage);
        }

        buttonImage = new Image(url.toExternalForm());
        play.updateStyle();
        exit.updateStyle();
    }

    public void setButtonSize(double width, double height) {
        this.buttonWidth = width;
        this.buttonHeight = height;
        updateButtonLayout();
    }

    public void setButtonRadius(double radius) {
        this.buttonRadius = radius;
        play.updateStyle();
        exit.updateStyle();
    }

    public void setButtonGap(double gap) {
        this.buttonGap = gap;
        updateButtonLayout();
    }


    // layout

    private void updateButtonLayout() {
        applyButtonSize(play);
        applyButtonSize(exit);

        double paneWidth = getWidth() > 0 ? getWidth() : getPrefWidth();
        double paneHeight = getHeight() > 0 ? getHeight() : getPrefHeight();

        // căn giữa ngang
        double x = (paneWidth - buttonWidth) / 2.0;

        // căn giữa dọc cho cả 2 nút
        double totalHeight = buttonHeight * 2 + buttonGap;
        double startY = (paneHeight - totalHeight) / 2.0;

        play.setLayoutX(x);
        play.setLayoutY(startY);

        exit.setLayoutX(x);
        exit.setLayoutY(startY + buttonHeight + buttonGap);

        play.updateStyle();
        exit.updateStyle();
    }

    private void applyButtonSize(Button button) {
        button.setPrefSize(buttonWidth, buttonHeight);
        button.setMinSize(buttonWidth, buttonHeight);
        button.setMaxSize(buttonWidth, buttonHeight);
    }

    // =========================
    // event
    // =========================

    @Override
    public void handle(ActionEvent e) {
        Object source = e.getSource();

        if (source == play) {
            if (onPlay != null) onPlay.run();
        }

        if (source == exit) {
            if (onExit != null) onExit.run();
        }
    }


    // inner button class

    class RoundedButton extends Button {

        private final Rectangle clip;

        public RoundedButton(String text) {
            super(text);

            setAlignment(Pos.CENTER);
            setTextFill(Color.WHITE);
            setFont(Font.font("Arial", FontWeight.BOLD, 20));
            setFocusTraversable(false);
            setBorder(Border.EMPTY);
            setPadding(Insets.EMPTY);

            // clip để bo góc thật
            clip = new Rectangle();
            clip.widthProperty().bind(widthProperty());
            clip.heightProperty().bind(heightProperty());
            setClip(clip);

            updateStyle();
        }

        public void updateStyle() {
            // bo góc đúng: dùng radius * 2
            clip.setArcWidth(buttonRadius * 2);
            clip.setArcHeight(buttonRadius * 2);

            BackgroundFill fill = new BackgroundFill(
                    Color.web("#3A3A3A"),
                    new CornerRadii(buttonRadius),
                    Insets.EMPTY
            );

            if (buttonImage != null) {
                // cover toàn bộ nút
                BackgroundSize size = new BackgroundSize(
                        100, 100,
                        true, true,
                        false, true
                );

                BackgroundImage bgImage = new BackgroundImage(
                        buttonImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        size
                );

                setBackground(new Background(
                        List.of(fill),
                        List.of(bgImage)
                ));
            } else {
                setBackground(new Background(fill));
            }
        }
    }
}
