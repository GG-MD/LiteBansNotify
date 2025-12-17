package ru.ggmd.litebansnotify.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ggmd.litebansnotify.Main;

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
            default:
                sendHelpMessage(sender);
        }

        return true;
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "/litebansnotify reload" + ChatColor.WHITE + " - Перезагрузить конфигурацию");
        sender.sendMessage(ChatColor.YELLOW + "/litebansnotify test" + ChatColor.WHITE + " - Отправить тестовое сообщение");
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

            return options.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
