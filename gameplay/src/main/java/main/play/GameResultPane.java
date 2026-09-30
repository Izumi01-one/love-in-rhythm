package main.play;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import main.model.Album;
import main.model.Song;
import main.score.ScoreManager;
import main.ui.MusicMenu;

public class GameResultPane extends StackPane {

    public GameResultPane(Stage stage, Album album, Song song, int finalScore) {
        int bestScore = ScoreManager.getBestScore(song.getId());

        setPrefSize(1400, 800);
        setMinSize(1400, 800);
        setMaxSize(1400, 800);

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.58);");
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);

        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(34, 48, 34, 48));
        panel.setMaxWidth(350);
        panel.setMaxHeight(400);

        panel.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #2e2145, #5f4bd6, #b15cff);
            -fx-background-radius: 28;
            -fx-border-color: rgba(255,255,255,0.45);
            -fx-border-width: 2;
            -fx-border-radius: 28;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0.25, 0, 8);
        """);

        Label title = new Label("GAME CLEAR");
        title.setFont(Font.font("Arial", 34));
        title.setTextFill(Color.WHITE);

        Label songName = new Label(song.getTitle());
        songName.setFont(Font.font("Arial", 20));
        songName.setTextFill(Color.web("#efe8ff"));

        Label scoreTitle = new Label("SCORE");
        scoreTitle.setFont(Font.font("Arial", 16));
        scoreTitle.setTextFill(Color.web("#d8ccff"));

        Label scoreLabel = new Label(String.format("%d", finalScore));
        scoreLabel.setFont(Font.font("Arial", 46));
        scoreLabel.setTextFill(Color.WHITE);

        Label bestScoreLabel = new Label("Best Score: " + String.format("%d", bestScore));
        bestScoreLabel.setFont(Font.font("Arial", 18));
        bestScoreLabel.setTextFill(Color.web("#f5edff"));

        VBox scoreBox = new VBox(6, scoreTitle, scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back to Music Menu");
        backButton.setFont(Font.font("Arial", 16));
        backButton.setTextFill(Color.WHITE);
        backButton.setCursor(Cursor.NONE);

        backButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #ff4fa1, #b15cff);
            -fx-background-radius: 22;
            -fx-padding: 10 26 10 26;
            -fx-cursor: none;
        """);

        backButton.setOnAction(e -> {
            MusicMenu musicMenu = new MusicMenu(album);

            Scene scene = musicMenu.createScene(stage);

            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
        });

        panel.getChildren().addAll(
                title,
                songName,
                scoreBox,
                bestScoreLabel,
                backButton
        );

        getChildren().add(panel);
    }
}
