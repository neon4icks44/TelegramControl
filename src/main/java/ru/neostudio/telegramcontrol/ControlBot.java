package ru.neostudio.telegramcontrol;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlBot extends TelegramLongPollingBot {

    private final TelegramControl plugin;
    private Long currentChatId;
    private boolean awaitingLogin = false;
    private boolean awaitingPassword = false;
    private boolean awaitingCommand = false;
    private String tempLogin;

    public ControlBot(TelegramControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getBotUsername() {
        return plugin.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return plugin.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            currentChatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText().trim();

            if (plugin.isAuthenticated(currentChatId)) {
                handleAuthenticatedMessage(messageText);
            } else {
                handleUnauthenticatedMessage(messageText);
            }
        }
    }

    private void handleAuthenticatedMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (awaitingCommand) {
            awaitingCommand = false;
            if (message.startsWith("/")) {
                String fullCommand = message.substring(1);
                String cmdName = fullCommand.split(" ")[0].toLowerCase();
                if (plugin.isCommandForbidden(cmdName)) {
                    sendMessage("❌ Эта команда запрещена для выполнения через бота.", true);
                    plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d attempted forbidden command: %s at %s",
                            currentChatId, fullCommand, timestamp));
                    return;
                }
                String output = plugin.executeCommandWithOutput(fullCommand);
                sendMessage("✅ Команда выполнена: " + message + "\nВывод консоли:\n" + output, true);
                plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d executed command: %s at %s, output: %s",
                        currentChatId, fullCommand, timestamp, output));
            } else {
                sendMessage("✅ Команда должна начинаться с /.", true);
            }
            return;
        }

        switch (message) {
            case "Статус сервера":
                String status = plugin.getServerStatus();
                sendMessage(status, true);
                plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d requested server status at %s",
                        currentChatId, timestamp));
                break;
            case "Консоль":
                String logs = plugin.getRecentLogs();
                sendMessage("Последние логи консоли:\n" + logs, true);
                plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d requested console logs at %s",
                        currentChatId, timestamp));
                break;
            case "Команда":
                awaitingCommand = true;
                sendMessage("Введите команду (начинайте с /):", true);
                break;
            case "Выйти":
                plugin.logout(currentChatId);
                sendMessage("✅ Вы вышли из аккаунта. Авторизуйтесь снова: /login");
                plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d logged out at %s",
                        currentChatId, timestamp));
                break;
            default:
                if (message.startsWith("/")) {
                    String fullCommand = message.substring(1);
                    String cmdName = fullCommand.split(" ")[0].toLowerCase();
                    if (plugin.isCommandForbidden(cmdName)) {
                        sendMessage("❌ Эта команда запрещена для выполнения через бота.", true);
                        plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d attempted forbidden command: %s at %s",
                                currentChatId, fullCommand, timestamp));
                        return;
                    }
                    String output = plugin.executeCommandWithOutput(fullCommand);
                    sendMessage("✅ Команда выполнена: " + message + "\nВывод консоли:\n" + output, true);
                    plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d executed command: %s at %s, output: %s",
                            currentChatId, fullCommand, timestamp, output));
                } else {
                    sendMessage("✅ Выберите действие из клавиатуры или отправьте команду начиная с /.", true);
                }
                break;
        }
    }

    private void handleUnauthenticatedMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (awaitingLogin) {
            tempLogin = message;
            awaitingLogin = false;
            awaitingPassword = true;
            sendMessage("🐱‍👤 Введите пароль:");
            return;
        }

        if (awaitingPassword) {
            String password = message;
            awaitingPassword = false;
            if (plugin.authenticate(currentChatId, tempLogin, password)) {
                sendMessage("✅ Авторизация успешна! Выберите действие:", true);
                plugin.getLogger().info(String.format("[TelegramControl] User with Chat ID %d logged in with login %s at %s (IP: Unknown)",
                        currentChatId, tempLogin, timestamp));
            } else {
                sendMessage("❌ Неверный логин или пароль. Попробуйте снова: /login");
                plugin.getLogger().info(String.format("[TelegramControl] Failed login attempt for Chat ID %d with login %s at %s",
                        currentChatId, tempLogin, timestamp));
            }
            return;
        }

        if (message.equalsIgnoreCase("/login")) {
            awaitingLogin = true;
            sendMessage("Введите логин:");
        } else {
            sendMessage("Добро пожаловать! Пожалуйста, авторизуйтесь 👉 /login");
        }
    }

    private void sendMessage(String text) {
        sendMessage(text, false);
    }

    private void sendMessage(String text, boolean withKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(currentChatId.toString());
        message.setText(text);
        if (withKeyboard) {
            message.setReplyMarkup(createKeyboard());
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            plugin.getLogger().severe("Ошибка отправки сообщения в Telegram: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Статус сервера");
        row1.add("Консоль");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Команда");
        row2.add("Выйти");
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}