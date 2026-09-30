package main.play;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.Objects;

public class GameCanvas extends Canvas {

    private final List<Note> notes;
    private Image noteImg;
    private int score, combo, status;
    private double time, duration;
    private int percentSong;

    private final int cols = 6;
    private final int col_width = 160;
    private final int note_width = 100;
    private final int note_height = 100;
    private final int hit_line_y = 600;
    private final String[] KEYS = {"A", "S", "D", "J", "K", "L"};

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public static final double W = 1400, H = 800, HIT = 600;

    public GameCanvas(List<Note> notes) {
        super(W, H);
        this.notes = notes;
    }

    public void setNote(String img) {
        noteImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(img)));
    }

    public void keyPressed(KeyCode key) {
        pressedKeys.add(key);
    }

    public void keyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }

    private boolean isLanePressed(int index) {
        switch (index) {
            case 0:
                return pressedKeys.contains(KeyCode.A);
            case 1:
                return pressedKeys.contains(KeyCode.S);
            case 2:
                return pressedKeys.contains(KeyCode.D);
            case 3:
                return pressedKeys.contains(KeyCode.J);
            case 4:
                return pressedKeys.contains(KeyCode.K);
            case 5:
                return pressedKeys.contains(KeyCode.L);
            default:
                return false;
        }
    }

    public void updateInfo(int score, int combo, int status, double time, double duration) {
        this.score = score;
        this.combo = combo;
        this.status = status;
        this.time = time;
        this.duration = duration;
    }

    public void render() {
        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, W, H);

        double first = (W - 6 * 160) / 2 + 25;

        // Hiệu ứng khung vàng khi có combo
        if (combo >= 10) {
            drawComboFrameGlow(g);
        }


        // Neon mờ chạy phía sau lane
        drawNeonLaneBackground(g, first);

        // Cột được nhấn sáng mạnh hơn
        drawPressedLaneGlow(g, first);

        // Line lane gốc
        g.setStroke(Color.WHITE);
        g.setLineWidth(3);
        for (int i = 0; i < cols; i++) {
            g.strokeLine(first + i * 160 + 50, 0, first + i * 160 + 50, H - 100);
        }


        // Phím
        //g.setFill(Color.WHITE);
        g.setFont(Font.font(24));

        for (int i = 0; i < cols; i++) {
            String key = KEYS[i];

            double x = first + i * col_width + 40;
            double y = hit_line_y + 150;

            if (isLanePressed(i)) {
                drawKeyGlow(g, x + 10, y - 10);
                drawNeonText(g, key, x, y, Color.YELLOW, Color.rgb(255, 230, 40, 0.9), 28
                );
            }
            else {
                drawNeonText(g, key, x, y, Color.WHITE, Color.rgb(0, 220, 255, 0.45), 24
                );
            }
        }

        // Hit line glow
        drawHitLineGlow(g, first);

        g.setStroke(Color.YELLOW);
        g.setLineWidth(5);
        g.strokeLine(first + 50, HIT, first + 5 * 160 + 50, HIT);

        // Note giữ nguyên
        for (Note n : notes) {
            g.drawImage(
                    noteImg,
                    first + n.getCol() * 160,
                    n.getY(),
                    note_width,
                    note_height
            );
        }

        // UI neon
        drawUI(g);
    }

    private void drawUI(GraphicsContext g) {
        g.setFont(Font.font(24));

        drawNeonText(
                g,
                "Score: " + score,
                20,
                30,
                Color.WHITE,
                Color.rgb(0, 220, 255, 0.75),
                24
        );


        if(duration > 0){
            percentSong = (int)(time / duration * 100);
        }


        drawNeonText(
                g,
                String.format("%d%%",percentSong),
                1200,
                30,
                Color.WHITE,
                Color.rgb(0, 220, 255, 0.75),
                24
        );

        drawNeonText(
                g,
                "Tap Space for exit",
                1150,
                60,
                Color.WHITE,
                Color.rgb(255, 80, 220, 0.65),
                24
        );

        if (status == 1) {
            drawNeonText(
                    g,
                    "Perfect",
                    W / 2 - 50,
                    450,
                    Color.YELLOW,
                    Color.rgb(255, 230, 40, 0.85),
                    28
            );
        }

        if (status == 0) {
            drawNeonText(
                    g,
                    "Miss",
                    W / 2 - 40,
                    450,
                    Color.RED,
                    Color.rgb(255, 40, 40, 0.90),
                    28
            );
        }

        if (combo >= 10) {
            drawNeonText(
                    g,
                    "COMBO " + combo,
                    W / 2 - 80,
                    120,
                    Color.YELLOW,
                    Color.rgb(255, 230, 40, 0.90),
                    32
            );
        }
    }

    private void drawNeonText(GraphicsContext g,
                              String text,
                              double x,
                              double y,
                              Color textColor,
                              Color glowColor,
                              int fontSize) {
        g.save();
        g.setFont(Font.font(fontSize));

        // Glow ngoài
        g.setFill(glowColor);
        g.fillText(text, x - 2, y);
        g.fillText(text, x + 2, y);
        g.fillText(text, x, y - 2);
        g.fillText(text, x, y + 2);

        // Glow mạnh hơn sát chữ
        g.setFill(Color.color(
                glowColor.getRed(),
                glowColor.getGreen(),
                glowColor.getBlue(),
                0.75
        ));
        g.fillText(text, x - 1, y);
        g.fillText(text, x + 1, y);
        g.fillText(text, x, y - 1);
        g.fillText(text, x, y + 1);

        // Chữ chính
        g.setFill(textColor);
        g.fillText(text, x, y);

        g.restore();
    }

    private void drawKeyGlow(GraphicsContext g, double x, double y) {
        g.save();

        // Vòng sáng ngoài
        g.setFill(Color.rgb(255, 230, 40, 0.18));
        g.fillOval(x - 35, y - 35, 70, 70);

        // Vòng sáng trong
        g.setFill(Color.rgb(255, 230, 40, 0.30));
        g.fillOval(x - 24, y - 24, 48, 48);

        // Viền neon
        g.setStroke(Color.rgb(255, 255, 120, 0.85));
        g.setLineWidth(3);
        g.strokeOval(x - 28, y - 28, 56, 56);

        g.restore();
    }

    private void drawComboFrameGlow(GraphicsContext g) {
        g.save();

        /*
         * Khung vàng nhạt dần vào trong.
         * Vẽ nhiều lớp viền lớn -> nhỏ với alpha giảm dần.
         */
        for (int i = 0; i < 12; i++) {
            double alpha = 0.13 - i * 0.008;
            if (alpha < 0) alpha = 0;

            g.setStroke(Color.rgb(255, 220, 40, alpha));
            g.setLineWidth(18 - i);

            double margin = i * 10;
            g.strokeRect(
                    margin,
                    margin,
                    W - margin * 2,
                    H - margin * 2
            );
        }

        // Viền ngoài sáng hơn một chút
        g.setStroke(Color.rgb(255, 230, 80, 0.35));
        g.setLineWidth(4);
        g.strokeRect(4, 4, W - 8, H - 8);

        g.restore();
    }

    private void drawNeonLaneBackground(GraphicsContext g, double first) {
        g.save();

        double anim = (System.nanoTime() / 1_000_000_000.0) * 220;
        double movingY = anim % (H + 300) - 300;

        for (int i = 0; i < cols; i++) {
            double centerX = first + i * col_width + 50;

            Color neonColor = getLaneNeonColor(i);

            // Glow rộng phía sau line
            g.setStroke(Color.color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    0.10
            ));
            g.setLineWidth(60);
            g.strokeLine(centerX, 0, centerX, H - 100);

            g.setStroke(Color.color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    0.15
            ));
            g.setLineWidth(35);
            g.strokeLine(centerX, 0, centerX, H - 100);

            // Glow vừa
            g.setStroke(Color.color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    0.18
            ));
            g.setLineWidth(18);
            g.strokeLine(centerX, 0, centerX, H - 100);

            // Vệt sáng chạy mờ phía sau
            g.setStroke(Color.color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    0.45
            ));
            g.setLineWidth(26);
            g.strokeLine(centerX, movingY - i * 45, centerX, movingY + 150 - i * 45);

            // Lõi sáng nhỏ
            g.setStroke(Color.color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    0.55
            ));
            g.setLineWidth(5);
            g.strokeLine(centerX, 0, centerX, H - 100);
        }

        g.restore();
    }

    private void drawPressedLaneGlow(GraphicsContext g, double first) {
        g.save();

        for (int i = 0; i < cols; i++) {
            if (!isLanePressed(i)) continue;

            double centerX = first + i * col_width + 50;

            // Glow rộng nhất của cột
            g.setStroke(Color.rgb(255, 230, 40, 0.16));
            g.setLineWidth(50);
            g.strokeLine(centerX, 0, centerX, H - 100);

            // Glow giữa
            g.setStroke(Color.rgb(255, 240, 80, 0.25));
            g.setLineWidth(40);
            g.strokeLine(centerX, 0, centerX, H - 100);

            // Glow gần line
            g.setStroke(Color.rgb(255, 255, 150, 0.40));
            g.setLineWidth(26);
            g.strokeLine(centerX, 0, centerX, H - 100);

            // Lõi sáng mạnh ngay giữa cột
            g.setStroke(Color.rgb(255, 255, 220, 0.85));
            g.setLineWidth(7);
            g.strokeLine(centerX, 0, centerX, H - 100);

            // Sáng mạnh ở vùng gần hit line
            g.setFill(Color.rgb(255, 230, 40, 0.22));
            g.fillOval(centerX - 55, HIT - 55, 110, 110);

            g.setFill(Color.rgb(255, 255, 180, 0.30));
            g.fillOval(centerX - 35, HIT - 35, 70, 70);
        }

        g.restore();
    }

    private void drawHitLineGlow(GraphicsContext g, double first) {
        g.save();

        double startX = first + 50;
        double endX = first + 5 * 160 + 50;

        g.setStroke(Color.rgb(255, 230, 40, 0.18));
        g.setLineWidth(24);
        g.strokeLine(startX, HIT, endX, HIT);

        g.setStroke(Color.rgb(255, 255, 120, 0.28));
        g.setLineWidth(12);
        g.strokeLine(startX, HIT, endX, HIT);

        g.restore();
    }

    private Color getLaneNeonColor(int i) {
        switch (i) {
            case 0:
                return Color.rgb(0, 230, 255);
            case 1:
                return Color.rgb(255, 80, 220);
            case 2:
                return Color.rgb(255, 240, 70);
            case 3:
                return Color.rgb(80, 255, 180);
            case 4:
                return Color.rgb(120, 120, 255);
            case 5:
                return Color.rgb(255, 130, 60);
            default:
                return Color.WHITE;
        }
    }
}