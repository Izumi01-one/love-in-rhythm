package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.checkResource.ResourceCheckerApp;
import main.comingSoon.ComingSoonPopup;
import main.costomCursor.GameCursor;
import main.moreButton.More;
import main.notesMenu.NotesMenu;
import main.play.UniKeyController;
import main.scale.AutoScale;
import main.score.ScoreManager;
import main.setting.SettingMenu;
import main.ui.AlbumMenu;

/**
 * Giao diện menu chính
 * - Top bar
 * - Menu bên trái
 * - Khối World ở giữa
 * - Panel Updates bên phải
 */
public class MainMenuApp extends Application {
    private GameCursor gameCursor;
    private ScoreManager scoreManager = new ScoreManager();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Love In Rhythm");
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/img/icons/icon2.png"))
        ));



        stage.setScene(createScene(stage));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private static final double APP_WIDTH = 1400;
    private static final double APP_HEIGHT = 800;

    public Scene createScene(Stage stage) {
        StackPane gameRoot = new StackPane();
        gameRoot.setPrefSize(APP_WIDTH, APP_HEIGHT);
        gameRoot.setMinSize(APP_WIDTH, APP_HEIGHT);
        gameRoot.setMaxSize(APP_WIDTH, APP_HEIGHT);

        Node backgroundLayer = createBackgroundLayer();

        Rectangle darkOverlay = new Rectangle(APP_WIDTH, APP_HEIGHT);
        darkOverlay.setFill(Color.rgb(20, 20, 30, 0.18));

        BorderPane mainLayout = new BorderPane();

        mainLayout.setTop(createTopBar());
        mainLayout.setLeft(createLeftMenu(stage, gameRoot));
        mainLayout.setCenter(createCenterArea());
        mainLayout.setRight(createUpdatesPanel());

        BorderPane.setMargin(mainLayout.getTop(), new Insets(0, 0, 8, 0));
        BorderPane.setMargin(mainLayout.getLeft(), new Insets(110, 0, 50, 40));
        BorderPane.setMargin(mainLayout.getCenter(), new Insets(80, 0, 60, 0));
        BorderPane.setMargin(mainLayout.getRight(), new Insets(170, 40, 70, 0));

        gameRoot.getChildren().addAll(backgroundLayer, darkOverlay, mainLayout);

        // Tính scale để game vừa với màn hình
        AutoScale autoScale = new AutoScale();
        double scale = autoScale.calculateScale(APP_WIDTH, APP_HEIGHT);

        /*
         * Nếu bạn muốn màn hình lớn vẫn giữ đúng kích cỡ gốc 1400x800,
         * không phóng to lên, bật dòng này:
         */
        scale = Math.min(scale, 1.0);

        // Chỉ scale gameRoot, KHÔNG scale cursor
        gameRoot.getTransforms().add(new Scale(scale, scale, 0, 0));

        Group scaledGameRoot = new Group(gameRoot);

        // Layer tổng
        StackPane windowRoot = new StackPane();

        // Layer cursor nằm trên cùng và không bị scale
        Pane cursorLayer = new Pane();
        cursorLayer.setMouseTransparent(true);
        cursorLayer.setPickOnBounds(false);

        windowRoot.getChildren().addAll(scaledGameRoot, cursorLayer);

        Scene scene = new Scene(
                windowRoot,
                APP_WIDTH * scale,
                APP_HEIGHT * scale
        );

        // Cho cursorLayer luôn bằng kích thước Scene
        cursorLayer.prefWidthProperty().bind(scene.widthProperty());
        cursorLayer.prefHeightProperty().bind(scene.heightProperty());

        gameCursor = new GameCursor(
                cursorLayer,
                scene
        );

        gameCursor.setSize(48, 48);

        // Nếu đầu chuột bị lệch thì chỉnh ở đây
        gameCursor.setHotSpot(0, 0);


        return scene;
    }



    /**
     * Tạo background:
     * - Nếu có file assets/menu_bg.jpg => dùng ảnh
     * - Nếu không có => dùng nền gradient để app vẫn chạy được
     */
    private Node createBackgroundLayer() {
        try {
            SettingMenu settingMenu = new SettingMenu();

            //lấy đường dẫn background từ setting
            Image bgImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(settingMenu.getBackgroundPath())));
            ImageView bgView = new ImageView(bgImage);
            bgView.setFitWidth(APP_WIDTH);
            bgView.setFitHeight(APP_HEIGHT);
            bgView.setPreserveRatio(false);
            return bgView;
        } catch (Exception e) {
            // Fallback gradient nếu chưa có ảnh
            Region fallback = new Region();
            fallback.setPrefSize(APP_WIDTH, APP_HEIGHT);
            fallback.setBackground(new Background(new BackgroundFill(
                    new LinearGradient(
                            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#ede9f4")),
                            new Stop(0.45, Color.web("#d7d3e3")),
                            new Stop(1, Color.web("#a9a6bd"))
                    ),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
            return fallback;
        }
    }

    /**
     * Thanh top bar phía trên:
     * - Logo/tên game bên trái
     * - Một số chip thông tin/điểm số bên phải
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(25);
        topBar.setPadding(new Insets(10, 25, 10, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("""
                -fx-background-color: rgba(255,255,255,0.75);
                -fx-border-color: #cdbdf5;
                -fx-border-width: 0 0 2 0;
                """);

        Label title = new Label("Love In Love");
        title.setFont(Font.font("Arial", 22));
        title.setTextFill(Color.web("#333333"));


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

        topBar.getChildren().addAll(title, spacer, fragments, memories);
        return topBar;
    }


    /**
     * Menu trái: các nút polygon giống phong cách ảnh
     * - Music Play
     * - Story Mode
     * - Course Mode
     * - More
     */
    private StackPane createLeftMenu(Stage stage, StackPane gameRoot) {
        StackPane leftMenu = new StackPane();
        leftMenu.setPickOnBounds(false);

        Pane layer = new Pane();
        layer.setPrefSize(520, 420);

        StackPane btnMusic = createPolygonButton("Music Play", 310, 68,
                Color.rgb(75, 70, 82, 0.72), Color.rgb(120, 110, 145, 0.95));


        StackPane btnFriends = createPolygonButton("Friends", 310, 68,
                Color.rgb(88, 74, 86, 0.74), Color.rgb(148, 118, 154, 0.95));

        StackPane btnNote = createPolygonButton("Notes", 290, 68,
                Color.rgb(93, 92, 100, 0.72), Color.rgb(125, 137, 170, 0.95));

        StackPane btnSetting = createPolygonButton("Setting", 135, 56,
                Color.rgb(165, 165, 175, 0.65), Color.rgb(190, 190, 210, 0.95));

        StackPane btnMore = createPolygonButton("More", 170, 56,
                Color.rgb(120, 114, 125, 0.72), Color.rgb(155, 143, 167, 0.96));

        StackPane worldCard = createDiamondCard("World", "Coming soon", "01");

        ComingSoonPopup comingSoonPopup = new ComingSoonPopup(gameRoot);

        More more = new More(gameRoot);

        // ===== Đặt vị trí thủ công =====
        int x = 70;
        btnMusic.setLayoutX(150 - x);
        btnMusic.setLayoutY(65);

        btnFriends.setLayoutX(150 - x);
        btnFriends.setLayoutY(150);

        btnNote.setLayoutX(250 - x);
        btnNote.setLayoutY(230);

        // hàng dưới
        btnSetting.setLayoutX(200 - x);
        btnSetting.setLayoutY(310);

        btnMore.setLayoutX(350 - x);
        btnMore.setLayoutY(310);

        worldCard.setLayoutX(430 - x);
        worldCard.setLayoutY(25);

        // Click event
        btnMusic.setOnMouseClicked(e -> {
            AlbumMenu albumMenu = new AlbumMenu();
            stage.setScene(albumMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();
            e.consume();
        });

        btnSetting.setOnMouseClicked(e -> {
            SettingMenu settingMenu = new SettingMenu();
            stage.setScene(settingMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();
            e.consume();
        });
        btnNote.setOnMouseClicked(e -> {
            NotesMenu notesMenu = new NotesMenu();
            stage.setScene(notesMenu.createScene(stage));
            stage.sizeToScene();
            stage.centerOnScreen();
            e.consume();
        });


        btnFriends.setOnMouseClicked(e -> {
            comingSoonPopup.show();
            e.consume();
        });

        worldCard.setOnMouseClicked(e -> {
            comingSoonPopup.show();
            e.consume();
        });

        btnMore.setOnMouseClicked(e -> {
            more.show();
            e.consume();
        });


        layer.getChildren().addAll(btnMusic, btnFriends, btnNote, btnSetting, btnMore, worldCard);
        leftMenu.getChildren().add(layer);

        return leftMenu;
    }




    /**
     * Khu vực giữa:
     * - Khối kim cương "World"
     * - Placeholder art nhân vật bên phải (nếu chưa có ảnh)
     */
    private StackPane createCenterArea() {
        StackPane center = new StackPane();
        center.setPrefSize(650, 500);


        Pane characterArtPane = createCharacterArtPlaceholder();

        StackPane.setAlignment(characterArtPane, Pos.CENTER_RIGHT);
        characterArtPane.setTranslateX(120);

        center.getChildren().addAll(characterArtPane);

        return center;
    }

    /**
     * Placeholder vùng art nhân vật.
     * Sau này có thể thay bằng ImageView ảnh nhân vật.
     */
    private Pane createCharacterArtPlaceholder() {
        Pane artPane = new Pane();
        artPane.setPrefSize(460, 460);

        try {
            // Load ảnh
            Image img = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/img/character01.jpg")
            ));

            ImageView iv = new ImageView(img);

            // ====== Kích thước vùng cần phủ ảnh ======
            double frameWidth = 340;
            double frameHeight = 390;
            double frameX = 80;
            double frameY = 20;

            // ====== Scale ảnh kiểu COVER ======
            double imgWidth = img.getWidth();
            double imgHeight = img.getHeight();

            double scale = Math.max(frameWidth / imgWidth, frameHeight / imgHeight);

            double fittedWidth = imgWidth * scale;
            double fittedHeight = imgHeight * scale;

            iv.setFitWidth(fittedWidth);
            iv.setFitHeight(fittedHeight);
            iv.setPreserveRatio(true);

            // Canh giữa ảnh trong vùng khung - vị trí hiển thị
            iv.setLayoutX(frameX - (fittedWidth - frameWidth) / 2 - 40);
            iv.setLayoutY(frameY - (fittedHeight - frameHeight) / 2);

            // ====== Polygon làm CLIP theo đúng khung abstractShape1 ======
            Polygon clipShape = new Polygon(
                    160.0, 20.0,
                    350.0, 70.0,
                    400.0, 250.0,
                    290.0, 410.0,
                    150.0, 390.0,
                    80.0, 220.0
            );
            iv.setClip(clipShape);

            // ====== Viền khung để nhìn rõ shape ======
            Polygon abstractShape2 = new Polygon(
                    250.0, 10.0,
                    420.0, 150.0,
                    360.0, 430.0,
                    180.0, 420.0,
                    130.0, 120.0
            );
            abstractShape2.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(20, 30, 60, 0.45)),
                    new Stop(1, Color.rgb(220, 120, 180, 0.25))
            ));

            artPane.getChildren().addAll(abstractShape2, iv);

        } catch (Exception e) {
            // ====== Fallback nếu không có ảnh ======
            Polygon abstractShape1 = new Polygon(
                    160.0, 20.0,
                    350.0, 70.0,
                    400.0, 250.0,
                    290.0, 410.0,
                    150.0, 390.0,
                    80.0, 220.0
            );
            abstractShape1.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(48, 44, 80, 0.65)),
                    new Stop(1, Color.rgb(130, 100, 145, 0.35))
            ));
            abstractShape1.setStroke(Color.rgb(255, 255, 255, 0.35));

            Polygon abstractShape2 = new Polygon(
                    250.0, 10.0,
                    420.0, 150.0,
                    360.0, 430.0,
                    180.0, 420.0,
                    130.0, 120.0
            );
            abstractShape2.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(20, 30, 60, 0.45)),
                    new Stop(1, Color.rgb(220, 120, 180, 0.25))
            ));

            Label placeholder = new Label("CHARACTER ART");
            placeholder.setFont(Font.font("Arial", 22));
            placeholder.setTextFill(Color.rgb(255, 255, 255, 0.85));
            placeholder.setLayoutX(170);
            placeholder.setLayoutY(205);

            artPane.getChildren().addAll(abstractShape2, abstractShape1, placeholder);
        }

        return artPane;
    }


    /**
     * Tạo khối hình kim cương ở giữa giống "World"
     */
    private StackPane createDiamondCard(String title, String subtitle, String number) {
        StackPane card = new StackPane();
        card.setPrefSize(230, 230);

        // Chỉ hover/click khi chuột nằm trên diamond thật
        card.setPickOnBounds(false);

        double size = 230;
        double half = size / 2;

        Polygon diamond = new Polygon(
                half, 0,
                size, half,
                half, size,
                0, half
        );

        diamond.setFill(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(50, 42, 60, 0.90)),
                new Stop(0.55, Color.rgb(122, 86, 138, 0.82)),
                new Stop(1, Color.rgb(55, 48, 78, 0.92))
        ));

        Color normalStroke = Color.rgb(255, 255, 255, 0.65);
        Color hoverStroke  = Color.rgb(255, 220, 255, 0.95);

        diamond.setStroke(normalStroke);
        diamond.setStrokeWidth(2.2);

        DropShadow normalShadow = new DropShadow(22, Color.rgb(0, 0, 0, 0.28));
        DropShadow hoverShadow  = new DropShadow(30, Color.rgb(255, 170, 220, 0.35));
        diamond.setEffect(normalShadow);

        VBox textBox = new VBox(6);
        textBox.setAlignment(Pos.CENTER);

        // Không để text bắt chuột
        textBox.setMouseTransparent(true);

        Label titleLb = new Label(title);
        titleLb.setFont(Font.font("Arial", 34));
        titleLb.setTextFill(Color.WHITE);
        titleLb.setMouseTransparent(true);

        Label subtitleLb = new Label(subtitle);
        subtitleLb.setFont(Font.font("Arial", 14));
        subtitleLb.setTextFill(Color.rgb(255, 255, 255, 0.92));
        subtitleLb.setMouseTransparent(true);

        Label numLb = new Label(number);
        numLb.setFont(Font.font("Arial", 24));
        numLb.setTextFill(Color.web("#ff4fa1"));
        numLb.setPadding(new Insets(2, 10, 2, 10));
        numLb.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 0.18),
                new CornerRadii(12),
                Insets.EMPTY
        )));
        numLb.setMouseTransparent(true);

        textBox.getChildren().addAll(titleLb, subtitleLb, numLb);

        ScaleTransition stIn = new ScaleTransition(Duration.millis(180), card);
        stIn.setToX(1.05);
        stIn.setToY(1.05);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(180), card);
        stOut.setToX(1.0);
        stOut.setToY(1.0);

        card.setCursor(javafx.scene.Cursor.NONE);

        card.setOnMouseEntered(e -> {
            stOut.stop();
            diamond.setStroke(hoverStroke);
            diamond.setEffect(hoverShadow);
            stIn.playFromStart();

            if (gameCursor != null) {
                gameCursor.useHoverCursor();
            }

        });

        card.setOnMouseExited(e -> {
            stIn.stop();
            diamond.setStroke(normalStroke);
            diamond.setEffect(normalShadow);
            stOut.playFromStart();

            if (gameCursor != null) {
                gameCursor.useNormalCursor();
            }

        });


        card.getChildren().addAll(diamond, textBox);
        return card;
    }


    /**
     * Panel updates ở góc phải dưới.
     */
    private VBox createUpdatesPanel() {
        VBox updates = new VBox(12);
        updates.setAlignment(Pos.TOP_CENTER);
        updates.setPadding(new Insets(14));
        updates.setPrefWidth(180);

        updates.setBackground(new Background(new BackgroundFill(
                Color.rgb(37, 35, 52, 0.58),
                new CornerRadii(22),
                Insets.EMPTY
        )));
        updates.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 255, 255, 0.20),
                BorderStrokeStyle.SOLID,
                new CornerRadii(22),
                new BorderWidths(1.2)
        )));

        Label title = new Label("UPDATES");
        title.setFont(Font.font("Arial", 15));
        title.setTextFill(Color.WHITE);

        Region card = createUpdateThumbnail();

        Label subtitle = new Label("Coming Soon");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.rgb(255, 255, 255, 0.85));

        updates.getChildren().addAll(title, card, subtitle);
        return updates;
    }

    /**
     * Thumbnail demo cho khung updates.
     * Nếu có assets/update.jpg thì dùng ảnh, nếu không thì dùng placeholder.
     */
    private Region createUpdateThumbnail() {
        StackPane thumb = new StackPane();
        thumb.setPrefSize(110, 150);
        thumb.setMaxSize(110, 150);

        Rectangle border = new Rectangle(110, 150);
        border.setArcWidth(18);
        border.setArcHeight(18);
        border.setStroke(Color.rgb(255, 255, 255, 0.45));
        border.setFill(Color.rgb(255, 255, 255, 0.10));

        try {
            Image img = new Image(new FileInputStream("assets/update.jpg"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(110);
            iv.setFitHeight(150);
            iv.setPreserveRatio(false);

            Rectangle clip = new Rectangle(110, 150);
            clip.setArcWidth(18);
            clip.setArcHeight(18);
            iv.setClip(clip);

            thumb.getChildren().addAll(iv, border);
        } catch (IOException | IllegalArgumentException e) {
            // Lỗi tải hình ảnh hoặc format không hợp lệ
            Rectangle fill = new Rectangle(110, 150);
            fill.setArcWidth(18);
            fill.setArcHeight(18);
            fill.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(92, 76, 140, 0.9)),
                    new Stop(1, Color.rgb(205, 92, 142, 0.85))
            ));


            Label event = new Label("EVENT");
            event.setFont(Font.font("Arial", 20));
            event.setTextFill(Color.WHITE);



            thumb.getChildren().addAll(fill, border, event);
        }

        return thumb;
    }

    /**
     * Tạo nút polygon kiểu sci-fi / anime UI
     * Dùng StackPane gồm:
     * - Polygon nền
     * - Label text
     *
     * @param text chữ hiển thị
     * @param width chiều rộng nút
     * @param height chiều cao nút
     * @param normalColor màu mặc định
     * @param hoverColor màu khi hover
     */
    private StackPane createPolygonButton(String text, double width, double height, @SuppressWarnings("unused") Color normalColor, @SuppressWarnings("unused") Color hoverColor) {
        StackPane card = new StackPane();
        card.setPrefSize(width, height);

        // Chỉ pick theo hình thật (không theo bounds hình chữ nhật)
        card.setPickOnBounds(false);

        // ===== Shape của nút (lục giác vát cạnh) =====
        Polygon shape = new Polygon(
                height / 2, 0.0,
                width - height / 2, 0.0,
                width, height / 2,
                width - height / 2, height,
                height / 2, height,
                0.0, height / 2
        );


        // Style cơ bản
        Color normalStroke = Color.rgb(255, 255, 255, 0.88);
        Color hoverStroke  = Color.rgb(255, 220, 255, 1.0);

        DropShadow normalShadow = new DropShadow(15, Color.rgb(0, 0, 0, 0.20));
        DropShadow hoverShadow  = new DropShadow(22, Color.rgb(255, 180, 220, 0.30));

        shape.setFill(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(50, 42, 60, 0.90)),
                new Stop(0.55, Color.rgb(122, 86, 138, 0.82)),
                new Stop(1, Color.rgb(55, 48, 78, 0.92))
        ));


        shape.setStroke(normalStroke);
        shape.setStrokeWidth(2.2);
        shape.setEffect(normalShadow);

        // ===== Text layer (VBox) =====
        VBox textBox = new VBox();
        textBox.setAlignment(Pos.CENTER);

        // Không để text bắt chuột
        textBox.setMouseTransparent(true);

        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 22));
        label.setMouseTransparent(true);

        textBox.getChildren().add(label);

        // QUAN TRỌNG: chữ không bắt chuột => hover/click theo polygon


        // ===== Hover animation (giống Diamond: scale + stroke + shadow + fill) =====
        ScaleTransition stIn = new ScaleTransition(Duration.millis(160), card);
        stIn.setToX(1.03);
        stIn.setToY(1.03);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(160), card);
        stOut.setToX(1.0);
        stOut.setToY(1.0);

        shape.setCursor(javafx.scene.Cursor.NONE);



        // ===== GẮN EVENT VÀO shape để chỉ hover đúng vùng polygon =====
        shape.setOnMouseEntered(e -> {
            stOut.stop();
            shape.setStroke(hoverStroke);
            shape.setEffect(hoverShadow);
            stIn.playFromStart();

            if (gameCursor != null) {
                gameCursor.useHoverCursor();
            }


        });

        shape.setOnMouseExited(e -> {
            stIn.stop();
            shape.setStroke(normalStroke);
            shape.setEffect(normalShadow);
            stOut.playFromStart();

            if (gameCursor != null) {
                gameCursor.useNormalCursor();
            }

        });

        // Click: KHÔNG set ở đây, để bạn vẫn setOnMouseClicked ở createLeftMenu() như cũ
        // (event từ shape sẽ bubble lên card nếu set handler trên card)

        card.getChildren().addAll(shape, textBox);
        return card;
    }



    public static void main(String[] args) {
        UniKeyController.init();
        ResourceCheckerApp.launchBeforeMain(MainMenuApp.class, args);
    }
}
