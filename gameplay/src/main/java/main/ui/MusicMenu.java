package main.ui;

import java.io.InputStream;
import java.lang.reflect.Method;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.costomCursor.GameCursor;
import main.createScene.GameSceneFactory;
import main.model.Album;
import main.model.Song;
import main.play.BackgroundPaneVLC;
import main.play.PianoGameApp;
import main.score.ScoreManager;

public class MusicMenu {
    private Label songNameLabel;
    private Label artistLabel;
    private ImageView coverImage;
    private final Album album;

    private BackgroundPaneVLC bg;

    private StackPane sceneLayer;
    private Pane previewLayer;
    private Pane dimLayer;

    private PauseTransition previewTimer;
    private Timeline fadeOutTimeline;

    private Song selectedSong;
    private int selectedLevel = 800;

    private Label easyButton;
    private Label normalButton;
    private Label advanceButton;
    private Label rankLabel;
    private Label scoreLabel;

    private VBox songListBox;
    private ScrollPane musicScrollPane;

    private static final double APP_WIDTH = GameSceneFactory.APP_WIDTH;
    private static final double APP_HEIGHT = GameSceneFactory.APP_HEIGHT;

    private GameCursor gameCursor;

    public MusicMenu(Album album) {
        this.album = album;
    }

    public Scene createScene(Stage stage) {
        if (album != null && album.getSongs() != null && !album.getSongs().isEmpty()) {
            selectedSong = album.getSongs().get(0);
            selectedLevel = selectedSong.getEasy();
        }

        BorderPane root = new BorderPane();

        root.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        root.setStyle("-fx-background-color: transparent;");

        root.setTop(createTopBar());
        root.setLeft(createMusicList());
        root.setCenter(createRightImagePanel());
        root.setBottom(createBottomBar(stage));

        sceneLayer = new StackPane();
        sceneLayer.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        sceneLayer.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        sceneLayer.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        sceneLayer.setAlignment(Pos.CENTER);

        previewLayer = new Pane();
        previewLayer.setManaged(false);
        previewLayer.setMouseTransparent(true);
        previewLayer.setPickOnBounds(false);
        previewLayer.resizeRelocate(
                0,
                0,
                GameSceneFactory.APP_WIDTH,
                GameSceneFactory.APP_HEIGHT
        );
        previewLayer.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        previewLayer.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        previewLayer.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        Rectangle previewClip = new Rectangle(
                GameSceneFactory.APP_WIDTH,
                GameSceneFactory.APP_HEIGHT
        );
        previewLayer.setClip(previewClip);

        dimLayer = new Pane();
        dimLayer.setManaged(false);
        dimLayer.setMouseTransparent(true);
        dimLayer.resizeRelocate(
                0,
                0,
                GameSceneFactory.APP_WIDTH,
                GameSceneFactory.APP_HEIGHT
        );
        dimLayer.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        dimLayer.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        dimLayer.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        dimLayer.setStyle("""
        -fx-background-color:
            linear-gradient(to bottom right,
                rgba(36,25,78,0.72),
                rgba(78,57,155,0.58),
                rgba(39,146,198,0.45));
    """);

        StackPane.setAlignment(root, Pos.CENTER);

        sceneLayer.getChildren().addAll(previewLayer, dimLayer, root);

        GameCursor[] cursorHolder = new GameCursor[1];

        Scene scene = GameSceneFactory.createScene(sceneLayer, cursorHolder);

        gameCursor = cursorHolder[0];

        stage.setResizable(false);
        stage.setOnCloseRequest(e -> stopPreviewBackground());

        return scene;
    }


    private void playPreviewBackground(String videoPath) {
        if (previewLayer == null || videoPath == null || videoPath.isBlank()) {
            return;
        }

        stopPreviewBackground();

        bg = new BackgroundPaneVLC(videoPath, APP_WIDTH, APP_HEIGHT);

        bg.setManaged(false);
        bg.setMouseTransparent(true);
        bg.setPickOnBounds(false);

        bg.resizeRelocate(0, 0, APP_WIDTH, APP_HEIGHT);
        bg.setPrefSize(APP_WIDTH, APP_HEIGHT);
        bg.setMinSize(APP_WIDTH, APP_HEIGHT);
        bg.setMaxSize(APP_WIDTH, APP_HEIGHT);

        /*
         * Không dùng GaussianBlur trực tiếp trên video VLC.
         * Một số VLC surface / Canvas khi setEffect có thể làm sai bounds,
         * gây UI bị lệch hoặc render không ổn định.
         */
        bg.setOpacity(0.72);

        previewLayer.getChildren().clear();
        previewLayer.getChildren().add(bg);

        bg.playFromStart(true);
        bg.setRuntimeVolume(100);

        BackgroundPaneVLC currentBg = bg;

        previewTimer = new PauseTransition(Duration.seconds(30));
        previewTimer.setOnFinished(e -> fadeOutAndStopPreview(currentBg));
        previewTimer.play();
    }

