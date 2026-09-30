package main.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.model.Album;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AlbumService {

    public static List<Album> loadAlbums() {
        try {
            InputStream is = AlbumService.class.getResourceAsStream("/data/albums.json");

            if (is == null) {
                System.out.println("Không tìm thấy file /data/albums.json");
                return new ArrayList<>();
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            Type listType = new TypeToken<List<Album>>() {}.getType();

            return new Gson().fromJson(reader, listType);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}