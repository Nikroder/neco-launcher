# Zen Minimalist Launcher (Android)

> **Минималистичный, стильный лаунчер на Android с эстетикой Nothing OS, медитативным звуковым профилем (Zen), динамическим медиа-плеером, поддержкой Режимов Samsung (Modes & Routines), мониторингом заряда подключенных устройств и плавными пружинными анимациями.**

---

## ✦ Новые возможности

### 🔋 1. Мониторинг заряда подключенных устройств (Bluetooth & Samsung Ecosystem)
* **Интеллектуальная раскрывающаяся карточка**:
  - В обычном состоянии показывает заряд смартфона и бейдж подключенных аксессуаров (например, `+3 DEV`).
  - **По тапу плавно раскрывается** с пружинной физикой, отображая заряд каждого подключенного устройства:
    - 📱 Смартфон (Galaxy S24 Ultra / др.)
    - 🎧 Наушники (Galaxy Buds, AirPods, гарнитуры)
    - ⌚ Смарт-часы (Galaxy Watch, WearOS)
    - ✏️ Стилус (S Pen) / другие Bluetooth-устройства
* **Точечно-матричные индикаторы**: каждое устройство имеет аккуратный 5-сегментный матричный индикатор и точный процент.
* **Техническая реализация**: [ConnectedDevicesManager.kt](file:///d:/Минималистичный%20лаунчер/app/src/main/java/com/zenlauncher/minimalist/battery/ConnectedDevicesManager.kt) + [BatteryWidget.kt](file:///d:/Минималистичный%20лаунчер/app/src/main/java/com/zenlauncher/minimalist/ui/components/BatteryWidget.kt) с автообновлением по broadcast-событиям `BATTERY_LEVEL_CHANGED` и `ACTION_ACL_CONNECTED`.

### ✨ 2. Плавная пружинная кинематика (Smooth Animations)
* **Тактильный микро-отклик кнопок**: модификатор [ZenAnimations.kt](file:///d:/Минималистичный%20лаунчер/app/src/main/java/com/zenlauncher/minimalist/ui/animation/ZenAnimations.kt) (`bounceClick`) с коэффициентом затухания `dampingRatio = 0.82f` — элементы интерфейса приятно «пружинят» при касании.
* **Бесшовный переход между режимами Samsung**: при смене режима (`Обычный` -> `Работа` -> `Отдых`...) список избранного анимируется плавным каскадным сдвигом и растворением (`AnimatedContent` + `FastOutSlowInEasing`).
* **Органичное раскрытие виджетов**: анимация `animateContentSize` с плавной физической деселерацией при разворачивании батареи и плеера.

---

## ⚡ Интерактивный тест в браузере

Файл [preview/index.html](file:///d:/Минималистичный%20лаунчер/preview/index.html) обновлен:
1. **Нажмите на плашку батареи** (`BATTERY +3 DEV`) — карточка шелковисто развернется, показав телефон, Galaxy Buds2 Pro, Galaxy Watch6 и S Pen.
2. **Переключайте режимы Samsung**: обратите внимание на каскадную плавную смену списка приложений и пружинные отклики кнопок.
3. **Управляйте медиа-плеером**: мягкая пульсация индикатора воспроизведения и переключение треков.

---

## 📂 Структура ключевых компонентов

```
app/src/main/java/com/zenlauncher/minimalist/
├── battery/
│   ├── ConnectedDevice.kt           # Модель устройства (Телефон, Наушники, Часы, Стилус)
│   └── ConnectedDevicesManager.kt   # Сбор уровня заряда через Bluetooth и систему
├── ui/
│   ├── animation/
│   │   └── ZenAnimations.kt         # Пружинные характеристики и тактильный bounceClick
│   └── components/
│       ├── BatteryWidget.kt         # Раскрывающийся виджет устройств с плавной анимацией
│       ├── MediaPlayerWidget.kt     # Карточка медиа-плеера Nothing OS
│       ├── ModeSelectorRow.kt       # Плавный селектор режимов Samsung
│       └── FavoritesList.kt         # Каскадно анимированный список избранного
```
