package main.score;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ScoreManager {

    private static final String SAVE_FOLDER =
            System.getProperty("user.home") + File.separator + "LoveInLove";

    private static final String SAVE_FILE =
            SAVE_FOLDER + File.separator + "player_save.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static int getBestScore(String songId) {
        JsonObject root = loadSaveFile();

        if (!root.has("scores")) {
            return 0;
        }

        JsonObject scores = root.getAsJsonObject("scores");

        if (!scores.has(songId)) {
            return 0;
        }

        JsonObject songScore = scores.getAsJsonObject(songId);

        if (!songScore.has("bestScore")) {
            return 0;
        }

        return songScore.get("bestScore").getAsInt();
    }

    public static String getRank(String songId) {
        JsonObject root = loadSaveFile();
        if (!root.has("scores")) {
            return "-";
        }

        JsonObject scores = root.getAsJsonObject("scores");
        if (!scores.has(songId)) {
            return "-";
        }

        JsonObject songScore = scores.getAsJsonObject(songId);
        if (songScore.has("rank")) {
            return songScore.get("rank").getAsString();
        }

        if (songScore.has("bestScore")) {
            return calculateRank(songScore.get("bestScore").getAsInt());
        }

        return "-";
    }

    public static void updateBestScoreIfHigher(String songId, int newScore) {
        JsonObject root = loadSaveFile();

        if (!root.has("playerName")) {
            root.addProperty("playerName", "lowiro");
        }

        if (!root.has("scores")) {
            root.add("scores", new JsonObject());
        }

        JsonObject scores = root.getAsJsonObject("scores");

        JsonObject songScore;

        if (scores.has(songId)) {
            songScore = scores.getAsJsonObject(songId);
        } else {
            songScore = new JsonObject();
            scores.add(songId, songScore);
        }

        int oldBest = 0;

        if (songScore.has("bestScore")) {
            oldBest = songScore.get("bestScore").getAsInt();
        }

        if (newScore > oldBest) {
            songScore.addProperty("bestScore", newScore);
            songScore.addProperty("cleared", true);
            songScore.addProperty("rank", calculateRank(newScore));

            saveFile(root);

            System.out.println("Cập nhật điểm mới: " + newScore);
        } else {
            System.out.println("Điểm cũ cao hơn hoặc bằng: " + oldBest);
        }
    }

    private static JsonObject loadSaveFile() {
        try {
            File folder = new File(SAVE_FOLDER);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(SAVE_FILE);

            if (!file.exists()) {
                JsonObject root = createDefaultSave();
                saveFile(root);
                return root;
            }

            try (Reader reader = new InputStreamReader(
                    new FileInputStream(file),
                    StandardCharsets.UTF_8
            )) {
                JsonObject root = gson.fromJson(reader, JsonObject.class);

                if (root == null) {
                    return createDefaultSave();
                }

                return root;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultSave();
        }
    }

    private static void saveFile(JsonObject root) {
        try {
            File folder = new File(SAVE_FOLDER);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(SAVE_FILE),
                    StandardCharsets.UTF_8
            )) {
                gson.toJson(root, writer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonObject createDefaultSave() {
        JsonObject root = new JsonObject();
        root.addProperty("playerName", "lowiro");
        root.addProperty("fragments", 1000);
        root.addProperty("memories", 1000);
        root.add("scores", new JsonObject());
        return root;
    }

    private static String calculateRank(int score) {
        if (score >= 50000) return "EX+";
        if (score >= 30000) return "EX";
        if (score >= 10000) return "AA";
        if (score >= 8000) return "A";
        if (score >= 5000) return "B";
        return "C";
    }

    public int getTotalPlayedSongs() {
        JsonObject root = loadSaveFile();
        if (!root.has("scores")) return 0;

        JsonObject scores = root.getAsJsonObject("scores");
        int count = 0;

        for (String key : scores.keySet()) {
            JsonObject song = scores.getAsJsonObject(key);
            if (song.has("cleared") && song.get("cleared").getAsBoolean()) {
                count++;
            }
        }
        return count;
    }

    public int getHighestScoreAllSongs() {
        JsonObject root = loadSaveFile();
        if (!root.has("scores")) return 0;

        JsonObject scores = root.getAsJsonObject("scores");
        int max = 0;

        for (String key : scores.keySet()) {
            JsonObject song = scores.getAsJsonObject(key);
            if (song.has("bestScore")) {
                int score = song.get("bestScore").getAsInt();
                if (score > max) {
                    max = score;
                }
            }
        }
        return max;
    }
}