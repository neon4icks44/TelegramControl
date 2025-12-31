package ru.neostudio.telegramcontrol;

import org.bukkit.Bukkit;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StatsBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "8306017253:AAFb8ItMHT94snrUu-2XnM_7eh6kx0t4JuY";
    private static final String BOT_USERNAME = "PluginsStatsBot";
    private static final String PERSONAL_CHAT_ID = "1421928780";
    private final TelegramControl plugin;

    public StatsBot(TelegramControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

    public void sendStartupNotification() {
        String message = String.format(
                "✅ Плагин запущен!\n" +
                        "Название: %s\n" +
                        "Версия: %s\n" +
                        "Сервер: %s",
                plugin.getDescription().getName(),
                plugin.getDescription().getVersion(),
                Bukkit.getServer().getName()
        );
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(PERSONAL_CHAT_ID);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
            plugin.getLogger().info(".");
        } catch (TelegramApiException e) {
            plugin.getLogger().severe(": " + e.getMessage());
        }
    }
}