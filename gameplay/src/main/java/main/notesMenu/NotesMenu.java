package main.notesMenu;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

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
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import main.MainMenuApp;
import main.costomCursor.GameCursor;
import main.createScene.GameSceneFactory;
import main.model.GameSetting;
import main.model.IconItem;
import main.setting.SettingManager;

public class NotesMenu {

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;

    private final IconRepository iconRepository = new IconRepository();
    private final SettingManager settingManager = new SettingManager();

    private List<IconItem> icons;

    private String selectedIconPath = "/img/icons/icon2.png";
    private String backgroundPath = "/img/background/background02.png";

    private StackPane backgroundLayer;
    private GridPane iconGrid;
    private ScrollPane iconScrollPane;

    private Label selectedNameLabel;
    private ImageView selectedPreview;

    private GameCursor gameCursor;

    public Scene createScene(Stage stage) {
        loadSettings();
        icons = iconRepository.loadIcons();

        StackPane root = new StackPane();
        root.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        backgroundLayer = createBackground();
        Region blurOverlay = createBlurOverlay();
        BorderPane panel = createMainPanel(stage);

        root.getChildren().addAll(backgroundLayer, blurOverlay, panel);

        GameCursor[] holder = new GameCursor[1];
        Scene scene = GameSceneFactory.createScene(root, holder);
        gameCursor = holder[0];

        stage.setResizable(false);

        return scene;
    }

    private StackPane createBackground() {
        StackPane bgLayer = new StackPane();
        bgLayer.setPrefSize(WIDTH, HEIGHT);
        bgLayer.setMinSize(WIDTH, HEIGHT);
        bgLayer.setMaxSize(WIDTH, HEIGHT);

        applyBackground(bgLayer);
        bgLayer.setEffect(new GaussianBlur(2));

        return bgLayer;
    }

    private void applyBackground(StackPane bgLayer) {
        bgLayer.getChildren().clear();

        try {
            URL imageUrl = getClass().getResource(backgroundPath);

            if (imageUrl == null) {
                throw new IllegalArgumentException("Background not found: " + backgroundPath);
            }

            Image bgImage = new Image(imageUrl.toExternalForm());
            ImageView bgView = new ImageView(bgImage);

            bgView.setFitWidth(WIDTH);
            bgView.setFitHeight(HEIGHT);
            bgView.setPreserveRatio(false);
            bgView.setSmooth(true);

            bgLayer.getChildren().add(bgView);

        } catch (Exception e) {
            Region fallback = new Region();
            fallback.setPrefSize(WIDTH, HEIGHT);
            fallback.setMinSize(WIDTH, HEIGHT);
            fallback.setMaxSize(WIDTH, HEIGHT);

            fallback.setStyle("""
                -fx-background-color:
                    linear-gradient(to bottom right,
                        #1b2435 0%,
                        #33445c 35%,
                        #67895f 70%,
                        #d8dda6 100%);
            """);

            bgLayer.getChildren().add(fallback);
        }
    }

