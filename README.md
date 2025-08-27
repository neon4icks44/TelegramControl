# TelegramControl 🎮🔥

**TelegramControl** — это 💥 мощный плагин для управления Minecraft-сервером (Spigot/Paper) через Telegram! 😺 Проверяй онлайн, выполняй команды, читай логи консоли — всё в пару кликов! Плагин идеально подходит для новичков и опытных админов, с простой настройкой и крутыми функциями. 🌟

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Spigot/Paper](https://img.shields.io/badge/Spigot%2FPaper-1.16.5%2B-blue)
![Java](https://img.shields.io/badge/Java-17-orange)

---

## 🚀 Возможности

- **Контроль сервера** 🕹️:
    - 📊 **Статус сервера**: Количество игроков онлайн, версия, TPS (через рефлексию для старых версий).
    - 📜 **Логи консоли**: Просматривай последние строки логов прямо в Telegram.
    - ⚡ **Команды**: Выполняй любые команды сервера (например, `/say Привет!`).
    - 🔑 **Авторизация**: Вход по логину и паролю для безопасности.
- **Логирование** 📝:
    - Все действия (вход, выход, команды, запросы статуса/логов) записываются в консоль с датой и Chat ID.
- **Безопасность** 🔒:
    - Защита через логин и пароль.
    - Запрещённые команды (`stop`, `reload`, `op`, `deop`) для предотвращения злоупотреблений.
- **Совместимость** 🛠️:
    - Поддержка Spigot/Paper 1.16.5+.
    - Получение TPS через рефлексию для совместимости со старыми версиями.
- **Простота** 😎:
    - Настройка через `config.yml` — разберётся даже школьник!

---

## 📋 Требования

| Требование           | Версия          |
|----------------------|-----------------|
| Minecraft Server     | Spigot/Paper 1.16.5+ |
| Java                 | 17 или выше     |
| Зависимости          | Spigot API, TelegramBots (в `pom.xml`) |

---

## 🛠️ Установка

1. **Скачай плагин** 📥:
    - Перейди в [Releases](https://github.com/neon4icks44/TelegramControl/releases) и скачай `TelegramControl.jar`.
2. **Добавь на сервер** ⚙️:
    - Скопируй `.jar` в папку `plugins/` твоего сервера.
3. **Создай Telegram-ботов** 🤖:
    - Открой Telegram, найди [@BotFather](https://t.me/BotFather).
    - Создай бота для управления:
        - Выполни `/newbot`, следуй инструкциям.
        - Сохрани **токен** (например, `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`) и имя бота (например, `@MyControlBot`).
    - (Опционально) Создай второго бота для уведомлений.
4. **Настрой `config.yml`** 📝:
    - После первого запуска плагина открой `plugins/TelegramControl/config.yml` в текстовом редакторе (например, Блокнот).
    - Укажи:
      ```yaml
      bot:
        token: "YOUR_BOT_TOKEN_HERE" # Токен ControlBot
        username: "YourBotUsername"   # Имя бота без @ (например, MyControlBot)
      auth:
        login: "admin"               # Логин
        password: "securepassword"   # Пароль (обязательно поменяй!)
      forbidden_commands:
        - "stop"
        - "reload"
        - "op"
        - "deop"
      log_lines: 10                 # Количество строк логов
      ```
5. **Перезапусти сервер** 🔄:
    - Запусти сервер, и боты начнут работать!

---

## 🎉 Как пользоваться?

1. **Авторизация** 🔑:
    - Напиши `/login` в чате с ControlBot.
    - Введи логин и пароль из `config.yml` (например, `admin` и `securepassword`).
    - Получи клавиатуру с командами.

2. **Команды** 🖱️:
    - Используй кнопки:
        - **Статус сервера** 📊: Показывает онлайн, версию и TPS.
        - **Консоль** 📜: Отправляет последние строки логов.
        - **Команда** ⚡: Введи команду (например, `/list`).
        - **Выйти** 🚪: Завершает сессию.
    - Или отправляй команды напрямую, например, `/say Привет!`.

3. **Логирование** 🕒:
    - Все действия записываются в консоль сервера:
      ```
      [2025-08-27 14:30:00 INFO] [TelegramControl] User with Chat ID 123456789 logged in with login admin at 2025-08-27 14:30:00 (IP: Unknown)
      [2025-08-27 14:30:05 INFO] [TelegramControl] User with Chat ID 123456789 executed command: say Привет at 2025-08-27 14:30:05, output: [Server] Привет
      ```

---

## 🖥️ Логирование

Плагин записывает в консоль сервера:

- **Вход/выход**: Успешные и неуспешные попытки авторизации.
- **Команды**: Выполненные команды и их вывод.
- **Запросы**: Статус сервера и логи консоли.

Пример логов:
```
[2025-08-27 14:30:00 INFO] [TelegramControl] Failed login attempt for Chat ID 123456789 with login admin at 2025-08-27 14:30:00
[2025-08-27 14:30:05 INFO] [TelegramControl] User with Chat ID 123456789 requested console logs at 2025-08-27 14:30:05
```

**TPS через рефлексию**:
- Для старых версий Spigot/Paper плагин использует рефлексию для получения TPS, обеспечивая совместимость.

---

## 🔧 Для разработчиков

1. **Клонируй репозиторий**:
   ```powershell
   git clone https://github.com/neon4icks44/TelegramControl.git
   ```
2. **Открой в IDE**:
    - Используй IntelliJ IDEA (или другую IDE с поддержкой Maven).
3. **Убедись в зависимостях**:
    - Проверь `pom.xml`, чтобы Maven загрузил Spigot API и TelegramBots.
4. **Скомпилируй**:
   ```powershell
   mvn clean package
   ```
    - Найди `.jar` в папке `target/`.

---

## 📜 Лицензия

[MIT License](LICENSE) — используй, изменяй, делись! 😎

## ❓ Нужна помощь?

- Создай [Issue](https://github.com/neon4icks44/TelegramControl/issues) на GitHub.
- Напиши на [SpigotMC](https://www.spigotmc.org) после публикации плагина.

---

## 🙌 Благодарности

- [SpigotMC](https://www.spigotmc.org) за крутой API! 🛠️
- [TelegramBots](https://github.com/rubenlagus/TelegramBots) за интеграцию с Telegram. 📩
- Всем, кто использует TelegramControl! ❤️

---

**Сделано с 💖 by Neon4ick**  
*Управляй своим сервером с кайфом! 😺*
