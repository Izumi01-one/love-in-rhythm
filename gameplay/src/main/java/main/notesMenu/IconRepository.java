package main.notesMenu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.model.IconItem;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IconRepository {

    private static final String ICON_DATA_JSON = "/data/icons.json";

    public List<IconItem> loadIcons() {
        Gson gson = new Gson();

        try (Reader reader = openJsonReader(ICON_DATA_JSON)) {
            if (reader == null) {
                System.out.println("Không tìm thấy file JSON: " + ICON_DATA_JSON);
                return new ArrayList<>();
            }

            Type listType = new TypeToken<List<IconItem>>() {}.getType();
            List<IconItem> icons = gson.fromJson(reader, listType);

            return icons == null ? new ArrayList<>() : icons;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Reader openJsonReader(String jsonPath) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream(jsonPath);

        if (inputStream != null) {
            return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        }

        File file = new File(jsonPath);

        if (file.exists()) {
            return new FileReader(file, StandardCharsets.UTF_8);
        }

        return null;
    }
}
