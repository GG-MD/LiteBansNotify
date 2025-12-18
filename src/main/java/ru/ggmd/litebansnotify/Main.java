package ru.ggmd.litebansnotify;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ggmd.litebansnotify.commands.LiteBansNotifyCommand;
import ru.ggmd.litebansnotify.config.LiteBansNotifyConfig;
import ru.ggmd.litebansnotify.listeners.LiteBansListener;
import ru.ggmd.litebansnotify.managers.TelegramManager;
import ru.ggmd.litebansnotify.utils.MessageFormatter;
import ru.ggmd.litebansnotify.utils.Utils;

@Getter
public final class Main extends JavaPlugin {

    private LiteBansNotifyConfig pluginConfig;
    private MessageFormatter messageFormatter;
    private TelegramManager telegramManager;
    private LiteBansListener liteBansListener;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("LiteBans")) {
            getLogger().severe("LiteBans плагин не найден! Отключение плагина.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        loadConfiguration();
        initializeComponents();
        registerListener();
        registerCommand();
        checkForUpdates();

        getLogger().info(getName() + " успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (liteBansListener != null) {
            liteBansListener.unregister();
        }
        getLogger().info(getName() + " отключен!");
    }

    private void loadConfiguration() {
        reloadConfig();
        pluginConfig = new LiteBansNotifyConfig(this);
    }

    private void initializeComponents() {
        messageFormatter = new MessageFormatter(this);
        telegramManager = new TelegramManager(this);
    }

    private void registerListener() {
        liteBansListener = new LiteBansListener(this);
        liteBansListener.register();
    }

    private void registerCommand() {
        LiteBansNotifyCommand commandExecutor = new LiteBansNotifyCommand(this);
        getCommand("litebansnotify").setExecutor(commandExecutor);
        getCommand("litebansnotify").setTabCompleter(commandExecutor);
    }

    public void reloadPlugin() {
        loadConfiguration();
        initializeComponents();

        if (liteBansListener != null) {
            liteBansListener.unregister();
        }
        registerListener();

        getLogger().info("Конфигурация перезагружена!");
    }

    public void checkForUpdates() {
        Utils.checkUpdates(this, version -> {
            getLogger().info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                getLogger().info("§aВы используете последнюю версию плагина!");
            } else {
                getLogger().info("§aВы используете устаревшую версию плагина!");
                getLogger().info("§aВы можете скачать новую версию здесь:");
                getLogger().info("§bhttps://github.com/GG-MD/LiteBansNotify/releases/");
                getLogger().info("");
                getLogger().info("§aИли обновите плагин при помощи §b/litebansnotify update");
            }
            getLogger().info("§6========================================");
        });
    }
}