    private void fadeOutAndStopPreview(BackgroundPaneVLC targetBg) {
        if (targetBg == null || bg != targetBg) {
            return;
        }

        if (fadeOutTimeline != null) {
            fadeOutTimeline.stop();
        }

        fadeOutTimeline = new Timeline();

        int steps = 20;
        double fadeMillis = 1500;

        for (int i = 0; i <= steps; i++) {
            final int volume = 100 - (100 * i / steps);
            final double opacity = 0.72 * (1.0 - i / (double) steps);
            double time = fadeMillis * i / steps;

            fadeOutTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(time), e -> {
                        if (bg == targetBg) {
                            targetBg.setRuntimeVolume(volume);
                            targetBg.setOpacity(opacity);
                        }
                    })
            );
        }

        fadeOutTimeline.setOnFinished(e -> {
            if (bg == targetBg) {
                removeAndDisposeBackground(targetBg);
                bg = null;
                fadeOutTimeline = null;
            }
        });

        fadeOutTimeline.play();
    }

    private void stopPreviewBackground() {
        if (previewTimer != null) {
            previewTimer.stop();
            previewTimer = null;
        }

        if (fadeOutTimeline != null) {
            fadeOutTimeline.stop();
            fadeOutTimeline = null;
        }

        if (bg != null) {
            removeAndDisposeBackground(bg);
            bg = null;
        }
    }

    private void removeAndDisposeBackground(BackgroundPaneVLC targetBg) {
        if (previewLayer != null) {
            previewLayer.getChildren().remove(targetBg);

        }

        try {
            targetBg.stop();
        } catch (Exception ignored) {
        }

        try {
            targetBg.dispose();
        } catch (Exception ignored) {
        }
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(24);
        topBar.setPadding(new Insets(16, 32, 14, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.setStyle("""
            -fx-background-color: rgba(255,255,255,0.16);
            -fx-border-color: rgba(255,255,255,0.28);
            -fx-border-width: 0 0 1.5 0;
        """);

        Label title = new Label("Music Select");
        title.setFont(Font.font("Arial", 29));
        title.setTextFill(Color.WHITE);

        Label pack = new Label(album != null ? album.getAlbum() : "PACK");
        pack.setFont(Font.font("Arial", 15));
        pack.setTextFill(Color.web("#eadcff"));
        pack.setPadding(new Insets(6, 16, 6, 16));
        pack.setStyle("""
            -fx-background-color: rgba(255,255,255,0.18);
            -fx-background-radius: 18;
            -fx-border-color: rgba(255,255,255,0.35);
            -fx-border-radius: 18;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        rankLabel = createCurrencyLabel("Rank", "-", "#3b8cff");
        scoreLabel = createCurrencyLabel("Score", "0", "#b15cff");
        updateTopBarScoreInfo();

        topBar.getChildren().addAll(title, pack, spacer, rankLabel, scoreLabel);
        return topBar;
    }

    private Label createCurrencyLabel(String name, String value, String color) {
        Label label = new Label(name + "  " + value);
        label.setFont(Font.font("Arial", 15));
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(8, 20, 8, 20));
        label.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 22;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 12, 0.2, 0, 4);
        """.formatted(color));
        return label;
    }

    private void updateTopBarScoreInfo() {
        if (rankLabel == null || scoreLabel == null) {
            return;
        }

        String songId = getSongId(selectedSong);
        if (songId.isBlank()) {
            rankLabel.setText("Rank  -");
            scoreLabel.setText("Score  0");
            return;
        }

        String rank = ScoreManager.getRank(songId);
        int score = ScoreManager.getBestScore(songId);

        rankLabel.setText("Rank  " + rank);
        scoreLabel.setText("Score  " + score);
    }

    private String getSongId(Song songData) {
        if (songData == null) {
            return "";
        }

        String[] methodNames = {"getSongId", "getSongID", "getId", "getID"};
        for (String methodName : methodNames) {
            try {
                Method method = songData.getClass().getMethod(methodName);
                Object value = method.invoke(songData);
                if (value != null && !value.toString().isBlank()) {
                    return value.toString();
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback: nếu class Song chưa có id riêng thì dùng title làm songId.
        return songData.getTitle() != null ? songData.getTitle() : "";
    }

    private VBox createMusicList() {
        VBox leftBox = new VBox(16);
        leftBox.setPrefWidth(540);
        leftBox.setMinWidth(540);
        leftBox.setMaxWidth(540);
        leftBox.setPadding(new Insets(28, 22, 90, 34));

        Label listTitle = new Label("Music List");
        listTitle.setFont(Font.font("Arial", 32));
        listTitle.setTextFill(Color.WHITE);

        Label hint = new Label("Choose your favorite track");
        hint.setFont(Font.font("Arial", 14));
        hint.setTextFill(Color.web("#d9ccff"));

        songListBox = new VBox(12);

        if (album != null && album.getSongs() != null && !album.getSongs().isEmpty()) {
            refreshSongList();
        } else {
            Label empty = new Label("Không có bài hát");
            empty.setFont(Font.font("Arial", 20));
            empty.setTextFill(Color.web("#eeeeee"));
            songListBox.getChildren().add(empty);
        }

        musicScrollPane = new ScrollPane(songListBox);
        musicScrollPane.setFitToWidth(true);
        musicScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        musicScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        musicScrollPane.setPannable(true);

        musicScrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
            -fx-padding: 0;
        """);

        musicScrollPane.setBackground(Background.EMPTY);
        songListBox.setBackground(Background.EMPTY);

        addSmoothScroll(musicScrollPane, songListBox);

        VBox.setVgrow(musicScrollPane, Priority.ALWAYS);

        leftBox.getChildren().addAll(listTitle, hint, musicScrollPane);

        return leftBox;
    }

    private void refreshSongList() {
        if (songListBox == null || album == null || album.getSongs() == null) {
            return;
        }

        songListBox.getChildren().clear();

        for (Song song : album.getSongs()) {
            boolean isSelected = song == selectedSong;
            songListBox.getChildren().add(createSongItem(song, isSelected));
        }
    }

    private void addSmoothScroll(ScrollPane scrollPane, VBox content) {
        final double[] targetVvalue = {scrollPane.getVvalue()};
        final boolean[] scrolling = {false};

        AnimationTimer smoothScroll = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double current = scrollPane.getVvalue();
                double target = targetVvalue[0];
                double diff = target - current;

                if (Math.abs(diff) < 0.001) {
                    scrollPane.setVvalue(target);
                    scrolling[0] = false;
                    stop();
                    return;
                }

                scrollPane.setVvalue(current + diff * 0.18);
            }
        };

        scrollPane.vvalueProperty().addListener((obs, oldValue, newValue) -> {
            if (!scrolling[0]) {
                targetVvalue[0] = newValue.doubleValue();
            }
        });

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double contentHeight = content.getBoundsInLocal().getHeight();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            double maxScroll = contentHeight - viewportHeight;

            if (maxScroll <= 0) {
                return;
            }

            double delta = event.getDeltaY();

            if (delta == 0) {
                delta = event.getDeltaX();
            }

            double scrollSpeed = 1.4;
            double next = targetVvalue[0] - delta * scrollSpeed / maxScroll;

            targetVvalue[0] = Math.max(0, Math.min(1, next));

            if (!scrolling[0]) {
                scrolling[0] = true;
                smoothScroll.start();
            }

            event.consume();
        });
    }

    private StackPane createSongItem(Song songData, boolean selected) {
        StackPane card = new StackPane();
        card.setPrefHeight(88);
        card.setMinHeight(88);
        card.setMaxHeight(88);

        card.setPrefWidth(470);
        card.setMaxWidth(470);
        card.setCursor(Cursor.NONE);

        card.setStyle(selected ? """
            -fx-background-color: linear-gradient(to right, rgba(143,76,255,0.98), rgba(186,82,255,0.9));
            -fx-background-radius: 20;
            -fx-border-color: rgba(255,255,255,0.55);
            -fx-border-radius: 20;
            -fx-border-width: 1.5;
        """ : """
            -fx-background-color: rgba(255,255,255,0.20);
            -fx-background-radius: 20;
            -fx-border-color: rgba(255,255,255,0.28);
            -fx-border-radius: 20;
            -fx-border-width: 1.2;
        """);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(selected ? 18 : 10);
        shadow.setColor(Color.rgb(0, 0, 0, selected ? 0.26 : 0.16));
        card.setEffect(shadow);

        HBox content = new HBox(14);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10, 16, 10, 14));

        ImageView thumb = new ImageView();
        Image image = loadImage(songData.getImagePath());

        if (image != null) {
            thumb.setImage(image);
        }

        thumb.setFitWidth(58);
        thumb.setFitHeight(58);
        thumb.setPreserveRatio(false);

        Rectangle clip = new Rectangle(58, 58);
        clip.setArcWidth(14);
        clip.setArcHeight(14);
        thumb.setClip(clip);

        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPrefWidth(285);
        textBox.setMaxWidth(285);

        Label song = new Label(songData.getTitle());
        song.setFont(Font.font("Arial", selected ? 21 : 19));
        song.setTextFill(Color.WHITE);
        song.setMaxWidth(285);
        song.setTextOverrun(OverrunStyle.ELLIPSIS);
        song.setWrapText(false);

        Label artist = new Label(songData.getArtist());
        artist.setFont(Font.font("Arial", 12));
        artist.setTextFill(selected ? Color.web("#f1e9ff") : Color.web("#dfd5ff"));
        artist.setMaxWidth(285);
        artist.setTextOverrun(OverrunStyle.ELLIPSIS);
        artist.setWrapText(false);

        HBox levels = new HBox(6);
        levels.setAlignment(Pos.CENTER_LEFT);

        levels.getChildren().addAll(
                createLevelTag("EASY " + songData.getEasy(), selected),
                createLevelTag("NORMAL " + songData.getNormal(), selected),
                createLevelTag("ADVANCE " + songData.getAdvance(), selected)
        );

        textBox.getChildren().addAll(song, artist, levels);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("›");
        arrow.setFont(Font.font("Arial", 34));
        arrow.setTextFill(Color.web("#ffffffcc"));

        content.getChildren().addAll(thumb, textBox, spacer, arrow);
        card.getChildren().add(content);

        ScaleTransition stIn = new ScaleTransition(Duration.millis(150), card);
        stIn.setToX(1.025);
        stIn.setToY(1.025);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(150), card);
        stOut.setToX(1.0);
        stOut.setToY(1.0);

        card.setOnMouseEntered(e -> {
            stOut.stop();
            stIn.playFromStart();
            useHoverCursor();
        });

        card.setOnMouseExited(e -> {
            stIn.stop();
            stOut.playFromStart();
            useNormalCursor();
        });

        card.setOnMouseClicked(e -> selectSong(songData));

        return card;
    }

    private void selectSong(Song songData) {
        if (songData == null) {
            return;
        }

        selectedSong = songData;
        selectedLevel = songData.getEasy();

        songNameLabel.setText(songData.getTitle());
        artistLabel.setText(songData.getArtist());

        updateDifficultyStyle("EASY");

        Image newImage = loadImage(songData.getImagePath());

        if (newImage != null) {
            coverImage.setImage(newImage);
        }
        updateTopBarScoreInfo();

        refreshSongList();

        playPreviewBackground(songData.getVideoPath());
    }

    private Label createLevelTag(String text, boolean selected) {
        Label tag = new Label(text);
        tag.setFont(Font.font("Arial", 11));
        tag.setTextFill(Color.WHITE);
        tag.setPadding(new Insets(3, 8, 3, 8));

        tag.setStyle(selected ? """
            -fx-background-color: rgba(255,255,255,0.24);
            -fx-background-radius: 12;
        """ : """
            -fx-background-color: rgba(255,255,255,0.16);
            -fx-background-radius: 12;
        """);

        return tag;
    }

    private VBox createRightImagePanel() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(18, 70, 70, 20));

        VBox rightBox = new VBox(14);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.setPadding(new Insets(26, 38, 30, 38));

        rightBox.setPrefWidth(520);
        rightBox.setMaxWidth(520);
        rightBox.setPrefHeight(615);
        rightBox.setMaxHeight(615);


        Song firstSong = selectedSong;

        songNameLabel = new Label(firstSong != null ? firstSong.getTitle() : "No Song");
        artistLabel = new Label(firstSong != null ? firstSong.getArtist() : "");

        songNameLabel.setFont(Font.font("Arial", 34));
        songNameLabel.setTextFill(Color.WHITE);
        songNameLabel.setMaxWidth(460);
        songNameLabel.setAlignment(Pos.CENTER);
        songNameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        songNameLabel.setWrapText(false);

        artistLabel.setFont(Font.font("Arial", 16));
        artistLabel.setTextFill(Color.web("#eadcff"));
        artistLabel.setMaxWidth(440);
        artistLabel.setAlignment(Pos.CENTER);
        artistLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        artistLabel.setWrapText(false);

        coverImage = new ImageView();

        Image defaultImage = loadImage(firstSong != null ? firstSong.getImagePath() : "/img/character01.jpg");

        if (defaultImage != null) {
            coverImage.setImage(defaultImage);
        }

        coverImage.setFitWidth(306);
        coverImage.setFitHeight(306);
        coverImage.setPreserveRatio(false);

        Rectangle clip = new Rectangle(306, 306);
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        coverImage.setClip(clip);

        DropShadow imageShadow = new DropShadow();
        imageShadow.setRadius(22);
        imageShadow.setColor(Color.rgb(0, 0, 0, 0.32));
        coverImage.setEffect(imageShadow);


        Label startButton = new Label("START");
        startButton.setFont(Font.font("Arial", 24));
        startButton.setTextFill(Color.WHITE);
        startButton.setPadding(new Insets(13, 82, 13, 82));
        startButton.setCursor(Cursor.NONE);

        startButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #6446e8, #b84fff);
            -fx-background-radius: 32;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.32), 18, 0.25, 0, 6);
        """);

        startButton.setOnMouseEntered(e -> {
            startButton.setScaleX(1.045);
            startButton.setScaleY(1.045);
            useHoverCursor();
        });

        startButton.setOnMouseExited(e -> {
            startButton.setScaleX(1.0);
            startButton.setScaleY(1.0);
            useNormalCursor();
        });

        startButton.setOnMouseClicked(e -> {
            if (selectedSong == null) {
                System.out.println("Chưa chọn bài hát");
                return;
            }

            stopPreviewBackground();

            Stage currentStage = (Stage) startButton.getScene().getWindow();

            PianoGameApp gameApp = new PianoGameApp();

            Scene gameScene = gameApp.createScene(
                    currentStage,
                    album,
                    selectedSong,
                    selectedLevel
            );

            currentStage.setScene(gameScene);
            currentStage.sizeToScene();
            currentStage.centerOnScreen();
            gameScene.getRoot().requestFocus();
        });

        rightBox.getChildren().addAll(
                songNameLabel,
                artistLabel,
                coverImage,
                createDifficultySelector(),
                startButton
        );

        outer.getChildren().add(rightBox);

        return outer;
    }

    private HBox createDifficultySelector() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPrefHeight(44);

        easyButton = createDifficultyButton("EASY", true);
        normalButton = createDifficultyButton("NORMAL", false);
        advanceButton = createDifficultyButton("ADVANCE", false);

        easyButton.setOnMouseClicked(e -> {
            if (selectedSong != null) {
                selectedLevel = selectedSong.getEasy();
                updateDifficultyStyle("EASY");
            }
        });

        normalButton.setOnMouseClicked(e -> {
            if (selectedSong != null) {
                selectedLevel = selectedSong.getNormal();
                updateDifficultyStyle("NORMAL");
            }
        });

        advanceButton.setOnMouseClicked(e -> {
            if (selectedSong != null) {
                selectedLevel = selectedSong.getAdvance();
                updateDifficultyStyle("ADVANCE");
            }
        });

        box.getChildren().addAll(easyButton, normalButton, advanceButton);

        return box;
    }

    private Label createDifficultyButton(String text, boolean selected) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 15));
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(108);
        label.setMinWidth(108);
        label.setMaxWidth(108);
        label.setPadding(new Insets(8, 0, 8, 0));

        label.setTextFill(selected ? Color.WHITE : Color.web("#eee6ff"));
        label.setStyle(selected ? selectedDifficultyStyle() : normalDifficultyStyle());
        label.setCursor(Cursor.NONE);

        label.setOnMouseEntered(e -> {
            label.setScaleX(1.045);
            label.setScaleY(1.045);
            useHoverCursor();
        });

        label.setOnMouseExited(e -> {
            label.setScaleX(1.0);
            label.setScaleY(1.0);
            useNormalCursor();
        });

        return label;
    }

    private void updateDifficultyStyle(String difficulty) {
        if (easyButton == null || normalButton == null || advanceButton == null) {
            return;
        }

        easyButton.setStyle(normalDifficultyStyle());
        easyButton.setTextFill(Color.web("#eee6ff"));

        normalButton.setStyle(normalDifficultyStyle());
        normalButton.setTextFill(Color.web("#eee6ff"));

        advanceButton.setStyle(normalDifficultyStyle());
        advanceButton.setTextFill(Color.web("#eee6ff"));

        if ("EASY".equals(difficulty)) {
            easyButton.setStyle(selectedDifficultyStyle());
            easyButton.setTextFill(Color.WHITE);
        } else if ("NORMAL".equals(difficulty)) {
            normalButton.setStyle(selectedDifficultyStyle());
            normalButton.setTextFill(Color.WHITE);
        } else if ("ADVANCE".equals(difficulty)) {
            advanceButton.setStyle(selectedDifficultyStyle());
            advanceButton.setTextFill(Color.WHITE);
        }
    }

    private String normalDifficultyStyle() {
        return """
            -fx-background-color: rgba(255,255,255,0.20);
            -fx-background-radius: 22;
            -fx-border-color: rgba(255,255,255,0.36);
            -fx-border-radius: 22;
            -fx-border-width: 1.2;
            -fx-cursor: none;
        """;
    }

    private String selectedDifficultyStyle() {
        return """
            -fx-background-color: linear-gradient(to right, #5f4bd6, #b15cff);
            -fx-background-radius: 22;
            -fx-border-color: rgba(255,255,255,0.55);
            -fx-border-radius: 22;
            -fx-border-width: 1.2;
            -fx-cursor: none;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 10, 0.2, 0, 4);
        """;
    }

    private HBox createBottomBar(Stage stage) {
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(0, 0, 24, 34));
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        Label backButton = new Label("Back");
        backButton.setFont(Font.font("Arial", 18));
        backButton.setTextFill(Color.WHITE);
        backButton.setPadding(new Insets(10, 32, 10, 32));
        backButton.setCursor(Cursor.NONE);

        backButton.setStyle("""
            -fx-background-color: rgba(255,255,255,0.18);
            -fx-background-radius: 26;
            -fx-border-color: rgba(255,255,255,0.35);
            -fx-border-radius: 26;
            -fx-border-width: 1.2;
        """);

        backButton.setOnMouseEntered(e -> {
            backButton.setScaleX(1.045);
            backButton.setScaleY(1.045);
            useHoverCursor();
        });

        backButton.setOnMouseExited(e -> {
            backButton.setScaleX(1.0);
            backButton.setScaleY(1.0);
            useNormalCursor();
        });

        backButton.setOnMouseClicked(e -> {
            stopPreviewBackground();

            AlbumMenu albumMenu = new AlbumMenu();
            stage.setScene(albumMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();
            e.consume();

        });

        bottomBar.getChildren().add(backButton);

        return bottomBar;
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            path = "/img/character01.jpg";
        }

        Image image = loadImageFromResource(path);

        if (image != null) {
            return image;
        }

        return loadImageFromResource("/img/character01.jpg");
    }

    private Image loadImageFromResource(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }

            return new Image(stream);
        } catch (Exception e) {
            System.out.println("Lỗi load ảnh: " + path);
            return null;
        }
    }

    private void useHoverCursor() {
        if (gameCursor != null) {
            gameCursor.useHoverCursor();
        }
    }

    private void useNormalCursor() {
        if (gameCursor != null) {
            gameCursor.useNormalCursor();
        }
    }
}