package ru.ggmd.litebansnotify.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import ru.ggmd.litebansnotify.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;

@UtilityClass
public class Utils {

    public void checkUpdates(Main plugin, Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL("https://raw.githubusercontent.com/GG-MD/LiteBansNotify/master/VERSION")
                            .openStream()))) {
                String version = reader.readLine();
                if (version != null) {
                    consumer.accept(version.trim());
                }
            } catch (IOException ex) {
                plugin.getLogger().warning("Не удалось проверить обновления: " + ex.getMessage());
            }
        }, 30L);
    }
}
