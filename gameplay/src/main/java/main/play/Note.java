package main.play;

public class Note {
    private final int col;
    private double y = -100;

    public Note(int col) {
        this.col = col;
    }

    public void update(double speed) { y += speed; }
    public boolean out(double limit) { return y > limit; }
    public boolean hittable(double hit) { return Math.abs(y - hit) <= 100; }

    public int getCol() { return col; }
    public double getY() { return y; }
}
