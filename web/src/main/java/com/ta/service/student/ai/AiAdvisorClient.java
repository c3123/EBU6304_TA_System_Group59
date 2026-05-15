package com.ta.service.student.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AiAdvisorClient {
    private static final String CONFIG_PATH = "/WEB-INF/config/ai.properties";
    private static final String API_KEY_PLACEHOLDER = "PUT_YOUR_DASHSCOPE_KEY_HERE";
    private static final Gson GSON = new Gson();

    public AiAdvisorResult ask(ServletContext context, String systemPrompt, String userPayload) {
        try {
            AiConfig config = loadConfig(context);
            if (!config.enabled || isBlank(config.apiUrl) || isBlank(config.apiKey)
                    || API_KEY_PLACEHOLDER.equals(config.apiKey.trim()) || isBlank(config.model)) {
                return AiAdvisorResult.failure();
            }

            HttpURLConnection connection = (HttpURLConnection) new URL(config.apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.timeoutMs);
            connection.setReadTimeout(config.timeoutMs);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + config.apiKey);

            byte[] payload = buildRequestJson(config.model, systemPrompt, userPayload)
                    .getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                drainError(connection);
                return AiAdvisorResult.failure();
            }

            String responseJson = readStream(connection.getInputStream());
            String content = parseContent(responseJson);
            if (isBlank(content)) {
                return AiAdvisorResult.failure();
            }
            return AiAdvisorResult.success(content.trim());
        } catch (Exception ex) {
            return AiAdvisorResult.failure();
        }
    }

    private AiConfig loadConfig(ServletContext context) throws IOException {
        if (context == null) {
            return AiConfig.disabled();
        }

        try (InputStream input = context.getResourceAsStream(CONFIG_PATH)) {
            if (input == null) {
                return AiConfig.disabled();
            }

            Properties properties = new Properties();
            properties.load(input);

            AiConfig config = new AiConfig();
            config.enabled = Boolean.parseBoolean(properties.getProperty("ai.enabled", "false").trim());
            config.apiUrl = properties.getProperty("ai.api.url", "").trim();
            config.apiKey = properties.getProperty("ai.api.key", "").trim();
            config.model = properties.getProperty("ai.model", "qwen-plus").trim();
            config.timeoutMs = parseTimeout(properties.getProperty("ai.timeout.ms", "15000"));
            return config;
        }
    }

    private String buildRequestJson(String model, String systemPrompt, String userPayload) {
        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.addProperty("temperature", 0.2);

        JsonArray messages = new JsonArray();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPayload));
        request.add("messages", messages);
        return GSON.toJson(request);
    }

    private JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private String parseContent(String responseJson) {
        JsonObject root = JsonParser.parseString(responseJson).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            return "";
        }

        JsonObject first = choices.get(0).getAsJsonObject();
        JsonObject message = first.getAsJsonObject("message");
        if (message == null || message.get("content") == null || message.get("content").isJsonNull()) {
            return "";
        }
        return message.get("content").getAsString();
    }

    private int parseTimeout(String value) {
        try {
            int timeout = Integer.parseInt(value);
            return timeout > 0 ? timeout : 15000;
        } catch (NumberFormatException ex) {
            return 15000;
        }
    }

    private void drainError(HttpURLConnection connection) {
        InputStream error = connection.getErrorStream();
        if (error == null) {
            return;
        }
        try {
            readStream(error);
        } catch (IOException ignored) {
            // Failure details are intentionally hidden from the frontend.
        }
    }

    private String readStream(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class AiConfig {
        private boolean enabled;
        private String apiUrl;
        private String apiKey;
        private String model;
        private int timeoutMs;

        private static AiConfig disabled() {
            AiConfig config = new AiConfig();
            config.enabled = false;
            config.timeoutMs = 15000;
            return config;
        }
    }

    public static class AiAdvisorResult {
        private final boolean success;
        private final String answer;

        private AiAdvisorResult(boolean success, String answer) {
            this.success = success;
            this.answer = answer;
        }

        public static AiAdvisorResult success(String answer) {
            return new AiAdvisorResult(true, answer);
        }

        public static AiAdvisorResult failure() {
            return new AiAdvisorResult(false, "");
        }

        public boolean isSuccess() {
            return success;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
