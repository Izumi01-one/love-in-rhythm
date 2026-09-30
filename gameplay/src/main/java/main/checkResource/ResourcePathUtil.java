package main.checkResource;

import java.nio.file.Files;
import java.nio.file.Path;

// chèn file vừa tải vào luồng của game
public class ResourcePathUtil {

    private static final Path RUNTIME_RESOURCE_ROOT =
            Path.of(System.getProperty("user.dir"), "resources_runtime");

    public static Path resolveRuntimePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath rỗng.");
        }

        String cleanPath = cleanPath(resourcePath);

        return RUNTIME_RESOURCE_ROOT.resolve(cleanPath);
    }

    public static Path resolveBundledPath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath rỗng.");
        }

        String cleanPath = cleanPath(resourcePath);

        Path targetPath = Path.of(
                System.getProperty("user.dir"),
                "target",
                "classes"
        ).resolve(cleanPath);

        if (Files.exists(targetPath)) {
            return targetPath;
        }

        return Path.of(
                System.getProperty("user.dir"),
                "src",
                "main",
                "resources"
        ).resolve(cleanPath);
    }

    public static Path resolveVideoPath(String resourcePath) {
        Path runtimePath = resolveRuntimePath(resourcePath);

        if (Files.exists(runtimePath)) {
            return runtimePath;
        }

        return resolveBundledPath(resourcePath);
    }

    public static String toMediaUri(String resourcePath) {
        Path path = resolveVideoPath(resourcePath);

        if (!Files.exists(path)) {
            throw new RuntimeException("Không tìm thấy video: " + path);
        }

        return path.toUri().toString();
    }

    private static String cleanPath(String resourcePath) {
        String cleanPath = resourcePath.trim();

        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        return cleanPath;
    }
}