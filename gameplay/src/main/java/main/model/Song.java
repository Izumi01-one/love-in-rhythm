package main.model;

public class Song {
    private String id;
    private String title;
    private String artist;
    private String imagePath;
    private String videoPath;
    private int bestScore;
    private int easy;
    private int normal;
    private int advance;
    private String backup;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public int getBestScore() {
        return bestScore;
    }

    public int getEasy() {
        return easy;
    }

    public int getNormal() {
        return normal;
    }

    public int getAdvance() {
        return advance;
    }
    public String getBackup(){return backup;}
}