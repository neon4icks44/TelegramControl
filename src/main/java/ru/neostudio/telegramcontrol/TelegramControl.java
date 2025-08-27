package ru.neostudio.telegramcontrol;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.*;

public class TelegramControl extends JavaPlugin {

    private String botToken;
    private String botUsername;
    private String authLogin;
    private String authPassword;
    private List<String> forbiddenCommands;
    private final Map<Long, UUID> authenticatedUsers = new HashMap<>();
    private static final String RED = "\u001b[31m";
    private static final String GREEN = "\u001b[32m";
    private static final String RESET = "\u001b[0m";
    private int logLines;
    private final List<String> consoleLogs = new LinkedList<>();
    private ConsoleLogHandler logHandler;
    private ControlBot controlBot;
    private StatsBot statsBot;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        if (botToken == null || botToken.isEmpty() || botToken.equals("YOUR_BOT_TOKEN_HERE")) {
            getLogger().warning("Токен основного бота не настроен в config.yml. Укажите валидный токен от @BotFather и перезапустите сервер. Плагин отключен.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (botUsername == null || botUsername.isEmpty() || botUsername.equals("YourBotUsername")) {
            getLogger().warning("Имя основного бота не настроено в config.yml. Укажите имя бота без @ и перезапустите сервер. Плагин отключен.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        setupConsoleLogging();

        controlBot = new ControlBot(this);
        try {
            GetMe getMe = new GetMe();
            controlBot.execute(getMe);
            getLogger().info("Токен основного бота валиден.");
        } catch (TelegramApiException e) {
            if (e instanceof TelegramApiRequestException && ((TelegramApiRequestException) e).getErrorCode() == 404) {
                getLogger().severe("Неверный токен основного бота (404 Not Found). Получите новый токен от @BotFather. Плагин отключен.");
            } else {
                getLogger().severe("Ошибка проверки токена основного бота: " + e.getMessage());
            }
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(controlBot);
            getLogger().info("Основной Telegram бот успешно запущен!");
            statsBot = new StatsBot(this);
            botsApi.registerBot(statsBot);
            getLogger().info("Telegram бот для статистики успешно запущен!");
            statsBot.sendStartupNotification();
            getLogger().info(GREEN + "Плагин TelegramControl успешно запущен!" + RESET);
            getLogger().info(RED + "by NeoStudio" + RESET);
        } catch (TelegramApiException e) {
            getLogger().severe("Ошибка запуска ботов: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (logHandler != null) {
            getLogger().removeHandler(logHandler);
            Logger.getLogger("").removeHandler(logHandler);
        }
        getLogger().info("Плагин TelegramControl выключен.");
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        botToken = config.getString("bot.token", "YOUR_BOT_TOKEN_HERE");
        botUsername = config.getString("bot.username", "YourBotUsername");
        authLogin = config.getString("auth.login", "admin");
        authPassword = config.getString("auth.password", "securepassword");
        forbiddenCommands = config.getStringList("forbidden_commands");
        logLines = config.getInt("log_lines", 10);
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public boolean authenticate(Long chatId, String login, String password) {
        if (authLogin.equals(login) && authPassword.equals(password)) {
            authenticatedUsers.put(chatId, UUID.randomUUID());
            return true;
        }
        return false;
    }

    public boolean isAuthenticated(Long chatId) {
        return authenticatedUsers.containsKey(chatId);
    }

    public void logout(Long chatId) {
        authenticatedUsers.remove(chatId);
    }

    public boolean isCommandForbidden(String command) {
        return forbiddenCommands.contains(command.toLowerCase());
    }

    public String executeCommandWithOutput(String command) {
        List<String> commandLogs = new ArrayList<>();
        ConsoleLogHandler commandHandler = new ConsoleLogHandler(commandLogs);

        synchronized (consoleLogs) {
            Logger.getLogger("").addHandler(commandHandler);
        }

        try {
            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Ошибка выполнения команды: " + e.getMessage();
        } finally {
            synchronized (consoleLogs) {
                Logger.getLogger("").removeHandler(commandHandler);
            }
        }

        String output = commandLogs.isEmpty() ? "Команда выполнена, но вывода нет." : String.join("\n", commandLogs);
        return output;
    }

    public String getServerStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("🐱‍👤 Статус сервера:\n");
        sb.append("🤖 Онлайн: ").append(Bukkit.getOnlinePlayers().size()).append("/").append(Bukkit.getMaxPlayers()).append("\n");
        sb.append("🐱‍👓 Версия: ").append(Bukkit.getVersion()).append("\n");
        try {
            Method getTpsMethod = Bukkit.getServer().getClass().getMethod("getTPS");
            double[] tps = (double[]) getTpsMethod.invoke(Bukkit.getServer());
            sb.append("🎇 TPS: ").append(String.format("%.2f", tps[0])).append("\n");
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            sb.append("🎇 TPS: Недоступно\n");
        }
        return sb.toString();
    }

    public String getRecentLogs() {
        synchronized (consoleLogs) {
            if (consoleLogs.isEmpty()) {
                return "Логи консоли отсутствуют.";
            }
            return String.join("\n", consoleLogs);
        }
    }

    private void setupConsoleLogging() {
        logHandler = new ConsoleLogHandler();
        getLogger().addHandler(logHandler);
        Logger.getLogger("").addHandler(logHandler);
        getLogger().info("Console logging initialized for TelegramControl.");
    }

    private class ConsoleLogHandler extends Handler {
        private final List<String> targetLogs;

        public ConsoleLogHandler() {
            this.targetLogs = consoleLogs;
        }

        public ConsoleLogHandler(List<String> targetLogs) {
            this.targetLogs = targetLogs;
        }

        @Override
        public void publish(LogRecord record) {
            synchronized (targetLogs) {
                String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date(record.getMillis()));
                String message = String.format("[%s %s] %s", timestamp, record.getLevel().getName(), record.getMessage());
                if (record.getThrown() != null) {
                    message += " [Ошибка]: " + record.getThrown().getMessage();
                }
                targetLogs.add(message);
                if (targetLogs == consoleLogs && targetLogs.size() > logLines * 2) {
                    targetLogs.remove(0);
                }
            }
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }
}