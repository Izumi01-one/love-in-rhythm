package main.ui;

import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import main.MainMenuApp;
import main.costomCursor.GameCursor;
import main.createScene.GameSceneFactory;
import main.model.Album;
import main.score.ScoreManager;
import main.service.AlbumService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumMenu {

    private static final double CARD_WIDTH = 190;
    private static final double CARD_HEIGHT = 390;

    private final List<AlbumCard> albumCards = new ArrayList<>();

    private GameCursor gameCursor;
    private StackPane sceneLayer;

    private ScoreManager scoreManager = new ScoreManager();
    public Scene createScene(Stage stage) {
        albumCards.clear();

        BorderPane root = new BorderPane();

        root.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        root.setStyle("""
            -fx-background-color:
            linear-gradient(to bottom right, rgba(247,244,255,0.35), rgba(234,247,255,0.35));
        """);

        root.setTop(createTopBar());
        root.setCenter(createAlbumArea(stage));
        root.setBottom(createBottomBar(stage));

        sceneLayer = new StackPane();

        sceneLayer.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        sceneLayer.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        sceneLayer.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        sceneLayer.getChildren().addAll(
                createBackgroundDecor(),
                root
        );

        GameCursor[] cursorHolder = new GameCursor[1];

        Scene scene = GameSceneFactory.createScene(sceneLayer, cursorHolder);

        gameCursor = cursorHolder[0];

        return scene;
    }

    private Pane createBackgroundDecor() {
        Pane bg = new Pane();
        bg.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        bg.setMouseTransparent(true);

        Polygon deco1 = new Polygon(
                80.0, 540.0,
                310.0, 320.0,
                390.0, 370.0,
                150.0, 610.0
        );
        deco1.setFill(Color.web("#bfc8ef", 0.20));

        Polygon deco2 = new Polygon(
                470.0, 610.0,
                700.0, 290.0,
                770.0, 330.0,
                540.0, 660.0
        );
        deco2.setFill(Color.web("#8ca4da", 0.16));

        Polygon deco3 = new Polygon(
                900.0, 570.0,
                1130.0, 350.0,
                1210.0, 410.0,
                960.0, 640.0
        );
        deco3.setFill(Color.web("#c4b6e8", 0.20));

        bg.getChildren().addAll(deco1, deco2, deco3);
        return bg;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(25);
        topBar.setPadding(new Insets(10, 25, 10, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.setStyle("""
            -fx-background-color: rgba(255,255,255,0.75);
            -fx-border-color: #cdbdf5;
            -fx-border-width: 0 0 2 0;
        """);

        Label title = new Label("Select Album");
        title.setFont(Font.font("Arial", 22));
        title.setTextFill(Color.web("#333333"));

        Label brand = new Label("Love in Love");
        brand.setFont(Font.font("Arial", 18));
        brand.setTextFill(Color.web("#5d4a8f"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label fragments = new Label("Fragments " + scoreManager.getTotalPlayedSongs());
        fragments.setFont(Font.font("Arial", 15));
        fragments.setTextFill(Color.WHITE);
        fragments.setPadding(new Insets(7, 18, 7, 18));
        fragments.setStyle("""
            -fx-background-color: #3b8ccf;
            -fx-background-radius: 4;
        """);

        Label memories = new Label("Memories " + scoreManager.getHighestScoreAllSongs());
        memories.setFont(Font.font("Arial", 15));
        memories.setTextFill(Color.WHITE);
        memories.setPadding(new Insets(7, 18, 7, 18));
        memories.setStyle("""
            -fx-background-color: #8b4ed8;
            -fx-background-radius: 4;
        """);

        topBar.getChildren().addAll(title, brand, spacer, fragments, memories);

        return topBar;
    }

    private StackPane createAlbumArea(Stage stage) {
        StackPane container = new StackPane();
        container.setPadding(new Insets(40, 50, 35, 50));

        VBox mainBox = new VBox(25);
        mainBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Album List");
        title.setFont(Font.font("Arial", 34));
        title.setTextFill(Color.web("#3c2c5f"));
        title.setStyle("-fx-text-fill: #3c2c5f;");

        HBox albumRow = new HBox(28);
        albumRow.setAlignment(Pos.CENTER_LEFT);
        albumRow.setPadding(new Insets(20, 25, 25, 25));

        List<Album> albums = AlbumService.loadAlbums();

        if (albums != null && !albums.isEmpty()) {
            for (Album album : albums) {
                AlbumCard card = new AlbumCard(album);

                card.setOnMouseClicked(e -> {
                    MusicMenu musicMenu = new MusicMenu(album);
                    stage.setScene(musicMenu.createScene(stage));
                    stage.sizeToScene();
                    stage.centerOnScreen();
                    e.consume();
                });

                albumCards.add(card);
                albumRow.getChildren().add(card);
            }
        } else {
            Label empty = new Label("Không có album");
            empty.setFont(Font.font("Arial", 24));
            empty.setTextFill(Color.GRAY);
            albumRow.getChildren().add(empty);
        }

        ScrollPane scrollPane = new ScrollPane(albumRow);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);

        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
            -fx-padding: 0;
        """);

        scrollPane.setBackground(Background.EMPTY);
        albumRow.setBackground(Background.EMPTY);

        setupSmoothHorizontalScroll(scrollPane, albumRow);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainBox.getChildren().addAll(title, scrollPane);

        Label sideLabel = new Label("Albums");
        sideLabel.setFont(Font.font("Arial", 26));
        sideLabel.setTextFill(Color.web("#7c6ca8"));
        sideLabel.setRotate(-90);

        StackPane.setAlignment(sideLabel, Pos.CENTER_RIGHT);
        StackPane.setMargin(sideLabel, new Insets(0, 0, 0, 0));

        container.getChildren().addAll(mainBox, sideLabel);

        return container;
    }

    private void setupSmoothHorizontalScroll(ScrollPane scrollPane, HBox content) {
        final double[] targetHvalue = {0.0};
        final boolean[] scrolling = {false};

        AnimationTimer smoothScroll = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double current = scrollPane.getHvalue();
                double target = targetHvalue[0];
                double diff = target - current;

                if (Math.abs(diff) < 0.001) {
                    scrollPane.setHvalue(target);
                    scrolling[0] = false;
                    stop();
                    return;
                }

                scrollPane.setHvalue(current + diff * 0.18);
            }
        };

        scrollPane.hvalueProperty().addListener((obs, oldValue, newValue) -> {
            if (!scrolling[0]) {
                targetHvalue[0] = newValue.doubleValue();
            }
        });

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double contentWidth = content.getBoundsInLocal().getWidth();
            double viewportWidth = scrollPane.getViewportBounds().getWidth();

            double maxScroll = contentWidth - viewportWidth;

            if (maxScroll <= 0) {
                return;
            }

            double delta = event.getDeltaY();

            if (delta == 0) {
                delta = event.getDeltaX();
            }

            double scrollSpeed = 1.4;
            double next = targetHvalue[0] - delta * scrollSpeed / maxScroll;

            targetHvalue[0] = Math.max(0, Math.min(1, next));

            if (!scrolling[0]) {
                scrolling[0] = true;
                smoothScroll.start();
            }

            event.consume();
        });
    }

    private HBox createBottomBar(Stage stage) {
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(0, 0, 25, 30));
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        Label mainButton = new Label("Main Menu");
        mainButton.setFont(Font.font("Arial", 20));
        mainButton.setTextFill(Color.WHITE);
        mainButton.setPadding(new Insets(10, 35, 10, 35));
        mainButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #5f4bd6, #b15cff);
            -fx-background-radius: 25;
            -fx-cursor: none;
        """);

        mainButton.setCursor(Cursor.NONE);

        mainButton.setOnMouseEntered(e -> {
            if (gameCursor != null) {
                gameCursor.useHoverCursor();
            }
        });

        mainButton.setOnMouseExited(e -> {
            if (gameCursor != null) {
                gameCursor.useNormalCursor();
            }
        });

        mainButton.setOnMouseClicked(e -> {
            MainMenuApp mainMenu = new MainMenuApp();
            stage.setScene(mainMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();
            e.consume();
        });

        bottomBar.getChildren().add(mainButton);

        return bottomBar;
    }

    // =========================
    // Album Card
    // =========================
    private class AlbumCard extends StackPane {

        private final Album album;
        private final Polygon border;
        private final Rectangle glow;

        public AlbumCard(Album album) {
            this.album = album;

            setPrefSize(CARD_WIDTH, CARD_HEIGHT);
            setMinSize(CARD_WIDTH, CARD_HEIGHT);
            setMaxSize(CARD_WIDTH, CARD_HEIGHT);
            setPickOnBounds(false);
            setCursor(Cursor.NONE);

            Polygon clip = createCardShape(0, 0, CARD_WIDTH, CARD_HEIGHT);
            setClip(clip);

            Region imageBackground = createImageBackground(album.getCoverPath());

            Rectangle topOverlay = new Rectangle(CARD_WIDTH, 76);
            topOverlay.setTranslateY(-(CARD_HEIGHT / 2) + 38);
            topOverlay.setFill(Color.web("#2e2145", 0.88));
            topOverlay.setMouseTransparent(true);

            Rectangle bottomOverlay = new Rectangle(CARD_WIDTH, 120);
            bottomOverlay.setTranslateY((CARD_HEIGHT / 2) - 60);
            bottomOverlay.setFill(Color.web("#08070c", 0.45));
            bottomOverlay.setMouseTransparent(true);

            glow = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            glow.setFill(Color.web("#ffffff", 0.18));
            glow.setEffect(new GaussianBlur(20));
            glow.setVisible(false);
            glow.setMouseTransparent(true);

            border = createCardShape(1.5, 1.5, CARD_WIDTH - 3, CARD_HEIGHT - 3);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.web("#6d6489"));
            border.setStrokeWidth(2);
            border.setMouseTransparent(true);

            DropShadow shadow = new DropShadow();
            shadow.setRadius(14);
            shadow.setColor(Color.rgb(100, 80, 160, 0.30));
            border.setEffect(shadow);

            VBox content = createCardContent();

            Polygon hitArea = createCardShape(0, 0, CARD_WIDTH, CARD_HEIGHT);
            hitArea.setFill(Color.TRANSPARENT);
            hitArea.setStroke(null);
            hitArea.setCursor(Cursor.NONE);

            getChildren().addAll(
                    imageBackground,
                    topOverlay,
                    bottomOverlay,
                    glow,
                    content,
                    border,
                    hitArea
            );

            setupHoverAnimation(hitArea);
        }

        private VBox createCardContent() {
            VBox content = new VBox();
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(18, 12, 20, 12));
            content.setMouseTransparent(true);

            Label topTitle = new Label(album.getAlbum());
            topTitle.setFont(Font.font("Arial", 17));
            topTitle.setTextFill(Color.WHITE);
            topTitle.setWrapText(true);
            topTitle.setAlignment(Pos.CENTER);
            topTitle.setMaxWidth(CARD_WIDTH - 30);

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            Label mainTitle = new Label(album.getAlbum());
            mainTitle.setFont(Font.font("Arial", 24));
            mainTitle.setTextFill(Color.WHITE);
            mainTitle.setWrapText(true);
            mainTitle.setAlignment(Pos.CENTER);
            mainTitle.setMaxWidth(CARD_WIDTH - 28);

            Label count = new Label(getSongCountText());
            count.setFont(Font.font("Arial", 14));
            count.setTextFill(Color.web("#e8e2ff"));

            content.getChildren().addAll(topTitle, spacer, mainTitle, count);

            return content;
        }

        private String getSongCountText() {
            if (album.getSongs() == null) {
                return "0 Songs";
            }

            int size = album.getSongs().size();

            if (size <= 1) {
                return size + " Song";
            }

            return size + " Songs";
        }

        private Region createImageBackground(String imagePath) {
            StackPane bg = new StackPane();
            bg.setMouseTransparent(true);

            InputStream imageStream = null;

            if (imagePath != null && !imagePath.isBlank()) {
                imageStream = getClass().getResourceAsStream(imagePath);
            }

            if (imageStream != null) {
                ImageView imageView = new ImageView(new Image(imageStream));
                imageView.setFitWidth(CARD_WIDTH);
                imageView.setFitHeight(CARD_HEIGHT);
                imageView.setPreserveRatio(false);
                imageView.setMouseTransparent(true);

                bg.getChildren().add(imageView);
            } else {
                Rectangle placeholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
                placeholder.setFill(Color.web("#b4aacd"));
                placeholder.setMouseTransparent(true);

                bg.getChildren().add(placeholder);
            }

            Rectangle darkOverlay = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            darkOverlay.setFill(Color.web("#120d1e", 0.25));
            darkOverlay.setMouseTransparent(true);

            bg.getChildren().add(darkOverlay);

            return bg;
        }

        private void setupHoverAnimation(Polygon hitArea) {
            ScaleTransition stIn = new ScaleTransition(Duration.millis(160), this);
            stIn.setToX(1.05);
            stIn.setToY(1.05);

            ScaleTransition stOut = new ScaleTransition(Duration.millis(160), this);
            stOut.setToX(1.0);
            stOut.setToY(1.0);

            hitArea.setOnMouseEntered(e -> {
                stOut.stop();
                stIn.playFromStart();

                glow.setVisible(true);
                border.setStroke(Color.web("#f1d8ff"));
                border.setStrokeWidth(3);

                if (gameCursor != null) {
                    gameCursor.useHoverCursor();
                }
            });

            hitArea.setOnMouseExited(e -> {
                stIn.stop();
                stOut.playFromStart();

                glow.setVisible(false);
                border.setStroke(Color.web("#6d6489"));
                border.setStrokeWidth(2);

                if (gameCursor != null) {
                    gameCursor.useNormalCursor();
                }
            });

            hitArea.setOnMouseClicked(e -> {
                AlbumCard.this.fireEvent(e);
            });
        }
    }

    private Polygon createCardShape(double x, double y, double width, double height) {
        return new Polygon(
                x + 20.0, y,
                x + width - 20.0, y,
                x + width, y + 30.0,
                x + width, y + height - 30.0,
                x + width - 20.0, y + height,
                x + 20.0, y + height,
                x, y + height - 30.0,
                x, y + 30.0
        );
    }
}