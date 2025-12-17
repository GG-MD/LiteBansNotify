package ru.ggmd.litebansnotify.listeners;

import litebans.api.Entry;
import litebans.api.Events;
import org.jetbrains.annotations.NotNull;
import ru.ggmd.litebansnotify.Main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LiteBansListener {

    private final Main plugin;
    private Events.Listener eventsListener;

    private final Map<String, Long> eventCache = new ConcurrentHashMap<>();
    private static final long DUPLICATE_THRESHOLD_MS = 1000;

    public LiteBansListener(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (eventsListener != null) {
            unregister();
        }

        eventsListener = new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                handlePunishment(entry);
            }

            @Override
            public void entryRemoved(Entry entry) {
                handleUnpunishment(entry);
            }
        };

        Events.get().register(eventsListener);
        plugin.getLogger().info("LiteBans события зарегистрированы");
    }

    public void unregister() {
        if (eventsListener != null) {
            Events.get().unregister(eventsListener);
            eventsListener = null;
            plugin.getLogger().info("LiteBans события отменены");
        }
    }

    private void handlePunishment(@NotNull Entry entry) {
        if (!plugin.getPluginConfig().isTelegramConfigured()) {
            return;
        }

        String type = entry.getType();
        if (type == null) {
            plugin.getLogger().warning("Получен Entry с null типом");
            return;
        }

        if (isDuplicate(entry, type)) {
            plugin.getLogger().info("Пропущено дублирующееся событие " + type + " для UUID: " + entry.getUuid());
            return;
        }

        switch (type.toLowerCase()) {
            case "ban":
            case "mute":
            case "warn":
            case "kick":
                sendNotification(type, entry);
                break;
            default:
                plugin.getLogger().info("Неизвестный тип наказания: " + type);
        }
    }

    private void handleUnpunishment(@NotNull Entry entry) {
        if (!plugin.getPluginConfig().isTelegramConfigured()) {
            return;
        }

        String type = entry.getType();
        if (type == null) {
            plugin.getLogger().warning("Получен Entry с null типом при снятии наказания");
            return;
        }

        String notificationType;
        switch (type.toLowerCase()) {
            case "ban":
                notificationType = "unban";
                break;
            case "mute":
                notificationType = "unmute";
                break;
            case "warn":
                notificationType = "unwarn";
                break;
            default:
                plugin.getLogger().info("Неизвестный тип снятия наказания: " + type);
                return;
        }

        if (isDuplicate(entry, notificationType)) {
            plugin.getLogger().info("Пропущено дублирующееся событие " + notificationType + " для UUID: " + entry.getUuid());
            return;
        }

        sendNotification(notificationType, entry);
    }

    private boolean isDuplicate(@NotNull Entry entry, @NotNull String type) {
        String eventKey = generateEventKey(entry, type);
        long currentTime = System.currentTimeMillis();

        Long lastProcessedTime = eventCache.get(eventKey);

        if (lastProcessedTime != null && (currentTime - lastProcessedTime) < DUPLICATE_THRESHOLD_MS) {
            return true;
        }

        eventCache.put(eventKey, currentTime);

        cleanupCache(currentTime);

        return false;
    }

    private String generateEventKey(@NotNull Entry entry, @NotNull String type) {
        return type + ":" + entry.getUuid() + ":" + (entry.getReason() != null ? entry.getReason() : "");
    }

    private void cleanupCache(long currentTime) {
        eventCache.entrySet().removeIf(e -> (currentTime - e.getValue()) > 5000);
    }

    private void sendNotification(@NotNull String type, @NotNull Entry entry) {
        try {
            String formattedMessage = plugin.getMessageFormatter().formatMessage(type, entry);
            plugin.getTelegramManager().sendMessage(formattedMessage);

            plugin.getLogger().info("Отправлено уведомление о " + type + " для игрока с UUID: " + entry.getUuid());
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при отправке уведомления о " + type + ": " + e.getMessage());
        }
    }
}
