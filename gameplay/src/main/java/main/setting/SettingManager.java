package main.setting;

import main.model.GameSetting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingManager {

    private static final Path SETTING_DIR =
            Path.of(System.getProperty("user.home") + File.separator + "LoveInLove");

    private static final Path SETTING_FILE =
            SETTING_DIR.resolve("set.json");

    private GameSetting settings = new GameSetting();

    public GameSetting getSettings() {
        return settings;
    }

    public void loadSettings() {
        try {
            if (!Files.exists(SETTING_FILE)) {
                saveSettings();
                return;
            }

            String json = Files.readString(SETTING_FILE, StandardCharsets.UTF_8);

            settings.setFallSpeed(readStringValue(json, "fallSpeed", "NORMAL"));
            settings.setBackgroundName(readStringValue(json, "backgroundName", "SAO"));
            settings.setBackgroundPath(readStringValue(json, "backgroundPath", "/img/background/background02.png"));
            settings.setVolume(readIntValue(json, "volume", 70));
            settings.setCode(readStringValue(json, "code", ""));
            settings.setIconPath(readStringValue(json, "iconPath", "/img/icons/icon2.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSettings() {
        try {
            if (!Files.exists(SETTING_DIR)) {
                Files.createDirectories(SETTING_DIR);
            }

            String json = """
                {
                  "fallSpeed": "%s",
                  "backgroundName": "%s",
                  "backgroundPath": "%s",
                  "volume": %d,
                  "code": "%s",
                  "iconPath": "%s"
                }
                """.formatted(
                    escapeJson(settings.getFallSpeed()),
                    escapeJson(settings.getBackgroundName()),
                    escapeJson(settings.getBackgroundPath()),
                    settings.getVolume(),
                    escapeJson(settings.getCode()),
                    escapeJson(settings.getIconPath())
            );

            Files.writeString(SETTING_FILE, json, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readStringValue(String json, String key, String defaultValue) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);

        if (keyIndex == -1) {
            return defaultValue;
        }

        int colonIndex = json.indexOf(":", keyIndex);
        int firstQuote = json.indexOf("\"", colonIndex + 1);
        int secondQuote = findClosingQuote(json, firstQuote + 1);

        if (colonIndex == -1 || firstQuote == -1 || secondQuote == -1) {
            return defaultValue;
        }

        return unescapeJson(json.substring(firstQuote + 1, secondQuote));
    }

    private int readIntValue(String json, String key, int defaultValue) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);

        if (keyIndex == -1) {
            return defaultValue;
        }

        int colonIndex = json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return defaultValue;
        }

        int start = colonIndex + 1;

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;

        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }

        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int findClosingQuote(String text, int start) {
        boolean escaped = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\\' && !escaped) {
                escaped = true;
                continue;
            }

            if (c == '"' && !escaped) {
                return i;
            }

            escaped = false;
        }

        return -1;
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}