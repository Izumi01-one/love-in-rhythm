package main.play;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UniKeyController {

    private static final String PROCESS_NAME = "UniKeyNT.exe";
    private static String uniKeyPath = null;

    // Lấy đường dẫn UniKey bằng WMIC
    public static String getUniKeyPath() {
        try {
            Process process = new ProcessBuilder(
                    "cmd", "/c",
                    "wmic process where name='" + PROCESS_NAME + "' get ExecutablePath"
            ).start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(":\\") && line.endsWith(".exe")) {
                    return line;
                }
            }

        } catch (Exception e) {
            System.out.println("Cannot get path UniKey by WMIC");
        }
        return null;
    }

    // Tắt UniKey
    public static void killUniKey() {
        try {
            new ProcessBuilder("cmd", "/c",
                    "taskkill /IM " + PROCESS_NAME + " /F"
            ).start();
            System.out.println("Closed UniKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Mở lại UniKey
    public static void startUniKey() {
        if (uniKeyPath == null) {
            System.out.println("No path UniKey for reload");
            return;
        }

        try {
            new ProcessBuilder("cmd", "/c", "\"" + uniKeyPath + "\"").start();
            System.out.println("Reload UniKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Setup toàn bộ logic
    public static void init() {
        // Lấy path trước khi kill
        uniKeyPath = getUniKeyPath();
        System.out.println("UniKey path: " + uniKeyPath);

        // Tắt UniKey
        killUniKey();

        // Khi tắt chương trình -> bật lại
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Game exit...");
            startUniKey();
        }));
    }
}