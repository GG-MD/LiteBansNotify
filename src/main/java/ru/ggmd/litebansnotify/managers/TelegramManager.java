package ru.ggmd.litebansnotify.managers;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.ggmd.litebansnotify.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Getter
public class TelegramManager {

    private final Main plugin;
    private final String botToken;
    private final String chatId;
    private final String messageThreadId;
    private final boolean enabled;

    public TelegramManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.botToken = plugin.getPluginConfig().getTelegramToken();
        this.chatId = plugin.getPluginConfig().getTelegramChatId();
        this.messageThreadId = plugin.getPluginConfig().getTelegramTheme();
        this.enabled = plugin.getPluginConfig().isTelegramConfigured();
    }

    public void sendMessage(@NotNull String message) {
        if (!enabled) {
            plugin.getLogger().warning("Telegram не настроен или отключен");
            return;
        }

        if (message.trim().isEmpty()) {
            plugin.getLogger().warning("Попытка отправить пустое сообщение в Telegram");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    sendTextMessage(message);
                } catch (Exception e) {
                    plugin.getLogger().severe("Ошибка при отправке сообщения в Telegram: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void sendTextMessage(@NotNull String text) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        String jsonBody = buildJsonBody(text);

        sendHttpRequest(apiUrl, jsonBody);
    }

    private @NotNull String buildJsonBody(@NotNull String text) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"chat_id\":\"").append(escapeJson(chatId)).append("\",");
        jsonBuilder.append("\"text\":\"").append(escapeJson(text)).append("\",");
        jsonBuilder.append("\"parse_mode\":\"HTML\"");

        if (!messageThreadId.isEmpty()) {
            jsonBuilder.append(",\"message_thread_id\":").append(messageThreadId);
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private void sendHttpRequest(@NotNull String apiUrl, @NotNull String jsonBody) throws IOException {
        URL url = URI.create(apiUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            if (responseCode >= 200 && responseCode < 300) {
                plugin.getLogger().info("Сообщение успешно отправлено в Telegram");
            } else {
                plugin.getLogger().severe("Ошибка при отправке сообщения в Telegram. Код: " +
                        responseCode + ", Ответ: " + response);
            }
        }

        connection.disconnect();
    }

    private @NotNull String escapeJson(@NotNull String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
