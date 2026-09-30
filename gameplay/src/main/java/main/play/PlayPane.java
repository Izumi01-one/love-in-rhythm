package main.play;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import main.costomCursor.GameCursor;
import main.model.Album;
import main.model.Song;
import main.score.ScoreManager;
import main.setting.SettingMenu;

public class PlayPane extends StackPane {

    private final List<Note> notes = new ArrayList<>();
    private final GameCanvas canvas;
    private final BackgroundPaneVLC bg;
    private PlayPausePane pauseMenu;
    private final SettingMenu setting;
    private final String songId;


    private final Stage stage;
    private final Album album;
    private final Song song;

    private int score = 0;
    private int combo = 0;
    private int lastCol = -1;
    private int status = -1;

    private double lastSpawn = 0;

    private final int level;
    private double speed = 5;
    private final int maxCols;

    private boolean paused = false;
    private boolean running = false;
    private boolean finished = false;
    private boolean countdownFinished = false;
    private CountdownPane countdownPane;

    private GameCursor gameCursor;
    private final Map<KeyCode, Integer> keyMap = new EnumMap<>(KeyCode.class);
    private final Random random = new Random();

    private AnimationTimer gameTimer;
    @SuppressWarnings("unused")
    private Consumer<Integer> onGameFinished;

    public PlayPane(Stage stage, Album album, Song song, String video, String note, int level, String songId) {
        this.stage = stage;
        this.album = album;
        this.song = song;

        this.level = Math.max(100, level);
        this.songId = songId;

        setting = new SettingMenu();
        setting.loadSettings();

        System.out.println("Level : " + this.level);

        if (level > 700) {
            maxCols = 2;
        } else if (level > 400) {
            maxCols = 3;
        } else {
            maxCols = 4;
        }

        speed = setting.getDifficultyProgress();

        bg = new BackgroundPaneVLC(video, 1400, 800);
        bg.setOnVideoFinished(this::end);

        canvas = new GameCanvas(notes);
        canvas.setNote(note);

        getChildren().addAll(bg, canvas);


        initKeyMap();
        initPauseMenu();
        input();


        setFocusTraversable(true);
        Platform.runLater(this::countdown);
    }

    private void initKeyMap() {
        keyMap.clear();

        keyMap.put(KeyCode.A, 0);
        keyMap.put(KeyCode.S, 1);
        keyMap.put(KeyCode.D, 2);
        keyMap.put(KeyCode.J, 3);
        keyMap.put(KeyCode.K, 4);
        keyMap.put(KeyCode.L, 5);
    }

    private void initPauseMenu() {
        pauseMenu = new PlayPausePane(
                this::resumeGame,
                this::exitGame
        );

        pauseMenu.setButtonSize(300, 50);
        pauseMenu.setButtonRadius(30);
        pauseMenu.setButtonGap(20);
        pauseMenu.setButtonImage("/img/PlayPause.jpg");

        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        getChildren().add(pauseMenu);
    }

    private void countdown() {
        running = false;
        countdownFinished = false;

        // cleanup nếu có cũ
        if (countdownPane != null) {
            countdownPane.stop();
            getChildren().remove(countdownPane);
        }

        gameCursor.setVisible(false);
        countdownPane = new CountdownPane(3, this::onCountdownFinished);

        countdownPane.prefWidthProperty().bind(widthProperty());
        countdownPane.prefHeightProperty().bind(heightProperty());

        getChildren().add(countdownPane);
        countdownPane.toFront();

        if (gameCursor != null) {
            gameCursor.setVisible(false);
        }

        countdownPane.start();
    }

    private void onCountdownFinished() {
        getChildren().remove(countdownPane);
        countdownPane = null;

        countdownFinished = true;
        running = true;
        paused = false;
        bg.playFromStart(false);
        startLoop();
        requestFocus();

        if (gameCursor != null) {
            gameCursor.setVisible(true);
        }
    }


