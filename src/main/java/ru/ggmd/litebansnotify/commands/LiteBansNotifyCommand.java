package ru.ggmd.litebansnotify.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ggmd.litebansnotify.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LiteBansNotifyCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public LiteBansNotifyCommand(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("litebansnotify.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "test":
                handleTest(sender);
                break;
            case "update":
                handleUpdate(sender);
                break;
            default:
                sendHelpMessage(sender);
        }

        return true;
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "/litebansnotify reload" + ChatColor.WHITE + " - Перезагрузить конфигурацию");
        sender.sendMessage(ChatColor.YELLOW + "/litebansnotify test" + ChatColor.WHITE + " - Отправить тестовое сообщение");
        sender.sendMessage(ChatColor.YELLOW + "/litebansnotify update" + ChatColor.WHITE + " - Скачать обновление плагина");
    }

    private void handleReload(@NotNull CommandSender sender) {
        try {
            plugin.reloadPlugin();
            sender.sendMessage(ChatColor.GREEN + "Конфигурация успешно перезагружена!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при перезагрузке: " + e.getMessage());
            plugin.getLogger().severe("Ошибка при перезагрузке конфигурации: " + e.getMessage());
        }
    }

    private void handleTest(@NotNull CommandSender sender) {
        if (!plugin.getPluginConfig().isTelegramConfigured()) {
            sender.sendMessage(ChatColor.RED + "Telegram не настроен! Проверьте конфигурацию.");
            return;
        }

        String testMessage = "🧪 <b>ТЕСТ</b>\n\n<b>Отправитель:</b> " + sender.getName() + "\n<b>Время:</b> " + new java.util.Date();
        plugin.getTelegramManager().sendMessage(testMessage);
        sender.sendMessage(ChatColor.GREEN + "Тестовое сообщение отправлено в Telegram!");
    }

    private void handleUpdate(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Начинаем загрузку обновления...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latestVersion = getLatestVersion();
                if (latestVersion == null) {
                    sender.sendMessage(ChatColor.RED + "Не удалось получить информацию о последней версии!");
                    return;
                }

                if (plugin.getDescription().getVersion().equals(latestVersion)) {
                    sender.sendMessage(ChatColor.GREEN + "У вас уже установлена последняя версия!");
                    return;
                }

                String downloadUrl = String.format(
                        "https://github.com/GG-MD/LiteBansNotify/releases/download/%s/LiteBansNotify.jar",
                        latestVersion
                );

                File updateFolder = new File("plugins/update");
                if (!updateFolder.exists()) {
                    updateFolder.mkdirs();
                }

                File targetFile = new File(updateFolder, "LiteBansNotify.jar");

                sender.sendMessage(ChatColor.YELLOW + "Загрузка версии " + latestVersion + "...");
                downloadFile(downloadUrl, targetFile);

                sender.sendMessage(ChatColor.GREEN + "Обновление успешно загружено!");
                sender.sendMessage(ChatColor.GREEN + "Перезагрузите сервер для применения изменений.");

            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Ошибка при загрузке обновления: " + e.getMessage());
                plugin.getLogger().severe("Ошибка загрузки обновления: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws IOException {
        try (InputStream inputStream = new URL("https://raw.githubusercontent.com/GG-MD/LiteBansNotify/master/VERSION")
                .openStream();
             java.io.BufferedReader reader = new java.io.BufferedReader(
                     new java.io.InputStreamReader(inputStream))) {
            String version = reader.readLine();
            return version != null ? version.trim() : null;
        }
    }

    private void downloadFile(String urlStr, File target) throws IOException {
        URLConnection connection = new URL(urlStr).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("litebansnotify.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("reload");
            options.add("test");
            options.add("update");

            return options.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
