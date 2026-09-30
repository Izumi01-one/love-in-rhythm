package main.setting;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import main.MainMenuApp;
import main.costomCursor.GameCursor;
import main.createScene.GameSceneFactory;
import main.model.GameSetting;

public class SettingMenu {

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;
    private final SettingManager settingManager = new SettingManager();

    private String fallSpeed = "NORMAL";
    private static final String BACKGROUND_JSON_RESOURCE = "/data/background.json";

    private final Map<String, String> backgroundMap = new LinkedHashMap<>();

    private String backgroundName = "Background 02";
    private String backgroundPath = "/img/background/background02.png";

    private StackPane backgroundLayer;


    private int volume = 70;
    private String code = "";

    private String iconPath = "/img/icons/icon2.png";
    private Label messageLabel;

    private GameCursor gameCursor;

    public Scene createScene(Stage stage) {
        loadBackgroundData();
        loadSettings();

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
        scene.getStylesheets().add(
                getClass().getResource("/css/styleSetting.css").toExternalForm()
        );
        gameCursor = holder[0];

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

        } catch (NullPointerException | IllegalArgumentException e) {
            // Lỗi tải background hoặc path không hợp lệ
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
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.28);");
        return overlay;
    }

    private BorderPane createMainPanel(Stage stage) {
        BorderPane panel = new BorderPane();
        panel.setMaxSize(900, 620);
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
//        panel.setLeft(createSidebar());
        panel.setCenter(createSettingContent());

        StackPane.setAlignment(panel, Pos.CENTER);
        return panel;
    }

    private HBox createHeader(Stage stage) {
        Label title = new Label("⚙ Settings / Game Options");
        title.setTextFill(Color.web("#f6e6b8"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.78);
            -fx-text-fill: #333333;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-background-radius: 50;
            -fx-min-width: 38;
            -fx-min-height: 38;
            -fx-cursor: none;
        """);

        closeBtn.setOnMouseEntered(e -> {
            closeBtn.setScaleX(1.08);
            closeBtn.setScaleY(1.08);
            gameCursor.useHoverCursor();
        });

        closeBtn.setOnMouseExited(e -> {
            closeBtn.setScaleX(1.0);
            closeBtn.setScaleY(1.0);
            gameCursor.useNormalCursor();
        });

        closeBtn.setOnAction(e -> {
            saveSettings();
            MainMenuApp mainMenu = new MainMenuApp();
            stage.setScene(mainMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();
            e.consume();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 22, 0));

        return header;
    }


    private VBox createSettingContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22, 0, 0, 25));

        Label sectionTitle = new Label("Game Settings");
        sectionTitle.setTextFill(Color.WHITE);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        HBox performanceRow = createPerformanceRow();

        ComboBox<String> speedBox = new ComboBox<>();
        speedBox.getItems().addAll("EASY", "NORMAL", "HARD");
        speedBox.setValue(fallSpeed);
        styleComboBox(speedBox);

        speedBox.setOnAction(e -> {
            fallSpeed = speedBox.getValue();
            saveSettings();
            showMessage("Đã lưu tốc độ rơi: " + getSpeedName(fallSpeed));
        });

        ComboBox<String> backgroundBox = new ComboBox<>();
        backgroundBox.getItems().addAll(backgroundMap.keySet());
        backgroundBox.setValue(backgroundName);
        styleComboBox(backgroundBox);

        backgroundBox.setOnAction(e -> {
            backgroundName = backgroundBox.getValue();
            backgroundPath = backgroundMap.getOrDefault(
                    backgroundName,
                    "/img/background/japan.jpg"
            );

            saveSettings();

            if (backgroundLayer != null) {
                applyBackground(backgroundLayer);
            }

            showMessage("Đã đổi background: " + backgroundName + " - " + backgroundPath);
        });

        Slider volumeSlider = new Slider(0, 100, volume);
        volumeSlider.setPrefWidth(330);

        Label volumeValue = new Label(volume + "%");
        volumeValue.setMinWidth(55);
        volumeValue.setTextFill(Color.web("#333333"));
        volumeValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            volume = newValue.intValue();
            volumeValue.setText(volume + "%");
            saveSettings();
        });

        HBox volumeBox = new HBox(14, volumeSlider, volumeValue);
        volumeBox.setAlignment(Pos.CENTER_LEFT);

        TextField codeInput = new TextField(code);
        codeInput.setPromptText("Nhập code...");
        codeInput.setPrefWidth(320);
        codeInput.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.82);
            -fx-background-radius: 18;
            -fx-border-color: transparent;
            -fx-padding: 8 15 8 15;
            -fx-font-size: 15px;
        """);

        Button applyCodeBtn = new Button("Apply");
        applyCodeBtn.setStyle("""
            -fx-background-color: #ffd34e;
            -fx-text-fill: #3a2b00;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-background-radius: 18;
            -fx-padding: 8 24 8 24;
            -fx-cursor: none;
        """);

        applyCodeBtn.setOnAction(e -> {
            code = codeInput.getText().trim();
            saveSettings();

            if (code.equalsIgnoreCase("SPEEDUP")) {
                fallSpeed = "HARD";
                saveSettings();
                showMessage("Code hợp lệ: đã chuyển tốc độ sang HARD.");
            } else if (code.isEmpty()) {
                showMessage("Bạn chưa nhập code.");
            } else {
                showMessage("Đã lưu code: " + code);
            }
        });

        HBox codeBox = new HBox(12, codeInput, applyCodeBtn);
        codeBox.setAlignment(Pos.CENTER_LEFT);

        messageLabel = new Label("");
        messageLabel.setTextFill(Color.web("#ffe9a6"));
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        content.getChildren().addAll(
                sectionTitle,
                performanceRow,
                createSettingRow("Fall Speed", getSpeedName(fallSpeed), speedBox),
                createSettingRow("Background", backgroundName, backgroundBox),
                createSettingRow("Volume", volume + "%", volumeBox),
                createSettingRow("Redeem Code", "Input", codeBox),
                messageLabel
        );

        return content;
    }

    private HBox createPerformanceRow() {
        Label label = new Label("Current Performance Load");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        Label status = new Label("FallSpeed");
        status.setTextFill(Color.web("#ffd34e"));
        status.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        ProgressBar bar = new ProgressBar(getDifficultyProgress()/10);
        bar.setPrefWidth(260);
        bar.setPrefHeight(9);

        HBox row = new HBox(20, label, status, bar);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 0, 10, 0));

        return row;
    }

    private HBox createSettingRow(String title, @SuppressWarnings("unused") String value, javafx.scene.Node control) {
        Label nameLabel = new Label(title);
        nameLabel.setMinWidth(170);
        nameLabel.setTextFill(Color.web("#4a4a4a"));
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(18, nameLabel, spacer, control);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 14, 9, 18));
        row.setMaxWidth(780);
        row.setMinHeight(48);
        row.setStyle("""
            -fx-background-color: rgba(255, 245, 220, 0.78);
            -fx-background-radius: 24;
        """);

        return row;
    }


    // custom cursor dùng riêng cho comboBox - sửa lỗi mất custom cursor
    private Cursor createImageCursor(String path) {
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("Cursor image not found: " + path);
            return Cursor.DEFAULT;
        }

        Image image = new Image(url.toExternalForm());

        // 0, 0 là điểm click của cursor.
        // Nếu ảnh cursor có đầu nhọn ở vị trí khác thì chỉnh lại.
        return new ImageCursor(image, 0, 0);
    }

    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.getStyleClass().add("setting-combo");
        comboBox.setVisibleRowCount(4);

        Cursor comboCursor = createImageCursor(
                "/img/cursor/Sanrio Hello Kitty White Arrow--cursor.png"
        );

        comboBox.setCursor(comboCursor);

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setCursor(comboCursor);
            }
        });

        comboBox.setCellFactory(listView -> {
            listView.setCursor(comboCursor);

            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    setCursor(comboCursor);
                }
            };

            cell.setCursor(comboCursor);
            return cell;
        });

        comboBox.setOnShowing(e -> comboBox.setCursor(comboCursor));
        comboBox.setOnHidden(e -> comboBox.setCursor(comboCursor));
    }



    public double getDifficultyProgress() {
        //loadSettings();
        return switch (fallSpeed) {
            case "EASY" -> 3.0;
            case "HARD" -> 9.0;
            default -> 5.0;
        };
    }

    private String getSpeedName(String speed) {
        return switch (speed) {
            case "EASY" -> "Dễ";
            case "HARD" -> "Khó";
            default -> "Thường";
        };
    }

    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
        System.out.println(message);
    }

    public void saveSettings() {
        GameSetting settings = settingManager.getSettings();

        settings.setFallSpeed(fallSpeed);
        settings.setBackgroundName(backgroundName);
        settings.setBackgroundPath(backgroundPath);
        settings.setVolume(volume);
        settings.setCode(code);
        settings.setIconPath(iconPath);

        settingManager.saveSettings();
    }

    public void loadSettings() {
        settingManager.loadSettings();

        GameSetting settings = settingManager.getSettings();

        fallSpeed = settings.getFallSpeed();
        backgroundName = settings.getBackgroundName();
        backgroundPath = settings.getBackgroundPath();
        volume = settings.getVolume();
        code = settings.getCode();
        iconPath = settings.getIconPath();

        if (!backgroundMap.isEmpty()) {
            if (!backgroundMap.containsKey(backgroundName)) {
                backgroundName = "Background 02";
                backgroundPath = "/img/background/background02.png";
            } else {
                backgroundPath = backgroundMap.get(backgroundName);
            }
        }
    }


    public String getIconPath() {
        loadSettings();
        return iconPath;
    }




    private void loadBackgroundData() {
        backgroundMap.clear();

        try (InputStream inputStream = getClass().getResourceAsStream(BACKGROUND_JSON_RESOURCE)) {

            if (inputStream == null) {
                loadDefaultBackgrounds();
                return;
            }

            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Pattern pattern = Pattern.compile(
                    "\\{\\s*\"name\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"path\"\\s*:\\s*\"(.*?)\"\\s*\\}",
                    Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(json);

            while (matcher.find()) {
                String name = matcher.group(1);
                String path = matcher.group(2);

                backgroundMap.put(name, path);
            }

            if (backgroundMap.isEmpty()) {
                loadDefaultBackgrounds();
            }

        } catch (IOException e) {
            System.err.println("Lỗi đọc file background: " + e.getMessage());
            loadDefaultBackgrounds();
        }
    }

    public String getBackgroundPath() {
        loadBackgroundData();
        loadSettings();
        return backgroundPath;
    }

    public int getVolume() {
        loadBackgroundData();
        loadSettings();
        return volume;
    }

    private void loadDefaultBackgrounds() {
        backgroundMap.put("Background 02", "/img/background/background02.png");
    }

}