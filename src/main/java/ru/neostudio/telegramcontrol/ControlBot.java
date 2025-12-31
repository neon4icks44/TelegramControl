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
                    sendMessage(plugin.getLang("command_forbidden"), true);
                    plugin.getLogger().info(String.format("[TelegramControl] User %d attempted forbidden command: %s at %s",
                            currentChatId, fullCommand, timestamp));
                    return;
                }
                String output = plugin.executeCommandWithOutput(fullCommand);
                sendMessage(plugin.getLang("command_executed") + ": " + message + "\n" +
                        plugin.getLang("command_output") + "\n" + output, true);
                plugin.getLogger().info(String.format("[TelegramControl] User %d executed command: %s at %s, output: %s",
                        currentChatId, fullCommand, timestamp, output));
            } else {
                sendMessage(plugin.getLang("command_must_start_slash"), true);
            }
            return;
        }

        switch (message) {
            case "Server Status":
            case "Статус сервера":
                sendMessage(plugin.getServerStatus(), true);
                plugin.getLogger().info(String.format("[TelegramControl] User %d requested status at %s", currentChatId, timestamp));
                break;
            case "Console":
            case "Консоль":
                String logs = plugin.getRecentLogs();
                sendMessage(plugin.getLang("command_output") + ":\n" + logs, true);
                plugin.getLogger().info(String.format("[TelegramControl] User %d requested logs at %s", currentChatId, timestamp));
                break;
            case "Command":
            case "Команда":
                awaitingCommand = true;
                sendMessage(plugin.getLang("enter_command"), true);
                break;
            case "Logout":
            case "Выйти":
                plugin.logout(currentChatId);
                sendMessage(plugin.getLang("logged_out"));
                plugin.getLogger().info(String.format("[TelegramControl] User %d logged out at %s", currentChatId, timestamp));
                break;
            default:
                if (message.startsWith("/")) {
                    String fullCommand = message.substring(1);
                    String cmdName = fullCommand.split(" ")[0].toLowerCase();
                    if (plugin.isCommandForbidden(cmdName)) {
                        sendMessage(plugin.getLang("command_forbidden"), true);
                        return;
                    }
                    String output = plugin.executeCommandWithOutput(fullCommand);
                    sendMessage(plugin.getLang("command_executed") + ": " + message + "\n" +
                            plugin.getLang("command_output") + "\n" + output, true);
                } else {
                    sendMessage(plugin.getLang("invalid_action"), true);
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
            sendMessage(plugin.getLang("password_prompt"));
            return;
        }

        if (awaitingPassword) {
            awaitingPassword = false;
            if (plugin.authenticate(currentChatId, tempLogin, message)) {
                sendMessage(plugin.getLang("auth_success"), true);
                plugin.getLogger().info(String.format("[TelegramControl] User %d logged in with login %s at %s",
                        currentChatId, tempLogin, timestamp));
            } else {
                sendMessage(plugin.getLang("auth_failed"));
                plugin.getLogger().info(String.format("[TelegramControl] Failed login for Chat ID %d with login %s at %s",
                        currentChatId, tempLogin, timestamp));
            }
            return;
        }

        if (message.equalsIgnoreCase("/login")) {
            awaitingLogin = true;
            sendMessage(plugin.getLang("login_prompt"));
        } else {
            sendMessage(plugin.getLang("welcome"));
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
            plugin.getLogger().severe("Error send message: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(plugin.getLang("status_button"));
        row1.add(plugin.getLang("console_button"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(plugin.getLang("command_button"));
        row2.add(plugin.getLang("logout_button"));
        keyboard.add(row2);

        markup.setKeyboard(keyboard);
        return markup;
    }
}