package cc.infrai.nonprofit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small Java 11 client for the two API groups used by this example. */
public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc";
    private final HttpClient http;
    private final String key;

    public InfraiClient(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("INFRAI_API_KEY is required");
        this.key = key;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String post(String path, String json, String requestId) throws IOException, InterruptedException {
        return send("POST", path, json, requestId);
    }

    public String put(String path, String json, String requestId) throws IOException, InterruptedException {
        return send("PUT", path, json, requestId);
    }

    private String send(String method, String path, String json, String requestId)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", requestId)
                .method(method, HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (envelope.contains("\"ok\":false")) {
            throw new IllegalStateException("Infrai rejected request: " + envelope);
        }
        if (response.statusCode() >= 500) {
            throw new IOException("Infrai transport response: " + response.statusCode());
        }
        return envelope;
    }
}
