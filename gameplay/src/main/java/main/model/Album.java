package main.model;

import java.util.List;

public class Album {
    private String id;
    private String album;
    private String coverPath;
    private List<Song> songs;

    public String getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public List<Song> getSongs() {
        return songs;
    }
}