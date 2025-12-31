package ru.neostudio.telegramcontrol;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TelegramControl extends JavaPlugin implements CommandExecutor {

    private String botToken;
    private String botUsername;
    private String authLogin;
    private String authPassword;
    private List<String> forbiddenCommands;
    private final Map<Long, UUID> authenticatedUsers = new HashMap<>();
    private int logLines = 15;

    private FileConfiguration langConfig;
    private String lang;

    private ControlBot controlBot;
    private StatsBot statsBot;

    private File latestLog;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File langFolder = new File(getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            saveResource("languages/en.yml", false);
            saveResource("languages/ru.yml", false);
        }

        latestLog = new File("logs/latest.log");
        if (!latestLog.exists()) {
            latestLog = new File("latest.log");
        }

        loadConfig();
        loadLanguage();

        if (botToken == null || botToken.isEmpty() || botToken.equals("YOUR_BOT_TOKEN_HERE")) {
            getLogger().warning("Bot token is not configured in config.yml. Plugin disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        controlBot = new ControlBot(this);
        try {
            controlBot.execute(new GetMe());
            getLogger().info("Bot token is valid.");
        } catch (TelegramApiException e) {
            getLogger().severe("Token validation error: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(controlBot);
            getLogger().info("Main Telegram bot launched!");

            statsBot = new StatsBot(this);
            botsApi.registerBot(statsBot);
            getLogger().info("Notification bot launched!");
            statsBot.sendStartupNotification();

            getLogger().info("TelegramControl successfully enabled!");
        } catch (TelegramApiException e) {
            getLogger().severe("Error launching bots: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.getCommand("tgcontrol").setExecutor(this);
    }

    @Override
    public void onDisable() {
        authenticatedUsers.clear();
        getLogger().info("TelegramControl disabled.");
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        botToken = config.getString("bot.token");
        botUsername = config.getString("bot.username");
        authLogin = config.getString("auth.login", "admin");
        authPassword = config.getString("auth.password", "securepassword");
        forbiddenCommands = config.getStringList("forbidden_commands");
        logLines = config.getInt("log_lines", 15);
    }

    private void loadLanguage() {
        lang = getConfig().getString("language", "en").toLowerCase();
        if (!lang.equals("ru") && !lang.equals("en")) lang = "en";

        File langFile = new File(getDataFolder(), "languages/" + lang + ".yml");
        if (!langFile.exists()) saveResource("languages/" + lang + ".yml", false);

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLang(String key) {
        String msg = langConfig.getString(key);
        return msg != null ? msg : key;
    }

    public String getLang(String key, Object... args) {
        String msg = getLang(key);
        return String.format(msg.replace("\\n", "\n"), args);
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
        if (!latestLog.exists()) {
            return "latest.log not found.";
        }

        long fileLengthBefore = latestLog.length();

        Bukkit.getScheduler().runTask(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        List<String> newLines = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(latestLog, "r")) {
            long fileLengthAfter = latestLog.length();
            if (fileLengthAfter <= fileLengthBefore) {
                return getLang("no_output");
            }

            raf.seek(fileLengthBefore);
            String line;
            while ((line = raf.readLine()) != null) {
                newLines.add(new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            getLogger().warning("Error reading new lines: " + e.getMessage());
            return "Error capturing output.";
        }

        if (newLines.isEmpty()) {
            return getLang("no_output");
        }

        while (!newLines.isEmpty() && newLines.get(0).trim().isEmpty()) newLines.remove(0);
        while (!newLines.isEmpty() && newLines.get(newLines.size() - 1).trim().isEmpty()) newLines.remove(newLines.size() - 1);

        return String.join("\n", newLines);
    }

    public String getRecentLogs() {
        if (!latestLog.exists()) {
            return "latest.log not found.";
        }

        List<String> lines = readLastNLines(latestLog, logLines);
        return lines.isEmpty() ? "Logs are empty." : String.join("\n", lines);
    }

    private List<String> readLastNLines(File file, int n) {
        List<String> lines = new ArrayList<>();
        if (!file.exists() || file.length() == 0) return lines;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            Deque<String> deque = new LinkedList<>();
            String line;
            while ((line = br.readLine()) != null) {
                deque.add(line);
                if (deque.size() > n) deque.removeFirst();
            }
            lines.addAll(deque);
        } catch (IOException e) {
            getLogger().warning("Error reading latest.log: " + e.getMessage());
        }
        return lines;
    }

    public String getServerStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(getLang("status_title")).append("\n");
        sb.append(getLang("online")).append(": ")
                .append(Bukkit.getOnlinePlayers().size()).append("/")
                .append(Bukkit.getMaxPlayers()).append("\n");
        sb.append(getLang("version")).append(": ").append(Bukkit.getVersion()).append("\n");

        try {
            Method m = Bukkit.getServer().getClass().getMethod("getTPS");
            double[] tps = (double[]) m.invoke(Bukkit.getServer());
            sb.append(getLang("tps")).append(": ").append(String.format("%.2f", tps[0])).append("\n");
        } catch (Exception ignored) {
            sb.append(getLang("tps")).append(": ").append(getLang("tps_unavailable")).append("\n");
        }
        return sb.toString();
    }

    public String getBotToken() { return botToken; }
    public String getBotUsername() { return botUsername; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /tgcontrol reload");
            return true;
        }

        if (!sender.hasPermission("tgcontrol.reload")) {
            sender.sendMessage("You don't have permission.");
            return true;
        }

        loadConfig();
        loadLanguage();

        sender.sendMessage("TelegramControl config and language reloaded successfully!");
        getLogger().info("TelegramControl reloaded by " + sender.getName());
        return true;
    }
}