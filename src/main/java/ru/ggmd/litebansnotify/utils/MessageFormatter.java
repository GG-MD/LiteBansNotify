package ru.ggmd.litebansnotify.utils;

import litebans.api.Entry;
import org.jetbrains.annotations.NotNull;
import ru.ggmd.litebansnotify.Main;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MessageFormatter {

    private final Main plugin;
    private final SimpleDateFormat dateFormat;

    public MessageFormatter(@NotNull Main plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat(plugin.getPluginConfig().getPatternTime());
    }

    public @NotNull String formatMessage(@NotNull String type, @NotNull Entry entry) {
        List<String> messageTemplate = getMessageTemplate(type);

        if (messageTemplate.isEmpty()) {
            return "Шаблон сообщения для типа '" + type + "' не найден";
        }

        StringBuilder formattedMessage = new StringBuilder();
        for (int i = 0; i < messageTemplate.size(); i++) {
            if (i > 0) {
                formattedMessage.append("\n");
            }
            formattedMessage.append(replacePlaceholders(messageTemplate.get(i), entry, type));
        }

        return formattedMessage.toString();
    }

    private @NotNull List<String> getMessageTemplate(@NotNull String type) {
        switch (type.toLowerCase()) {
            case "kick":
                return plugin.getPluginConfig().getKickMessage();
            case "ban":
                return plugin.getPluginConfig().getBanMessage();
            case "mute":
                return plugin.getPluginConfig().getMuteMessage();
            case "warn":
                return plugin.getPluginConfig().getWarnMessage();
            case "unban":
                return plugin.getPluginConfig().getUnbanMessage();
            case "unmute":
                return plugin.getPluginConfig().getUnmuteMessage();
            case "unwarn":
                return plugin.getPluginConfig().getUnwarnMessage();
            default:
                return Collections.emptyList();
        }
    }

    private @NotNull String replacePlaceholders(@NotNull String message, @NotNull Entry entry, @NotNull String eventType) {
        String result = message;

        result = result.replace("{player}", getPlayerName(entry));
        result = result.replace("{exec_player}", getExecutorName(entry, eventType));
        result = result.replace("{time}", formatTimeFromLong(entry.getDateStart()));
        result = result.replace("{time_end}", formatEndTime(entry));
        result = result.replace("{reason}", getReason(entry, eventType));
        result = result.replace("{servers}", getServerScope(entry));

        return result;
    }

    private @NotNull String getPlayerName(@NotNull Entry entry) {
        String uuid = entry.getUuid();
        if (uuid == null || uuid.isEmpty()) {
            return plugin.getPluginConfig().getNonePlayer();
        }

        try {
            String playerName = litebans.api.Database.get().getPlayerName(java.util.UUID.fromString(uuid));
            return playerName != null ? playerName : plugin.getPluginConfig().getNonePlayer();
        } catch (Exception e) {
            return plugin.getPluginConfig().getNonePlayer();
        }
    }

    private @NotNull String getExecutorName(@NotNull Entry entry, @NotNull String eventType) {
        if (isRemovalEvent(eventType)) {
            String removedByName = entry.getRemovedByName();
            if (removedByName != null && !removedByName.isEmpty()) {
                return removedByName;
            }

            String removedByUuid = entry.getRemovedByUUID();
            if (removedByUuid != null && !removedByUuid.isEmpty()) {
                try {
                    String executorName = litebans.api.Database.get().getPlayerName(java.util.UUID.fromString(removedByUuid));
                    return executorName != null ? executorName : plugin.getPluginConfig().getNonePlayer();
                } catch (Exception e) {
                    return plugin.getPluginConfig().getNonePlayer();
                }
            }

            return plugin.getPluginConfig().getNonePlayer();
        }

        String executorName = entry.getExecutorName();
        if (executorName != null && !executorName.isEmpty()) {
            return executorName;
        }

        String executorUuid = entry.getExecutorUUID();
        if (executorUuid == null || executorUuid.isEmpty()) {
            return plugin.getPluginConfig().getNonePlayer();
        }

        try {
            String name = litebans.api.Database.get().getPlayerName(java.util.UUID.fromString(executorUuid));
            return name != null ? name : plugin.getPluginConfig().getNonePlayer();
        } catch (Exception e) {
            return plugin.getPluginConfig().getNonePlayer();
        }
    }

    private @NotNull String formatTimeFromLong(long timestamp) {
        if (timestamp <= 0) {
            return plugin.getPluginConfig().getNoneTime();
        }
        return dateFormat.format(new Date(timestamp));
    }

    private @NotNull String formatEndTime(@NotNull Entry entry) {
        if (entry.isPermanent()) {
            return plugin.getPluginConfig().getNoneTime();
        }

        long endTime = entry.getDateEnd();
        if (endTime <= 0) {
            return plugin.getPluginConfig().getNoneTime();
        }

        return dateFormat.format(new Date(endTime));
    }

    private @NotNull String getReason(@NotNull Entry entry, @NotNull String eventType) {
        if (isRemovalEvent(eventType)) {
            String removalReason = entry.getRemovalReason();
            return removalReason != null && !removalReason.isEmpty() ? removalReason : "Причина снятия не указана";
        }

        String reason = entry.getReason();
        return reason != null && !reason.isEmpty() ? reason : "Причина не указана";
    }

    private @NotNull String getServerScope(@NotNull Entry entry) {
        String serverScope = entry.getServerScope();
        return serverScope != null && !serverScope.isEmpty() ? serverScope : "Все сервера";
    }

    private boolean isRemovalEvent(@NotNull String eventType) {
        String type = eventType.toLowerCase();
        return type.equals("unban") || type.equals("unmute") || type.equals("unwarn");
    }
}