    private void startLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };

        gameTimer.start();
    }

    private void updateGame() {
        if (!running || paused || finished || !countdownFinished) {
            return;
        }

        double time = bg.timeMs();
        double dur = bg.durationMs();

        if (time <= 0) {
            canvas.updateInfo(score, combo, status, time, dur);
            canvas.render();
            return;
        }

        spawnNoteIfNeeded(time);

        for (Note note : notes) {
            note.update(speed);
        }

        removeMissedNotes();

        canvas.updateInfo(score, combo, status, time, dur);
        canvas.render();

        if (dur > 0 && time >= dur - 100) {
            end();
        }
    }


    // spawn note theo độ khó
    private void spawnNoteIfNeeded(double time) {
        if (time - lastSpawn <= level) {
            return;
        }

        List<Integer> cols = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            if (i != lastCol) {
                cols.add(i);
            }
        }

        Collections.shuffle(cols, random);

        // Số note random từ 1 -> maxCols
        // Nhưng không được vượt quá số cột hiện có trong cols
        int limit = Math.min(maxCols, cols.size());
        int numberOfCols = random.nextInt(limit) + 1;

        for (int i = 0; i < numberOfCols; i++) {
            int col = cols.get(i);
            notes.add(new Note(col));
        }

        // Lưu cột cuối để lần sau tránh lặp lại
        lastCol = cols.get(0);

        lastSpawn = time;
    }

    private void removeMissedNotes() {
        notes.removeIf(note -> {
            if (note.out(GameCanvas.HIT + 5)) {
                status = 0;
                combo = 0;
                score = Math.max(0, score - 10);
                return true;
            }

            return false;
        });
    }

    // thuật toán nhấn phím
    private void input() {
        setFocusTraversable(true);

        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            if (code == KeyCode.SPACE) {
                if (!countdownFinished || finished) {
                    e.consume();
                    return;
                }

                if (paused) {
                    resumeGame();
                } else {
                    pauseGame();
                }

                e.consume();
                return;
            }

            if (paused || !running || finished || !countdownFinished) {
                e.consume();
                return;
            }

            Integer col = keyMap.get(code);

            if (col != null) {
                canvas.keyPressed(code);
                hit(col);
                e.consume();
            }
        });

        addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            KeyCode code = e.getCode();

            Integer col = keyMap.get(code);

            if (col != null) {
                canvas.keyReleased(code);
                e.consume();
            }
        });
    }

    // tạm dừng
    private void pauseGame() {
        if (finished || paused) {
            return;
        }

        paused = true;

        bg.pause();

        pauseMenu.setVisible(true);
        pauseMenu.setManaged(true);
        pauseMenu.toFront();

        requestFocus();
    }

    // tiếp tục
    private void resumeGame() {
        if (finished || !paused) {
            return;
        }

        paused = false;

        pauseMenu.setVisible(false);
        pauseMenu.setManaged(false);

        bg.resume();

        requestFocus();
    }

    private void exitGame() {
        finishGame();
    }


    // thuật toán tính điểm
    private void hit(int col) {
        Iterator<Note> iterator = notes.iterator();

        while (iterator.hasNext()) {
            Note note = iterator.next();

            if (note.getCol() != col) {
                continue;
            }

            if (note.hittable(GameCanvas.HIT)) {
                combo++;

                if (combo >= 10) {
                    score += (combo / 10) * 10;
                } else {
                    score += 10;
                }

                status = 1;
            } else {
                combo = 0;
                score = Math.max(0, score - 10);
                status = 0;
            }

            iterator.remove();
            return;
        }

        // Nếu bấm sai cột khi không có note nào thì có thể trừ điểm nhẹ.
        // Nếu không muốn trừ điểm khi bấm hụt, hãy xóa đoạn này.
        combo = 0;
        score = Math.max(0, score - 5);
        status = 0;
    }

    public void setOnGameFinished(Consumer<Integer> onGameFinished) {
        this.onGameFinished = onGameFinished;
    }


    // kết thúc game
    private void finishGame() {
        if (finished) {
            return;
        }

        finished = true;
        running = false;
        paused = false;

        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }

        try {
            bg.stop();
        } catch (Exception e) {
            System.out.println("Lỗi khi stop background.");
            System.err.println("Chi tiết: " + e.getMessage());
        }

        try {
            int oldBestScore = ScoreManager.getBestScore(songId);

            if (score > oldBestScore) {
                ScoreManager.updateBestScoreIfHigher(songId, score);
                System.out.println("New Best Score: " + score);
            } else {
                System.out.println("Best Score giữ nguyên: " + oldBestScore);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu best score.");
            System.err.println("Chi tiết: " + e.getMessage());
        }



        try {
            getChildren().remove(bg);
            bg.dispose();
        } catch (Exception e) {
            System.out.println("Lỗi khi dispose background.");
            System.err.println("Chi tiết: " + e.getMessage());
        }

        int finalScore = score;

        Platform.runLater(() -> {
            showResultPane(finalScore);
        });

    }
    private void showResultPane(int finalScore) {
        GameResultPane resultPane = new GameResultPane(stage, album, song, finalScore);

        // Không dùng setManaged(false)
        resultPane.setManaged(true);

        // Cho overlay phủ toàn bộ PlayPane
        resultPane.prefWidthProperty().bind(widthProperty());
        resultPane.prefHeightProperty().bind(heightProperty());

        resultPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane.setAlignment(resultPane, Pos.CENTER);

        getChildren().add(resultPane);
        resultPane.toFront();
        resultPane.requestFocus();
    }

    public void setGameCursor(GameCursor gameCursor) {
        this.gameCursor = gameCursor;

        if (!countdownFinished && gameCursor != null) {
            gameCursor.setVisible(false);
        }
    }



    public void end() {
        finishGame();
    }
}