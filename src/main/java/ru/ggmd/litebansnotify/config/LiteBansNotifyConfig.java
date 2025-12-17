package ru.ggmd.litebansnotify.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ru.ggmd.litebansnotify.Main;

import java.util.Collections;
import java.util.List;

@Getter
public class LiteBansNotifyConfig {

    private final Main plugin;
    private final String patternTime;
    private final boolean telegramEnabled;
    private final String telegramToken;
    private final String telegramChatId;
    private final String telegramTheme;
    private final String nonePlayer;
    private final String noneTime;
    private final List<String> kickMessage;
    private final List<String> banMessage;
    private final List<String> muteMessage;
    private final List<String> warnMessage;
    private final List<String> unbanMessage;
    private final List<String> unmuteMessage;
    private final List<String> unwarnMessage;

    public LiteBansNotifyConfig(Main plugin) {
        this.plugin = plugin;

        this.patternTime = plugin.getConfig().getString("patternTime", "dd-MM-yy HH:mm");

        ConfigurationSection telegramSection = plugin.getConfig().getConfigurationSection("telegram");
        if (telegramSection != null) {
            this.telegramEnabled = telegramSection.getBoolean("enable", false);
            this.telegramToken = telegramSection.getString("token", "");
            this.telegramChatId = telegramSection.getString("id", "");
            this.telegramTheme = telegramSection.getString("theme", "");
        } else {
            this.telegramEnabled = false;
            this.telegramToken = "";
            this.telegramChatId = "";
            this.telegramTheme = "";
        }

        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection != null) {
            this.nonePlayer = messagesSection.getString("nonePlayer", "❓ Неизвестно");
            this.noneTime = messagesSection.getString("noneTime", "⏳ Навсегда");
            this.kickMessage = messagesSection.getStringList("kick");
            this.banMessage = messagesSection.getStringList("ban");
            this.muteMessage = messagesSection.getStringList("mute");
            this.warnMessage = messagesSection.getStringList("warn");
            this.unbanMessage = messagesSection.getStringList("unban");
            this.unmuteMessage = messagesSection.getStringList("unmute");
            this.unwarnMessage = messagesSection.getStringList("unwarn");
        } else {
            this.nonePlayer = "❓ Неизвестно";
            this.noneTime = "⏳ Навсегда";
            this.kickMessage = Collections.emptyList();
            this.banMessage = Collections.emptyList();
            this.muteMessage = Collections.emptyList();
            this.warnMessage = Collections.emptyList();
            this.unbanMessage = Collections.emptyList();
            this.unmuteMessage = Collections.emptyList();
            this.unwarnMessage = Collections.emptyList();
        }
    }

    public boolean isTelegramConfigured() {
        return telegramEnabled &&
               !telegramToken.isEmpty() &&
               !telegramToken.equals("TOKEN_BOT_API") &&
               !telegramChatId.isEmpty();
    }
}
