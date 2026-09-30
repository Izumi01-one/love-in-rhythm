package main.checkResource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import main.costomCursor.GameCursor;
import main.createScene.GameSceneFactory;
import main.model.Album;
import main.model.Song;
import main.scale.AutoScale;
import main.setting.SettingMenu;

public class ResourceCheckerApp extends Application {

    private Label titleLabel;
    private Label detailLabel;
    private Label percentLabel;
    private Label subLabel;
    private ProgressBar progressBar;

    private Button muteButton;
    private boolean musicMuted = false;

    private MediaPlayer bgmPlayer;

    private static Class<? extends Application> nextAppClass;

    private static final String JSON_PATH = "/data/albums.json";
    private static final String BACKGROUND_MUSIC = "/audio/loading.mp3";

    private String BACKGROUND_IMAGE = "/img/loading_background.jpg";


    private static final double APP_WIDTH = 1400;
    private static final double APP_HEIGHT = 800;

    private GameCursor gameCursor;

    public static void launchBeforeMain(
            Class<? extends Application> mainAppClass,
            String[] args
    ) {
        nextAppClass = mainAppClass;
        Application.launch(ResourceCheckerApp.class, args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Love in Rhythm");
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/img/icons/icon2.png"))
        ));

        stage.setScene(createScene(stage));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        Thread worker = new Thread(() -> checkResources(stage));
        worker.setDaemon(true);
        worker.start();
    }

    private Scene createScene(Stage stage) {
        StackPane gameRoot = new StackPane();

        gameRoot.setPrefSize(APP_WIDTH, APP_HEIGHT);
        gameRoot.setMinSize(APP_WIDTH, APP_HEIGHT);
        gameRoot.setMaxSize(APP_WIDTH, APP_HEIGHT);

        setupBackground(gameRoot);
        setupDarkOverlay(gameRoot);
        setupLoadingPanel(gameRoot);
        setupMuteButton(gameRoot);
        startBackgroundMusic();

        // Tính scale giống MainMenuApp
        AutoScale autoScale = new AutoScale();
        double scale = autoScale.calculateScale(APP_WIDTH, APP_HEIGHT);

        // Giữ kích thước gốc nếu màn hình lớn hơn
        scale = Math.min(scale, 1.0);

        // Chỉ scale gameRoot, không scale cursor
        gameRoot.getTransforms().add(new Scale(scale, scale, 0, 0));

        Group scaledGameRoot = new Group(gameRoot);

        // Layer tổng
        StackPane windowRoot = new StackPane();

        // Layer cursor nằm trên cùng, không bị scale
        Pane cursorLayer = new Pane();
        cursorLayer.setMouseTransparent(true);
        cursorLayer.setPickOnBounds(false);

        windowRoot.getChildren().addAll(scaledGameRoot, cursorLayer);

        Scene scene = new Scene(
                windowRoot,
                APP_WIDTH * scale,
                APP_HEIGHT * scale
        );

        // Cursor layer luôn bằng kích thước Scene
        cursorLayer.prefWidthProperty().bind(scene.widthProperty());
        cursorLayer.prefHeightProperty().bind(scene.heightProperty());

        // Tạo custom cursor giống MainMenuApp
        gameCursor = new GameCursor(
                cursorLayer,
                scene
        );

        gameCursor.setSize(48, 48);
        gameCursor.setHotSpot(0, 0);

        return scene;
    }

    private StackPane createRoot() {
        StackPane root = new StackPane();

        root.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);

        return root;
    }

    private void setupBackground(StackPane root) {
        try {
            SettingMenu setting = new SettingMenu();
            BACKGROUND_IMAGE = setting.getBackgroundPath();

            var url = getClass().getResource(BACKGROUND_IMAGE);

            if (url == null) {
                throw new IllegalStateException("Không tìm thấy background: " + BACKGROUND_IMAGE);
            }

            BackgroundImage bgImage = new BackgroundImage(
                    new javafx.scene.image.Image(
                            url.toExternalForm(),
                            APP_WIDTH,
                            APP_HEIGHT,
                            false,
                            true
                    ),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(
                            APP_WIDTH,
                            APP_HEIGHT,
                            false,
                            false,
                            false,
                            false
                    )
            );

            root.setBackground(new Background(bgImage));

        } catch (Exception e) {
            System.err.println("[WARN] Không load được background: " + e.getMessage());

            root.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #090A1A, #1B1B3A, #3B185F);"
            );
        }
    }


    private void setupDarkOverlay(StackPane root) {
        Region overlay = new Region();

        overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);
        overlay.setMinSize(APP_WIDTH, APP_HEIGHT);
        overlay.setMaxSize(APP_WIDTH, APP_HEIGHT);

        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
        overlay.setMouseTransparent(true);

        root.getChildren().add(overlay);
    }

    private void setupLoadingPanel(StackPane root) {
        titleLabel = new Label("Đang kiểm tra tài nguyên...");
        titleLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;"
        );

        subLabel = new Label("Vui lòng chờ trong giây lát");
        subLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.75);" +
                        "-fx-font-size: 20px;"
        );

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(980);
        progressBar.setPrefHeight(22);
        progressBar.setStyle("-fx-accent: #8B5CF6;");

        percentLabel = new Label("0%");
        percentLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        detailLabel = new Label("Đang khởi tạo...");
        detailLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-font-size: 16px;"
        );
        detailLabel.setWrapText(true);
        detailLabel.setMaxWidth(1000);

        VBox textBox = new VBox(6, titleLabel, subLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox progressRow = new HBox(16, progressBar, percentLabel);
        progressRow.setAlignment(Pos.CENTER);

        VBox panelContent = new VBox(16, textBox, progressRow, detailLabel);
        panelContent.setAlignment(Pos.CENTER_LEFT);

        StackPane panel = new StackPane(panelContent);
        panel.setPadding(new Insets(28, 36, 28, 36));
        panel.setMaxWidth(1200);
        panel.setMaxHeight(250);

        panel.setStyle(
                "-fx-background-color: rgba(10, 10, 25, 0.78);" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: rgba(255,255,255,0.18);" +
                        "-fx-border-radius: 28;" +
                        "-fx-border-width: 1;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.55));
        shadow.setRadius(28);
        shadow.setOffsetY(8);

        panel.setEffect(shadow);

        StackPane.setAlignment(panel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(panel, new Insets(0, 0, 58, 0));

        root.getChildren().add(panel);
    }

    private void setupMuteButton(StackPane root) {
        muteButton = new Button("MUTE");
        muteButton.setFocusTraversable(false);

        // Cố định kích thước để 2 trạng thái BẬT/TẮT không bị lệch layout
        muteButton.setPrefSize(138, 46);
        muteButton.setMinSize(138, 46);
        muteButton.setMaxSize(138, 46);

        // Tránh lỗi cursor mặc định đè lên custom cursor
        muteButton.setCursor(Cursor.NONE);

        muteButton.setStyle(
                "-fx-background-color: rgba(10, 10, 25, 0.78);" +
                        "-fx-background-radius: 23;" +
                        "-fx-border-color: rgba(255,255,255,0.28);" +
                        "-fx-border-radius: 23;" +
                        "-fx-border-width: 1;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 0;"
        );

        muteButton.setOnAction(e -> toggleBackgroundMusic());

        StackPane.setAlignment(muteButton, Pos.TOP_RIGHT);
        StackPane.setMargin(muteButton, new Insets(24, 28, 0, 0));
        root.getChildren().add(muteButton);
    }


    private void toggleBackgroundMusic() {
        musicMuted = !musicMuted;

        if (musicMuted) {
            if (bgmPlayer != null) {
                bgmPlayer.pause();
            }
            updateMuteButtonText();
            return;
        }

        if (bgmPlayer == null) {
            startBackgroundMusic();
        } else {
            bgmPlayer.play();
        }
        updateMuteButtonText();
    }

    private void updateMuteButtonText() {
        if (muteButton != null) {
            muteButton.setText(musicMuted ? "UNMUTE" : "MUTE");
        }
    }

    private void startBackgroundMusic() {
        if (musicMuted) {
            updateMuteButtonText();
            return;
        }

        try {
            var url = getClass().getResource(BACKGROUND_MUSIC);

            if (url == null) {
                throw new IllegalStateException("Không tìm thấy nhạc nền: " + BACKGROUND_MUSIC);
            }

            Media media = new Media(url.toExternalForm());

            media.setOnError(() -> {
                MediaException e = media.getError();
                System.err.println("[WARN] Media background music error: "
                        + (e != null ? e.getMessage() : "unknown"));
            });

            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(0.35);

            bgmPlayer.setOnError(() -> {
                MediaException e = bgmPlayer.getError();
                System.err.println("[WARN] Background music player error: "
                        + (e != null ? e.getMessage() : "unknown"));
            });

            bgmPlayer.play();
            updateMuteButtonText();

        } catch (Exception e) {
            System.err.println("[WARN] Không phát được nhạc nền loading: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        try {
            if (bgmPlayer != null) {
                bgmPlayer.stop();
                bgmPlayer.dispose();
                bgmPlayer = null;
            }
        } catch (Exception e) {
            System.err.println("[WARN] Lỗi khi dừng nhạc nền: " + e.getMessage());
        }
    }

    private void checkResources(Stage checkerStage) {
        try {
            List<Album> albums = loadAlbums();

            int totalSongs = countSongs(albums);
            int totalDownloadSongs = countSongsNeedDownload(albums);

            int currentSong = 0;
            int currentDownloadSong = 0;

            for (Album album : albums) {
                checkLocalFile("Ảnh bìa album", album.getCoverPath());

                if (album.getSongs() == null || album.getSongs().isEmpty()) {
                    continue;
                }

                for (Song song : album.getSongs()) {
                    currentSong++;

                    String songName = getSongDisplayName(song);
                    String songDownloadName = getSongDisplayNameWithAuthor(song);

                    updateMainProgress(
                            "Đang kiểm tra tài nguyên",
                            "Kiểm tra bài hát: " + songName,
                            currentSong - 1,
                            totalSongs
                    );

                    checkLocalFile("Ảnh bài hát", song.getImagePath());

                    if (song.getVideoPath() == null || song.getVideoPath().isBlank()) {
                        System.err.println("[WARN] videoPath rỗng: " + songName);
                        continue;
                    }

                    Path runtimeVideoPath = ResourcePathUtil.resolveRuntimePath(song.getVideoPath());
                    Path bundledVideoPath = ResourcePathUtil.resolveBundledPath(song.getVideoPath());

                    boolean videoExists =
                            existsNonEmpty(runtimeVideoPath)
                                    || existsNonEmpty(bundledVideoPath);

                    if (videoExists) {
                        Path usedPath = existsNonEmpty(runtimeVideoPath)
                                ? runtimeVideoPath
                                : bundledVideoPath;

                        updateMainProgress(
                                "Video đã tồn tại",
                                "Sẵn sàng: " + usedPath,
                                currentSong,
                                totalSongs
                        );

                        continue;
                    }

                    if (song.getBackup() == null || song.getBackup().isBlank()) {
                        System.err.println("[WARN] Thiếu video và không có backup: " + song.getVideoPath());
                        continue;
                    }

                    currentDownloadSong++;

                    final int songIndex = currentSong;
                    final int downloadIndex = currentDownloadSong;

                    updateDownloadStart(songDownloadName, downloadIndex, totalDownloadSongs);

                    GoogleDriveDownloader.downloadFile(
                            song.getBackup(),
                            runtimeVideoPath,
                            (downloaded, total) -> updateDownloadProgress(
                                    songDownloadName,
                                    downloadIndex,
                                    totalDownloadSongs,
                                    downloaded,
                                    total
                            )
                    );

                    updateMainProgress(
                            "Tải xong tài nguyên",
                            "Đã lưu: " + runtimeVideoPath,
                            songIndex,
                            totalSongs
                    );
                }
            }

            updateMainProgress(
                    "Hoàn tất kiểm tra tài nguyên",
                    "Đang mở ứng dụng...",
                    totalSongs,
                    totalSongs
            );

            Thread.sleep(700);

            Platform.runLater(() -> openMainApp(checkerStage));

        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra tài nguyên: " + e.getMessage());

            Platform.runLater(() -> {
                titleLabel.setText("Có lỗi xảy ra");
                subLabel.setText("Không thể hoàn tất kiểm tra tài nguyên");
                detailLabel.setText(e.getMessage());
                percentLabel.setText("Lỗi");
                progressBar.setProgress(0);
            });
        }
    }

    private int countSongsNeedDownload(List<Album> albums) {
        int count = 0;

        for (Album album : albums) {
            if (album.getSongs() == null || album.getSongs().isEmpty()) {
                continue;
            }

            for (Song song : album.getSongs()) {
                if (song.getVideoPath() == null || song.getVideoPath().isBlank()) {
                    continue;
                }

                Path runtimeVideoPath = ResourcePathUtil.resolveRuntimePath(song.getVideoPath());
                Path bundledVideoPath = ResourcePathUtil.resolveBundledPath(song.getVideoPath());

                boolean videoExists =
                        existsNonEmptyNoThrow(runtimeVideoPath)
                                || existsNonEmptyNoThrow(bundledVideoPath);

                if (!videoExists && song.getBackup() != null && !song.getBackup().isBlank()) {
                    count++;
                }
            }
        }

        return Math.max(count, 1);
    }

    private List<Album> loadAlbums() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream(JSON_PATH);

        if (inputStream == null) {
            throw new IllegalStateException("Không tìm thấy file JSON: " + JSON_PATH);
        }

        try (InputStreamReader reader =
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            Type type = new TypeToken<List<Album>>() {}.getType();

            List<Album> albums = new Gson().fromJson(reader, type);

            if (albums == null) {
                throw new IllegalStateException("File JSON rỗng hoặc sai định dạng.");
            }

            return albums;
        }
    }

    private int countSongs(List<Album> albums) {
        int count = 0;

        for (Album album : albums) {
            if (album.getSongs() != null) {
                count += album.getSongs().size();
            }
        }

        return Math.max(count, 1);
    }

    private void checkLocalFile(String type, String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            System.err.println("[WARN] " + type + " rỗng.");
            return;
        }

        Path runtimePath = ResourcePathUtil.resolveRuntimePath(resourcePath);
        Path bundledPath = ResourcePathUtil.resolveBundledPath(resourcePath);

        boolean exists =
                existsNonEmptyNoThrow(runtimePath)
                        || existsNonEmptyNoThrow(bundledPath);

        if (!exists) {
            System.err.println("[WARN] Không tìm thấy " + type + ": " + resourcePath);
            System.err.println("       runtime: " + runtimePath);
            System.err.println("       bundled: " + bundledPath);
        }
    }

    private boolean existsNonEmpty(Path path) throws Exception {
        return path != null
                && Files.exists(path)
                && Files.isRegularFile(path)
                && Files.size(path) > 0;
    }

    private boolean existsNonEmptyNoThrow(Path path) {
        try {
            return existsNonEmpty(path);
        } catch (Exception e) {
            return false;
        }
    }

    private String getSongDisplayName(Song song) {
        if (song.getTitle() != null && !song.getTitle().isBlank()) {
            return song.getTitle();
        }

        if (song.getId() != null && !song.getId().isBlank()) {
            return song.getId();
        }

        return "Unknown Song";
    }

    private String getSongDisplayNameWithAuthor(Song song) {
        String title = getSongDisplayName(song);

        String author = "";
        if (song.getArtist() != null && !song.getArtist().isBlank()) {
            author = song.getArtist();
        }

        if (author.isBlank()) {
            return title;
        }

        return title + " - " + author;
    }

    private void updateMainProgress(
            String title,
            String detail,
            int current,
            int total
    ) {
        Platform.runLater(() -> {
            double progress = total <= 0 ? 1.0 : (double) current / total;
            progress = Math.max(0, Math.min(1, progress));

            titleLabel.setText(title);
            subLabel.setText("Đang chuẩn bị dữ liệu trò chơi");
            detailLabel.setText(detail);
            progressBar.setProgress(progress);
            percentLabel.setText(String.format("%.0f%%", progress * 100));
        });
    }

    private void updateDownloadStart(String songName, int current, int totalSongs) {
        Platform.runLater(() -> {
            titleLabel.setText("Đang tải tài nguyên (" + current + "/" + totalSongs + ")");
            subLabel.setText(songName);
            detailLabel.setText("Đang kết nối Cloud...");
            percentLabel.setText("Loading...");
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            if (!musicMuted && bgmPlayer == null) {
                startBackgroundMusic();
            }
        });
    }

    private void updateDownloadProgress(
            String songName,
            int current,
            int totalSongs,
            long downloaded,
            long total
    ) {
        Platform.runLater(() -> {
            titleLabel.setText("Đang tải tài nguyên (" + current + "/" + totalSongs + ")");
            subLabel.setText(songName);

            if (total > 0) {
                double progress = (double) downloaded / total;
                progress = Math.max(0, Math.min(1, progress));

                progressBar.setProgress(progress);
                percentLabel.setText(String.format("%.1f%%", progress * 100));

                detailLabel.setText(
                        formatSize(downloaded)
                                + " / "
                                + formatSize(total)
                );
            } else {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                percentLabel.setText("Loading...");
                detailLabel.setText(formatSize(downloaded));
            }
        });
    }

    private String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;

        if (mb >= 1024) {
            return String.format("%.2f GB", mb / 1024.0);
        }

        return String.format("%.2f MB", mb);
    }

    private void openMainApp(Stage checkerStage) {
        try {
            stopBackgroundMusic();

            if (gameCursor != null) {
                gameCursor.dispose();
                gameCursor = null;
            }

            checkerStage.close();

            Application mainApp = nextAppClass.getDeclaredConstructor().newInstance();

            Stage mainStage = new Stage();
            mainApp.start(mainStage);

        } catch (Exception e) {
            System.err.println("Lỗi khi mở ứng dụng chính: " + e.getMessage());

            titleLabel.setText("Không thể mở ứng dụng chính");
            subLabel.setText("Đã xảy ra lỗi khi khởi chạy");
            detailLabel.setText(e.getMessage());
            percentLabel.setText("Lỗi");
            progressBar.setProgress(0);
        }
    }

    @Override
    public void stop() {
        stopBackgroundMusic();

        if (gameCursor != null) {
            gameCursor.dispose();
            gameCursor = null;
        }
    }
}
