# TelegramControl 🎮🔥

**TelegramControl** is a 💥 powerful plugin for managing your Minecraft server (Spigot/Paper) via Telegram! 😺  
Check online status, execute commands, read console logs — all in just a couple of clicks!  
The plugin is perfect for beginners and experienced admins, with simple setup and awesome features. 🌟

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Spigot/Paper](https://img.shields.io/badge/Spigot%2FPaper-1.16.5%2B-blue)
![Java](https://img.shields.io/badge/Java-17-orange)

---

## 🚀 Features

- **Server Control** 🕹️:
    - 📊 **Server Status**: Number of online players, version, TPS (via reflection for older versions).
    - 📜 **Console Logs**: View the latest log lines directly in Telegram.
    - ⚡ **Commands**: Execute any server commands (e.g., `/say Hello!`).
    - 🔑 **Authorization**: Login with username and password for security.

- **Logging** 📝:
    - All actions (login, logout, commands, status/logs requests) are logged to the console with date and Chat ID.

- **Security** 🔒:
    - Protection via login and password.
    - Forbidden commands (`stop`, `reload`, `op`, `deop`) to prevent abuse.

- **Compatibility** 🛠️:
    - Supports Spigot/Paper 1.16.5+.
    - TPS retrieval via reflection for compatibility with older versions.

- **Simplicity** 😎:
    - Configuration via `config.yml` — even a beginner can figure it out!

---

## 📋 Requirements

| Requirement          | Version                  |
|----------------------|--------------------------|
| Minecraft Server     | Spigot/Paper 1.16.5+     |
| Java                 | 17 or higher             |
| Dependencies         | Spigot API, TelegramBots (in `pom.xml`) |

---

## 🛠️ Installation

1. **Download the plugin** 📥:
    - Go to [Releases](https://github.com/neon4icks44/TelegramControl/releases) and download `TelegramControl.jar`.

2. **Add to the server** ⚙️:
    - Copy the `.jar` file into your server's `plugins/` folder.

3. **Create Telegram bots** 🤖:
    - Open Telegram and message [@BotFather](https://t.me/BotFather).
    - Create a bot for control:
        - Send `/newbot` and follow the instructions.
        - Save the **token** (e.g., `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`) and the bot username (e.g., `@MyControlBot`).
    - (Optional) Create a second bot for notifications.

4. **Configure `config.yml`** 📝:
    - After the first server start, open `plugins/TelegramControl/config.yml` in a text editor (e.g., Notepad).
    - Set the following:
      ```yaml
      language: en
      
      bot:
        token: "YOUR_BOT_TOKEN_HERE" # ControlBot token
        username: "YourBotUsername" # Bot username without @ (e.g., MyControlBot)
      
      auth:
        login: "admin" # Login
        password: "securepassword" # Password (be sure to change it!)
      
      forbidden_commands:
        - "stop"
        - "reload"
        - "op"
        - "deop"
      
      log_lines: 10 # Number of log lines
      ```
5. **Restart the server** 🔄:
    - Restart the server, and the bots will start working!

---

## 🎉 How to Use

1. **Authorization** 🔑:
    - Message your ControlBot and send `/login`.
    - Enter the login and password from `config.yml` (e.g., `admin` and `securepassword`).
    - You will receive a keyboard with commands.

2. **Commands** 🖱️:
    - Use the buttons:
        - **Server Status** 📊: Shows online players, version, and TPS.
        - **Console** 📜: Sends the latest log lines.
        - **Command** ⚡: Enter a command (e.g., `/list`).
        - **Logout** 🚪: Ends the session.
    - Or send commands directly, e.g., `/say Hello!`.

---

**TPS via Reflection**:
- For older Spigot/Paper versions, the plugin uses reflection to obtain TPS, ensuring compatibility.

---

## 📜 License

[MIT License](LICENSE) — use, modify, share! 😎

## ❓ Need Help?

- Create an [Issue](https://github.com/neon4icks44/TelegramControl/issues) on GitHub.
- Post on [SpigotMC](https://www.spigotmc.org) after the plugin is published there.

---

## 🙌 Credits

- [SpigotMC](https://www.spigotmc.org) for the awesome API! 🛠️
- [TelegramBots](https://github.com/rubenlagus/TelegramBots) for Telegram integration. 📩
- Everyone who uses TelegramControl! ❤️

---

**Made with 💖 by Neon4ick**  
*Manage your server with pleasure! 😺*
=======
*Manage your server with pleasure! 😺*
