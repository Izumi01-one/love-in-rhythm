package main.checkResource;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// lấy id từ ResourceCheckerApp để tải về, có tự động nhấn tải khi file lớn
public class GoogleDriveDownloader {

    public interface ProgressCallback {
        void onProgress(long downloadedBytes, long totalBytes);
    }

    private static final CookieManager COOKIE_MANAGER = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .cookieHandler(COOKIE_MANAGER)
            .build();


    public static void downloadFile(
            String backup,
            Path outputPath,
            ProgressCallback callback
    ) throws IOException, InterruptedException {

        if (backup == null || backup.isBlank()) {
            throw new IllegalArgumentException("Backup Google Drive rỗng.");
        }

        String fileId = extractFileId(backup);

        if (fileId == null || fileId.isBlank()) {
            throw new IllegalArgumentException("Không lấy được Google Drive file id từ: " + backup);
        }

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        String firstUrl = "https://drive.google.com/uc?export=download&id=" + encode(fileId);

        HttpResponse<InputStream> firstResponse = sendRequest(firstUrl);

        if (isRealFileResponse(firstResponse)) {
            saveResponseBody(firstResponse, outputPath, callback);
            validateDownloadedFile(outputPath);
            return;
        }

        String html = readAllText(firstResponse.body());

        String confirmUrl = extractConfirmUrl(html, fileId);

        if (confirmUrl == null || confirmUrl.isBlank()) {
            throw new IOException("Không tìm thấy link xác nhận Download anyway của Google Drive.");
        }

        HttpResponse<InputStream> finalResponse = sendRequest(confirmUrl);

        if (!isSuccess(finalResponse.statusCode())) {
            throw new IOException("Không tải được file. HTTP status: " + finalResponse.statusCode());
        }

        saveResponseBody(finalResponse, outputPath, callback);
        validateDownloadedFile(outputPath);
    }

    private static HttpResponse<InputStream> sendRequest(String url)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        return CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static boolean isRealFileResponse(HttpResponse<InputStream> response) {
        Optional<String> contentDisposition =
                response.headers().firstValue("Content-Disposition");

        Optional<String> contentType =
                response.headers().firstValue("Content-Type");

        if (contentDisposition.isPresent()
                && contentDisposition.get().toLowerCase().contains("attachment")) {
            return true;
        }

        return contentType.isPresent()
                && !contentType.get().toLowerCase().contains("text/html");
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private static void saveResponseBody(
            HttpResponse<InputStream> response,
            Path outputPath,
            ProgressCallback callback
    ) throws IOException {

        long totalSize = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1);

        Path tempPath = outputPath.resolveSibling(
                outputPath.getFileName().toString() + ".download"
        );

        try (
                InputStream inputStream = response.body();
                OutputStream outputStream = Files.newOutputStream(
                        tempPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                )
        ) {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            long downloaded = 0;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                downloaded += read;

                if (callback != null) {
                    callback.onProgress(downloaded, totalSize);
                }
            }
        }

        Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String readAllText(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extractConfirmUrl(String html, String fileId) {
        if (html == null || html.isBlank()) {
            return null;
        }

        html = html.replace("&amp;", "&");

        Pattern hrefPattern = Pattern.compile("href=\"([^\"]*confirm=[^\"]*)\"");
        Matcher hrefMatcher = hrefPattern.matcher(html);

        if (hrefMatcher.find()) {
            String url = hrefMatcher.group(1);

            if (url.startsWith("/")) {
                url = "https://drive.google.com" + url;
            }

            return url;
        }

        Pattern formPattern = Pattern.compile(
                "<form[^>]*action=\"([^\"]+)\"[^>]*>(.*?)</form>",
                Pattern.DOTALL
        );

        Matcher formMatcher = formPattern.matcher(html);

        while (formMatcher.find()) {
            String action = formMatcher.group(1).replace("&amp;", "&");
            String formBody = formMatcher.group(2);

            if (!action.startsWith("http")) {
                action = "https://drive.usercontent.google.com" + action;
            }

            Map<String, String> params = new LinkedHashMap<>();

            Pattern inputPattern = Pattern.compile(
                    "<input[^>]*name=\"([^\"]+)\"[^>]*value=\"([^\"]*)\"[^>]*>"
            );

            Matcher inputMatcher = inputPattern.matcher(formBody);

            while (inputMatcher.find()) {
                params.put(inputMatcher.group(1), inputMatcher.group(2));
            }

            params.putIfAbsent("id", fileId);
            params.putIfAbsent("export", "download");
            params.putIfAbsent("confirm", "t");

            return action + "?" + buildQuery(params);
        }

        return "https://drive.usercontent.google.com/download?id="
                + encode(fileId)
                + "&export=download&confirm=t";
    }

    private static String buildQuery(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }

            builder.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(entry.getValue()));
        }

        return builder.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String extractFileId(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        input = input.trim();

        Pattern filePattern = Pattern.compile("/file/d/([^/]+)");
        Matcher fileMatcher = filePattern.matcher(input);

        if (fileMatcher.find()) {
            return fileMatcher.group(1);
        }

        Pattern idPattern = Pattern.compile("[?&]id=([^&]+)");
        Matcher idMatcher = idPattern.matcher(input);

        if (idMatcher.find()) {
            return idMatcher.group(1);
        }

        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            return input;
        }

        return null;
    }

    // tự động nhấn tải về khi file lớn, cảnh báo trên GG Drive
    private static void validateDownloadedFile(Path outputPath) throws IOException {
        if (!Files.exists(outputPath)) {
            throw new IOException("File chưa được tải về: " + outputPath);
        }

        long size = Files.size(outputPath);

        if (size == 0) {
            Files.deleteIfExists(outputPath);
            throw new IOException("File tải về rỗng.");
        }

        if (size < 1024 * 100) {
            String content = Files.readString(outputPath, StandardCharsets.UTF_8);

            String lower = content.toLowerCase();

            if (lower.contains("<html")
                    || lower.contains("google drive")
                    || lower.contains("virus")
                    || lower.contains("sign in")
                    || lower.contains("download anyway")) {

                Files.deleteIfExists(outputPath);
                throw new IOException("Tải nhầm trang HTML Google Drive, chưa phải file video thật.");
            }
        }
    }
}
