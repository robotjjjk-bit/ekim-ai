package net.ekmai.android.in.utilities;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionChecker {

    private static final String API_URL = "https://api.github.com/repos/ekam-labs/ekm_android/releases/latest";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface VersionCallback {
        void onResponse(VersionInfo info);
        void onError(String message);
    }

    // A clean data model to hold the results
    public static class VersionInfo {
        public final String tagName;
        public final String downloadUrl;

        public VersionInfo(String tagName, String downloadUrl) {
            this.tagName = tagName;
            this.downloadUrl = downloadUrl;
        }
    }

    /**
     * Executes the update check asynchronously.
     */
    public void fetchLatestVersion(VersionCallback callback) {
        executor.execute(() -> {
            try {
                VersionInfo info = getLatestFromNetwork();
                if (info.tagName != null) {
                    mainHandler.post(() -> callback.onResponse(info));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to parse data"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Internal logic to fetch data.
     * This is separate so you can call it from a Worker or Service easily.
     */
    public VersionInfo getLatestFromNetwork() throws Exception {
        URL url = URI.create(API_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "EkmAI-App");
        conn.setConnectTimeout(5000);

        if (conn.getResponseCode() != 200) return new VersionInfo(null, null);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        String json = sb.toString();

        // Extraction Logic
        String tag = extractValue(json, "tag_name");

        // Try to find direct APK link, otherwise use the release page URL
        String link = extractValue(json, "browser_download_url");
        if (link == null) {
            link = extractValue(json, "html_url");
        }

        return new VersionInfo(tag, link);
    }

    /**
     * Helper to compare version strings (e.g., "v1.1" vs "v1.2")
     */
    public boolean isNewerVersion(String current, String latest) {
        if (current == null || latest == null) return false;

        try {
            String[] cParts = current.replaceAll("[^0-9.]", "").split("\\.");
            String[] lParts = latest.replaceAll("[^0-9.]", "").split("\\.");

            int length = Math.max(cParts.length, lParts.length);
            for (int i = 0; i < length; i++) {
                int c = i < cParts.length ? Integer.parseInt(cParts[i]) : 0;
                int l = i < lParts.length ? Integer.parseInt(lParts[i]) : 0;
                if (l > c) return true;
                if (l < c) return false;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private String extractValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}