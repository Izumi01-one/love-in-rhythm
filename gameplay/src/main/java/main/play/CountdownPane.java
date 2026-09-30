package main.play;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import main.costomCursor.GameCursor;

public class CountdownPane extends StackPane {

    private int start;
    private Runnable onFinished;
    private Timeline timeline;

    private final Label label;

    public CountdownPane(int start, Runnable onFinished) {
        this.start = start;
        this.onFinished = onFinished;

        label = new Label(String.valueOf(start));
        label.setFont(Font.font(150));
        label.setTextFill(Color.WHITE);

        setMouseTransparent(true); // FIX cursor
        getChildren().add(label);
    }

    public void start() {
        final int[] number = {start};


        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    number[0]--;

                    if (number[0] > 0) {
                        label.setText(String.valueOf(number[0]));
                    } else {
                        stop();
                        if (onFinished != null) {
                            onFinished.run();
                        }
                    }
                })
        );

        timeline.setCycleCount(start);
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}