    private Region createBlurOverlay() {
        Region overlay = new Region();
        overlay.setPrefSize(WIDTH, HEIGHT);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.30);");
        return overlay;
    }

    private BorderPane createMainPanel(Stage stage) {
        BorderPane panel = new BorderPane();

        panel.setMaxSize(980, 650);
        panel.setPadding(new Insets(28));

        panel.setStyle("""
            -fx-background-color: rgba(24, 28, 38, 0.58);
            -fx-background-radius: 28;
            -fx-border-radius: 28;
            -fx-border-color: rgba(255, 255, 255, 0.25);
            -fx-border-width: 1.2;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.45), 35, 0.25, 0, 10);
        """);

        panel.setTop(createHeader(stage));
        panel.setCenter(createIconContent());

        StackPane.setAlignment(panel, Pos.CENTER);

        return panel;
    }

    private HBox createHeader(Stage stage) {
        Label title = new Label("Icon Select");
        title.setTextFill(Color.web("#f6e6b8"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label closeBtn = new Label("✕");
        closeBtn.setTextFill(Color.web("#333333"));
        closeBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        closeBtn.setAlignment(Pos.CENTER);
        closeBtn.setCursor(Cursor.NONE);
        closeBtn.setPrefSize(38, 38);

        closeBtn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.78);
            -fx-background-radius: 50;
        """);

        closeBtn.setOnMouseEntered(e -> {
            closeBtn.setScaleX(1.08);
            closeBtn.setScaleY(1.08);
            useHoverCursor();
        });

        closeBtn.setOnMouseExited(e -> {
            closeBtn.setScaleX(1.0);
            closeBtn.setScaleY(1.0);
            useNormalCursor();
        });

        closeBtn.setOnMouseClicked(e -> {
            saveSettings();

            MainMenuApp mainMenu = new MainMenuApp();
            stage.setScene(mainMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();

            e.consume();
        });

        HBox header = new HBox(12, title, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 22, 0));

        return header;
    }

    private VBox createIconContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(10, 0, 0, 0));

        Label sectionTitle = new Label("Choose Your Icon");
        sectionTitle.setTextFill(Color.WHITE);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));

        HBox previewBox = createSelectedPreview();

        iconGrid = new GridPane();
        iconGrid.setHgap(18);
        iconGrid.setVgap(18);
        iconGrid.setPadding(new Insets(8, 8, 8, 8));

        refreshIconGrid();

        iconScrollPane = new ScrollPane(iconGrid);
        iconScrollPane.setFitToWidth(true);
        iconScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        iconScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        iconScrollPane.setPannable(true);
        iconScrollPane.setBackground(Background.EMPTY);

        iconScrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
            -fx-padding: 0;
        """);

        addSmoothScroll(iconScrollPane, iconGrid);

        VBox.setVgrow(iconScrollPane, Priority.ALWAYS);

        content.getChildren().addAll(sectionTitle, previewBox, iconScrollPane);

        return content;
    }

    private HBox createSelectedPreview() {
        selectedPreview = new ImageView();
        selectedPreview.setFitWidth(72);
        selectedPreview.setFitHeight(72);
        selectedPreview.setPreserveRatio(false);

        Rectangle clip = new Rectangle(72, 72);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        selectedPreview.setClip(clip);

        Image img = loadImage(selectedIconPath);
        if (img != null) {
            selectedPreview.setImage(img);
        }

        selectedNameLabel = new Label("Selected Icon");
        selectedNameLabel.setTextFill(Color.web("#ffe9a6"));
        selectedNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label hint = new Label("Click icon để chọn và lưu vào settings");
        hint.setTextFill(Color.web("#eeeeee"));
        hint.setFont(Font.font("Segoe UI", 13));

        VBox textBox = new VBox(4, selectedNameLabel, hint);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox box = new HBox(16, selectedPreview, textBox);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.setMaxWidth(900);

        box.setStyle("""
            -fx-background-color: rgba(255, 245, 220, 0.20);
            -fx-background-radius: 24;
            -fx-border-color: rgba(255,255,255,0.25);
            -fx-border-radius: 24;
            -fx-border-width: 1.1;
        """);

        return box;
    }

    private void refreshIconGrid() {
        iconGrid.getChildren().clear();

        if (icons == null || icons.isEmpty()) {
            Label empty = new Label("Không có icon");
            empty.setTextFill(Color.WHITE);
            empty.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            iconGrid.add(empty, 0, 0);
            return;
        }

        int columns = 5;

        for (int i = 0; i < icons.size(); i++) {
            IconItem icon = icons.get(i);

            int col = i % columns;
            int row = i / columns;

            StackPane item = createIconItem(icon);
            iconGrid.add(item, col, row);
        }
    }

    private StackPane createIconItem(IconItem icon) {
        boolean selected = icon.getIconPath() != null
                && icon.getIconPath().equals(selectedIconPath);

        StackPane card = new StackPane();
        card.setPrefSize(160, 150);
        card.setMinSize(160, 150);
        card.setMaxSize(160, 150);
        card.setCursor(Cursor.NONE);

        card.setStyle(selected ? """
            -fx-background-color: linear-gradient(to bottom right, rgba(255,211,78,0.95), rgba(255,151,70,0.88));
            -fx-background-radius: 24;
            -fx-border-color: rgba(255,255,255,0.75);
            -fx-border-radius: 24;
            -fx-border-width: 2;
        """ : """
            -fx-background-color: rgba(255,255,255,0.18);
            -fx-background-radius: 24;
            -fx-border-color: rgba(255,255,255,0.28);
            -fx-border-radius: 24;
            -fx-border-width: 1.2;
        """);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(selected ? 18 : 10);
        shadow.setColor(Color.rgb(0, 0, 0, selected ? 0.32 : 0.18));
        card.setEffect(shadow);

        ImageView iconView = new ImageView();
        Image image = loadImage(icon.getIconPath());

        if (image != null) {
            iconView.setImage(image);
        }

        iconView.setFitWidth(82);
        iconView.setFitHeight(82);
        iconView.setPreserveRatio(false);
        iconView.setSmooth(true);

        Rectangle clip = new Rectangle(82, 82);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        iconView.setClip(clip);

        Label nameLabel = new Label(icon.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setMaxWidth(132);
        nameLabel.setAlignment(Pos.CENTER);

        VBox box = new VBox(10, iconView, nameLabel);
        box.setAlignment(Pos.CENTER);

        card.getChildren().add(box);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(140), card);
        scaleIn.setToX(1.055);
        scaleIn.setToY(1.055);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(140), card);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        card.setOnMouseEntered(e -> {
            scaleOut.stop();
            scaleIn.playFromStart();
            useHoverCursor();
        });

        card.setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.playFromStart();
            useNormalCursor();
        });

        card.setOnMouseClicked(e -> {
            selectIcon(icon);
            e.consume();
        });

        return card;
    }

    private void selectIcon(IconItem icon) {
        if (icon == null || icon.getIconPath() == null || icon.getIconPath().isBlank()) {
            return;
        }

        selectedIconPath = icon.getIconPath();

        if (selectedPreview != null) {
            Image image = loadImage(selectedIconPath);
            if (image != null) {
                selectedPreview.setImage(image);
            }
        }

        if (selectedNameLabel != null) {
            selectedNameLabel.setText("Selected: " + icon.getName());
        }

        saveSettings();
        refreshIconGrid();

        System.out.println("Đã chọn icon: " + icon.getName() + " - " + icon.getIconPath());
    }

    private void addSmoothScroll(ScrollPane scrollPane, Region content) {
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

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            path = "/img/icons/icon2.png";
        }

        Image image = loadImageFromResource(path);

        if (image != null) {
            return image;
        }

        return loadImageFromResource("/img/icons/icon2.png");
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

    private void loadSettings() {
        settingManager.loadSettings();

        GameSetting settings = settingManager.getSettings();

        if (settings.getIconPath() != null && !settings.getIconPath().isBlank()) {
            selectedIconPath = settings.getIconPath();
        }

        if (settings.getBackgroundPath() != null && !settings.getBackgroundPath().isBlank()) {
            backgroundPath = settings.getBackgroundPath();
        }
    }

    private void saveSettings() {
        GameSetting settings = settingManager.getSettings();

        settings.setIconPath(selectedIconPath);

        settingManager.saveSettings();
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