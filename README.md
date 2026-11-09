# Zen Minimalist Launcher (Vue 3 + Rust / Tauri Mobile)

> **Минималистичный Android-лаунчер на стеке VUE 3 + RUST с эстетикой Nothing OS, медитативным звуковым профилем (Zen), динамическим медиа-плеером, поддержкой Режимов Samsung, зарядом подключенных устройств и автообновлением через GitHub Releases.**

---

## ✦ Стек технологий

* **Frontend**: **Vue 3** (Composition API, `<script setup>`, Vite 5)
  - Глубокая OLED-тема (`#000000`)
  - Плавные пружинные анимации (`cubic-bezier(0.16, 1, 0.3, 1)`)
  - Синтезатор звуков системы Zen на базе Web Audio API + Haptic feedback
* **Backend**: **Rust** (`src-tauri` / Tauri v2 Mobile)
  - JNI-мост с Android SDK
  - Мониторинг Bluetooth-устройств и заряда батареи
  - Клиент автообновления через GitHub Releases API
  - Вызов Android Package Installer через FileProvider
* **CI/CD & Сборка APK**: **GitHub Actions** (`.github/workflows/build-apk.yml`)
  - Автоматическая компиляция Vue + Rust + Android NDK в облаке
  - Сборка и публикация APK в GitHub Releases при каждом новом теге

---

## 🚀 Как подключить к своему GitHub репозиторию

Репозиторий уже инициализирован локально на ветке `main`. Чтобы отправить его на GitHub:

1. Создайте новый пустой репозиторий на [GitHub](https://github.com/new) (например, `zen-launcher`).
2. В терминале выполните:
```bash
git remote add origin https://github.com/<ВАШ_GITHUB_ЛОГИН>/zen-launcher.git
git push -u origin main
```

---

## 📦 Сборка APK и автообновление через GitHub Releases

### 1. Как собирается APK
В репозиторий встроен workflow [build-apk.yml](file:///d:/Минималистичный%20лаунчер/.github/workflows/build-apk.yml):
* При каждом `push` в ветку `main` GitHub Actions автоматически компилирует Vue-фронтенд, собирает Rust и генерирует готовый `app-debug.apk` в разделе **Actions** -> **Artifacts**.

### 2. Как выпустить релиз с автообновлением
Чтобы опубликовать новую версию лаунчера:
```bash
git tag v1.0.1
git push origin v1.0.1
```
GitHub Actions автоматически:
1. Соберет APK в окружении Ubuntu + Android SDK/NDK.
2. Создаст официальный **GitHub Release** с тегом `v1.0.1`.
3. Прикрепит файл `zen-launcher-v1.0.1.apk` к релизу.

### 3. Как работает автообновление на смартфоне
* При запуске лаунчер опрашивает GitHub API: `https://api.github.com/repos/<owner>/<repo>/releases/latest`.
* Если обнаружена новая версия (например, `v1.0.1` при текущей `v1.0.0`):
  - На главном экране лаунчера всплывает баннер **«ДОСТУПНА ВЕРСИЯ v1.0.1»** с описанием изменений.
  - Нажатие на кнопку **«СКАЧАТЬ И ОБНОВИТЬ APK»** скачивает файл и запускает системный установщик Android (через FileProvider).
* Репозиторий для проверки обновлений можно указать в `ZEN // PREFS -> АВТООБНОВЛЕНИЕ ЧЕРЕЗ GITHUB RELEASES`.

---

## 📂 Структура проекта

```
d:\Минималистичный лаунчер/
├── .github/
│   └── workflows/
│       └── build-apk.yml            # CI/CD: сборка APK и публикация релиза на GitHub
├── src/                             # VUE 3 ФРОНТЕНД
│   ├── App.vue                      # Главный экран лаунчера
│   ├── components/
│   │   ├── ClockWidget.vue          # Часы и дата Nothing OS
│   │   ├── BatteryWidget.vue        # Раскрывающийся виджет заряда подключенных устройств
│   │   ├── MediaPlayerWidget.vue    # Медиа-плеер с элементами управления
│   │   ├── ModeSelectorRow.vue      # Селектор Режимов Samsung (Modes & Routines)
│   │   ├── FavoritesList.vue        # Список избранного с пружинной анимацией
│   │   ├── AppDrawer.vue            # Выдвижная шторка со всеми приложениями и поиском
│   │   ├── UpdateBanner.vue         # Баннер обновления через GitHub Releases
│   │   └── SettingsModal.vue        # Настройки звуков, режимов и GitHub repo
│   ├── services/
│   │   ├── updater.ts               # Клиент GitHub Releases API
│   │   ├── audio.ts                 # Звуковой движок Zen (5 тонов)
│   │   └── modes.ts                 # Менеджер режимов Samsung
│   ├── types/
│   │   └── index.ts                 # TypeScript-интерфейсы
│   ├── style.css                    # OLED палитра и пружинные анимации
│   └── main.ts                      # Точка входа Vue
├── src-tauri/                       # RUST БЭКЕНД
│   ├── Cargo.toml                   # Rust зависимости (Tauri 2, reqwest, tokio)
│   ├── tauri.conf.json              # Конфигурация мобильного лаунчера
│   └── src/
│       ├── lib.rs                   # Rust-команды автообновления и системных вызовов
│       └── main.rs
├── package.json                     # Скрипты Vite и зависимости Vue 3
├── vite.config.ts                   # Конфигурация Vite
├── preview/
│   └── index.html                   # Автономное превью для проверки в браузере
└── .gitignore
```

---

## ⚡ Локальная разработка и тестирование

* **Запуск Vue-фронтенда в режиме разработки**:
  ```powershell
  npm run dev
  ```
* **Сборка продакшен-бандла**:
  ```powershell
  npm run build
  ```
* **Автономное тестирование в браузере**:
  Откройте [preview/index.html](file:///d:/Минималистичный%20лаунчер/preview/index.html) или запустите локальный сервер.
