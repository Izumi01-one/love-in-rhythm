package main.play;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import main.checkResource.ResourcePathUtil;
import main.setting.SettingMenu;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class BackgroundPaneVLC extends StackPane {

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer player;

    private ImageView videoView;
    private Rectangle darkOverlay;

    private final String mp4Path;
    private String mediaMrl;

    private boolean playRequested = false;
    private boolean retrying = false;
    private boolean disposed = false;
    @SuppressWarnings("unused")
    private boolean pausedByUser = false;

    private volatile boolean loopEnabled = false;
    private volatile boolean stopRequested = false;

    private int retryCount = 0;
    private int volumePercent = 100;

    private static final int MAX_RETRY = 3;
    private static final double RETRY_DELAY_SECONDS = 1.0;

    private Runnable onVideoFinished;

    public BackgroundPaneVLC(String mp4, double width, double height) {
        this.mp4Path = mp4;

        setPrefSize(width, height);
        setStyle("-fx-background-color: black;");

        setupOverlay();
        loadSettingVolume();
        initVlcFactory();
        loadVideo();
    }

    private void loadSettingVolume() {
        try {
            SettingMenu settingMenu = new SettingMenu();
            settingMenu.loadSettings();

            volumePercent = settingMenu.getVolume();
            volumePercent = Math.max(0, Math.min(100, volumePercent));
        } catch (Exception e) {
            System.out.println("Không thể đọc volume từ SettingMenu, dùng mặc định 100%.");
            volumePercent = 100;
        }
    }

    private void initVlcFactory() {
        if (disposed || mediaPlayerFactory != null) {
            return;
        }

        try {
            mediaPlayerFactory = new MediaPlayerFactory(
                    "--no-video-title-show",
                    "--quiet",
                    "--no-snapshot-preview"
            );

            System.out.println("Khởi tạo VLCJ MediaPlayerFactory thành công.");
        } catch (Exception e) {
            System.out.println("Không thể khởi tạo VLCJ MediaPlayerFactory.");
            System.out.println("Hãy kiểm tra đã cài VLC đúng bản 32-bit/64-bit với Java.");
            System.err.println("Chi tiết lỗi: " + e.getMessage());

            retryLoadVideo("Không khởi tạo được MediaPlayerFactory");
        }
    }

    private void loadVideo() {
        if (disposed || stopRequested) {
            return;
        }

        try {
            disposePlayerOnly();

            mediaMrl = loadMediaMrl(mp4Path);

            if (mediaMrl == null) {
                retryLoadVideo("mediaMrl null");
                return;
            }

            if (mediaPlayerFactory == null) {
                initVlcFactory();

                if (mediaPlayerFactory == null) {
                    retryLoadVideo("mediaPlayerFactory null");
                    return;
                }
            }

            System.out.println("VLC MEDIA MRL = " + mediaMrl);

            setupVideoView();

            player = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
            player.videoSurface().set(new ImageViewVideoSurface(videoView));
            player.audio().setVolume(volumePercent);

            setupPlayerEvents();

            getChildren().clear();
            getChildren().addAll(videoView, darkOverlay);

            if (playRequested) {
                playInternalFromStart();
            }
        } catch (Exception e) {
            System.out.println("Không thể load background video bằng VLCJ: " + mp4Path);
            System.err.println("Chi tiết: " + e.getMessage());

            retryLoadVideo("Exception khi load video");
        }
    }

    private void setupVideoView() {
        videoView = new ImageView();

        videoView.fitWidthProperty().bind(widthProperty());
        videoView.fitHeightProperty().bind(heightProperty());

        videoView.setPreserveRatio(false);
        videoView.setSmooth(true);
    }

    private void setupPlayerEvents() {
        if (player == null) {
            return;
        }

        player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("VLC PLAYER PLAYING");

                retryCount = 0;
                retrying = false;
                playRequested = false;
                stopRequested = false;
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                System.out.println("VLC PLAYER PAUSED");
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                System.out.println("VLC PLAYER STOPPED");
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.out.println("VLC PLAYER FINISHED");

                pausedByUser = false;

                if (disposed || stopRequested) {
                    return;
                }

                // Nếu không bật loop, nghĩa là bài hát/game đã kết thúc
                if (!loopEnabled) {
                    Platform.runLater(() -> {
                        if (!disposed && !stopRequested && onVideoFinished != null) {
                            onVideoFinished.run();
                        }
                    });
                    return;
                }

                // Nếu có bật loop thì phát lại
                Platform.runLater(() -> {
                    if (disposed || stopRequested || !loopEnabled) {
                        return;
                    }

                    try {
                        if (player != null && mediaMrl != null) {
                            player.controls().setTime(0);
                            player.media().play(mediaMrl);
                            player.audio().setVolume(volumePercent);
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi khi loop video VLCJ.");
                        System.err.println("Chi tiết: " + e.getMessage());
                        retryLoadVideo("Loop video lỗi");
                    }
                });
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.out.println("VLC PLAYER ERROR");
                retryLoadVideo("VLC player error");
            }
        });
    }

    private void retryLoadVideo(String reason) {
        if (disposed || stopRequested || retrying) {
            return;
        }

        retrying = true;

        Platform.runLater(() -> {
            if (disposed || stopRequested) {
                retrying = false;
                return;
            }

            if (retryCount >= MAX_RETRY) {
                System.out.println("Đã thử load lại video tối đa " + MAX_RETRY + " lần. Dừng retry.");
                retrying = false;
                return;
            }

            boolean shouldPlayAfterRetry = playRequested || isPlaying();

            retryCount++;

            System.out.println("Thử load lại video bằng VLCJ lần " + retryCount + "/" + MAX_RETRY);
            System.out.println("Lý do retry: " + reason);

            playRequested = shouldPlayAfterRetry;

            PauseTransition delay = new PauseTransition(Duration.seconds(RETRY_DELAY_SECONDS));

            delay.setOnFinished(e -> {
                try {
                    if (disposed || stopRequested) {
                        return;
                    }

                    disposePlayerOnly();
                    loadVideo();
                } finally {
                    retrying = false;
                }
            });

            delay.play();
        });
    }

    private void setupOverlay() {
        darkOverlay = new Rectangle();

        darkOverlay.widthProperty().bind(widthProperty());
        darkOverlay.heightProperty().bind(heightProperty());

        darkOverlay.setFill(Color.rgb(0, 0, 0, 0.35));
        darkOverlay.setMouseTransparent(true);
    }

    private String loadMediaMrl(String mp4) {
        if (mp4 == null || mp4.isBlank()) {
            System.out.println("Đường dẫn video rỗng.");
            return null;
        }

        try {
            Path videoPath = ResourcePathUtil.resolveVideoPath(mp4);

            System.out.println("Video path resolved = " + videoPath);
            System.out.println("Video exists        = " + Files.exists(videoPath));

            if (!Files.exists(videoPath)) {
                System.out.println("Không tìm thấy file video: " + videoPath);
                return null;
            }

            long size = Files.size(videoPath);

            System.out.println("Video size          = " + size + " bytes");

            if (size <= 0) {
                System.out.println("File video bị rỗng: " + videoPath);
                return null;
            }

            return videoPath.toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("Lỗi khi resolve video path: " + mp4);
            System.err.println("Chi tiết: " + e.getMessage());
            return null;
        }
    }


    public void playFromStart(boolean loop) {
        if (disposed) {
            return;
        }

        playRequested = true;
        pausedByUser = false;
        // loopEnable = true -> lặp, false -> chỉ chạy 1 lần
        loopEnabled = loop;
        stopRequested = false;

        if (player == null) {
            retryLoadVideo("playFromStart nhưng player null");
            return;
        }

        playInternalFromStart();
    }

    private void playInternalFromStart() {
        if (disposed || player == null || mediaMrl == null) {
            return;
        }

        try {
            pausedByUser = false;
            stopRequested = false;

            player.controls().setTime(0);
            player.media().play(mediaMrl);
            player.audio().setVolume(volumePercent);

            playRequested = false;
        } catch (Exception e) {
            System.out.println("Không thể play video từ đầu bằng VLCJ.");
            System.err.println("Chi tiết: " + e.getMessage());

            retryLoadVideo("playInternalFromStart lỗi");
        }
    }

    public void setOnVideoFinished(Runnable onVideoFinished) {
        this.onVideoFinished = onVideoFinished;
    }

    public void pause() {
        if (disposed || player == null) {
            return;
        }

        playRequested = false;
        pausedByUser = true;

        try {
            if (player.status().isPlaying()) {
                player.controls().pause();
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi pause VLCJ player.");
            System.err.println("Chi tiết: " + e.getMessage());
        }
    }

    public void resume() {
        if (disposed) {
            return;
        }

        playRequested = true;
        stopRequested = false;

        if (player == null) {
            retryLoadVideo("resume nhưng player null");
            return;
        }

        try {
            if (mediaMrl == null) {
                return;
            }

            player.controls().play();
            player.audio().setVolume(volumePercent);

            pausedByUser = false;
            playRequested = false;
        } catch (Exception e) {
            System.out.println("Không thể resume VLCJ player.");
            System.err.println("Chi tiết: " + e.getMessage());

            retryLoadVideo("resume lỗi");
        }
    }

    public void stop() {
        playRequested = false;
        pausedByUser = false;
        loopEnabled = false;
        stopRequested = true;

        if (player == null) {
            return;
        }

        try {
            player.controls().stop();
        } catch (Exception e) {
            System.out.println("Lỗi khi stop VLCJ player.");
            System.err.println("Chi tiết: " + e.getMessage());
        }
    }

    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        playRequested = false;
        pausedByUser = false;
        retrying = false;
        retryCount = 0;
        loopEnabled = false;
        stopRequested = true;

        disposePlayerOnly();

        try {
            if (mediaPlayerFactory != null) {
                mediaPlayerFactory.release();
                mediaPlayerFactory = null;
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi release MediaPlayerFactory.");
            System.err.println("Chi tiết: " + e.getMessage());
        }
    }

    private void disposePlayerOnly() {
        try {
            if (player != null) {
                try {
                    player.controls().stop();
                } catch (Exception ignored) {
                }

                try {
                    player.release();
                } catch (Exception ignored) {
                }

                player = null;
            }

            if (videoView != null) {
                try {
                    videoView.fitWidthProperty().unbind();
                    videoView.fitHeightProperty().unbind();
                    videoView.imageProperty().unbind();
                } catch (Exception ignored) {
                }

                getChildren().remove(videoView);

                try {
                    videoView.setImage(null);
                } catch (Exception ignored) {
                }

                videoView = null;
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi dispose VLCJ player cũ.");
            System.err.println("Chi tiết: " + e.getMessage());
        }
    }

    private boolean isPlaying() {
        try {
            return player != null && player.status().isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public double timeMs() {
        if (player == null) {
            return 0;
        }

        try {
            return Math.max(0, player.status().time());
        } catch (Exception e) {
            return 0;
        }
    }

    public double durationMs() {
        if (player == null) {
            return 0;
        }

        try {
            long length = player.status().length();
            return Math.max(0, length);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setRuntimeVolume(int volume) {
        int safeVolume = Math.max(0, Math.min(100, volume));

        try {
            if (player != null) {
                player.audio().setVolume(safeVolume);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi set volume VLCJ.");
            System.err.println("Chi tiết: " + e.getMessage());
        }
    }
}