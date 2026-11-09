<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ClockWidget from './components/ClockWidget.vue'
import BatteryWidget from './components/BatteryWidget.vue'
import MediaPlayerWidget from './components/MediaPlayerWidget.vue'
import ModeSelectorRow from './components/ModeSelectorRow.vue'
import FavoritesList from './components/FavoritesList.vue'
import AppDrawer from './components/AppDrawer.vue'
import UpdateBanner from './components/UpdateBanner.vue'
import SettingsModal from './components/SettingsModal.vue'
import { GitHubUpdater } from './services/updater'
import { ModesService } from './services/modes'
import { ZenAudio } from './services/audio'
import type { UpdateInfo } from './types'

const isDrawerOpen = ref(false)
const isSettingsOpen = ref(false)
const isNotificationShadeOpen = ref(false)
const showAddModeModal = ref(false)
const newModeName = ref('')
const updateInfo = ref<UpdateInfo | null>(null)
const toastMsg = ref('')
let toastTimer: number | null = null

// Quick Settings state in shade
const wifiActive = ref(true)
const btActive = ref(true)
const dndActive = ref(false)
const torchActive = ref(false)
const brightness = ref(85)

let touchStartX = 0
let touchStartY = 0
let lastTapTime = 0

function showToast(msg: string) {
  toastMsg.value = msg
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMsg.value = ''
  }, 2000)
}

function handleTouchStart(e: TouchEvent) {
  if (e.touches.length > 0) {
    touchStartX = e.touches[0].clientX
    touchStartY = e.touches[0].clientY
  }
}

function handleTouchEnd(e: TouchEvent) {
  if (e.changedTouches.length === 0) return
  const deltaX = e.changedTouches[0].clientX - touchStartX
  const deltaY = e.changedTouches[0].clientY - touchStartY
  const absX = Math.abs(deltaX)
  const absY = Math.abs(deltaY)

  // Double tap detection on empty screen
  const now = Date.now()
  if (absX < 15 && absY < 15 && now - lastTapTime < 320) {
    ZenAudio.tap()
    isSettingsOpen.value = true
    lastTapTime = 0
    return
  }
  lastTapTime = now

  if (absY > absX && absY > 40) {
    if (deltaY > 0) {
      // Swipe down: Open notification shade
      if (!isNotificationShadeOpen.value && !isDrawerOpen.value && !isSettingsOpen.value) {
        ZenAudio.tap()
        isNotificationShadeOpen.value = true
      }
    } else {
      // Swipe up: Open drawer
      if (!isNotificationShadeOpen.value && !isDrawerOpen.value && !isSettingsOpen.value) {
        ZenAudio.drawer()
        isDrawerOpen.value = true
      }
    }
  } else if (absX > absY && absX > 50) {
    // Swipe left / right: cycle Samsung Modes
    if (!isNotificationShadeOpen.value && !isDrawerOpen.value && !isSettingsOpen.value) {
      const modes = ModesService.state.allModes
      const current = ModesService.state.activeMode
      const idx = modes.findIndex(m => m.id === current.id)
      if (idx !== -1 && modes.length > 0) {
        ZenAudio.tick()
        const nextIdx = deltaX < 0
          ? (idx + 1) % modes.length
          : (idx - 1 < 0 ? modes.length - 1 : idx - 1)
        ModesService.selectMode(modes[nextIdx].id)
        showToast(`РЕЖИМ // ${modes[nextIdx].name.toUpperCase()}`)
      }
    }
  }
}

onMounted(async () => {
  // Check for GitHub Releases update in background
  try {
    const info = await GitHubUpdater.checkForUpdates()
    if (info.hasUpdate) {
      updateInfo.value = info
    }
  } catch (e) {
    // Silent ignore on startup
  }
})

function handleAddMode() {
  if (newModeName.value.trim()) {
    ZenAudio.tap()
    ModesService.addCustomMode(newModeName.value.trim())
    showAddModeModal.value = false
    newModeName.value = ''
    showToast('Режим создан!')
  }
}

function handleLaunchApp(appName: string) {
  showToast('Запуск: ' + appName)
}
</script>

<template>
  <div
    class="launcher-shell"
    @touchstart="handleTouchStart"
    @touchend="handleTouchEnd"
  >
    <!-- Top Invisible Pull Handle for Notification Shade -->
    <div
      class="top-pull-handle"
      title="Потяните вниз для вызова шторки"
      @click="isNotificationShadeOpen = true"
    >
      <div class="pull-bar"></div>
    </div>

    <!-- Main Screen (Full Screen OLED, System Status Bar Hidden) -->
    <div class="main-content">
      <!-- Clock Widget -->
      <ClockWidget />

      <!-- Expandable Battery Widget -->
      <BatteryWidget />

      <!-- Minimalist Media Player -->
      <MediaPlayerWidget />

      <!-- Auto Update Banner from GitHub Releases -->
      <UpdateBanner :update-info="updateInfo" @dismiss="updateInfo = null" />

      <div class="spacer"></div>

      <!-- Samsung Modes & Routines Row -->
      <ModeSelectorRow @add-mode="showAddModeModal = true" />

      <!-- Mode-dependent Favorites List -->
      <FavoritesList
        @open-drawer="isDrawerOpen = true"
        @open-settings="isSettingsOpen = true"
        @launch-app="handleLaunchApp"
      />
    </div>

    <!-- Right Edge Niagara Quick Alphabet Strip on Home Screen -->
    <div
      v-if="!isDrawerOpen && !isSettingsOpen"
      class="home-niagara-strip"
      title="Нажмите для открытия алфавитного водопада Niagara"
      @click="isDrawerOpen = true; ZenAudio.drawer()"
    >
      <span>A</span>
      <span>•</span>
      <span>Б</span>
      <span>•</span>
      <span>M</span>
      <span>•</span>
      <span>T</span>
      <span>•</span>
      <span>Я</span>
    </div>

    <!-- Pull-down Notification Shade (Шторка уведомлений Samsung / Nothing OS) -->
    <div
      class="notification-shade-overlay"
      :class="{ open: isNotificationShadeOpen }"
      @click.self="isNotificationShadeOpen = false"
    >
      <div class="notification-shade-panel">
        <div class="shade-header">
          <div class="shade-clock">
            <span class="shade-time">22:45</span>
            <span class="shade-date">ПН, 9 НОЯ</span>
          </div>
          <button class="shade-close-btn bounce-tap" @click="isNotificationShadeOpen = false">✕</button>
        </div>

        <!-- Quick Toggles Row -->
        <div class="quick-toggles-grid">
          <div
            class="toggle-card bounce-tap"
            :class="{ active: wifiActive }"
            @click="wifiActive = !wifiActive; ZenAudio.tap()"
          >
            <div class="toggle-icon">Wi-Fi</div>
            <div class="toggle-name">{{ wifiActive ? 'Подключено' : 'Выкл' }}</div>
          </div>

          <div
            class="toggle-card bounce-tap"
            :class="{ active: btActive }"
            @click="btActive = !btActive; ZenAudio.tap()"
          >
            <div class="toggle-icon">BT</div>
            <div class="toggle-name">{{ btActive ? 'Galaxy Buds' : 'Выкл' }}</div>
          </div>

          <div
            class="toggle-card bounce-tap"
            :class="{ active: dndActive }"
            @click="dndActive = !dndActive; ZenAudio.tap()"
          >
            <div class="toggle-icon">DND</div>
            <div class="toggle-name">{{ dndActive ? 'Не беспокоить' : 'Звук' }}</div>
          </div>

          <div
            class="toggle-card bounce-tap"
            :class="{ active: torchActive }"
            @click="torchActive = !torchActive; ZenAudio.tap()"
          >
            <div class="toggle-icon">ФОНАРЬ</div>
            <div class="toggle-name">{{ torchActive ? 'Вкл' : 'Выкл' }}</div>
          </div>
        </div>

        <!-- Brightness Slider -->
        <div class="brightness-row">
          <span class="brightness-label">ЯРКОСТЬ: {{ brightness }}%</span>
          <input v-model="brightness" type="range" min="10" max="100" class="brightness-slider" />
        </div>

        <!-- Active Notification Item -->
        <div class="notifications-list">
          <div class="notification-card">
            <div class="notif-header">
              <span class="notif-app">N.ECO LAUNCHER</span>
              <span class="notif-time">СЕЙЧАС</span>
            </div>
            <div class="notif-title">Полноэкранный режим активен</div>
            <div class="notif-body">Строка состояния скрыта. Свайпайте вниз для вызова шторки или свайпайте влево/вправо для смены режимов Samsung.</div>
          </div>
        </div>

        <div class="shade-footer" @click="isNotificationShadeOpen = false">
          <span>СВЕРНУТЬ ШТОРКУ ↑</span>
        </div>
      </div>
    </div>

    <!-- Full Screen App Drawer -->
    <AppDrawer
      :is-open="isDrawerOpen"
      @close="isDrawerOpen = false"
      @launch-app="handleLaunchApp"
    />

    <!-- Settings Modal -->
    <SettingsModal
      :is-open="isSettingsOpen"
      @close="isSettingsOpen = false"
      @update-found="(info) => updateInfo = info"
    />

    <!-- Add Custom Mode Dialog -->
    <div v-if="showAddModeModal" class="add-mode-modal" @click.self="showAddModeModal = false">
      <div class="add-mode-box">
        <div class="box-title">НОВЫЙ РЕЖИМ SAMSUNG</div>
        <p class="box-desc">Введите название режима (например: Учеба, Фокус):</p>
        <input
          v-model="newModeName"
          type="text"
          placeholder="Название режима..."
          @keyup.enter="handleAddMode"
        />
        <div class="box-actions">
          <button class="cancel-btn bounce-tap" @click="showAddModeModal = false">ОТМЕНА</button>
          <button class="confirm-btn bounce-tap" @click="handleAddMode">СОЗДАТЬ</button>
        </div>
      </div>
    </div>

    <!-- Floating Toast -->
    <div class="toast" :class="{ show: !!toastMsg }">
      {{ toastMsg }}
    </div>
  </div>
</template>

<style scoped>
.launcher-shell {
  width: 100vw;
  height: 100vh;
  background-color: var(--bg-black);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  user-select: none;
}

.top-pull-handle {
  width: 100%;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 40;
  cursor: pointer;
}

.pull-bar {
  width: 38px;
  height: 3px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
  transition: all 0.2s ease;
}

.top-pull-handle:hover .pull-bar {
  background: var(--accent);
  width: 48px;
}

.main-content {
  flex: 1;
  padding: 24px 20px 16px 20px;
  display: flex;
  flex-direction: column;
}

.spacer {
  flex: 1;
  min-height: 32px;
}

.home-niagara-strip {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 10px 4px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  user-select: none;
  transition: all 0.2s ease;
  z-index: 25;
}

.home-niagara-strip:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--accent);
  border-color: var(--accent);
}

/* Notification Shade Styles */
.notification-shade-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(14px);
  z-index: 80;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.notification-shade-overlay.open {
  opacity: 1;
  pointer-events: auto;
}

.notification-shade-panel {
  width: 100%;
  background: #0d0d11;
  border-bottom: 1px solid var(--border);
  border-radius: 0 0 24px 24px;
  padding: 24px 20px 16px 20px;
  transform: translateY(-100%);
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.notification-shade-overlay.open .notification-shade-panel {
  transform: translateY(0);
}

.shade-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.shade-clock {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.shade-time {
  font-family: var(--font-dot);
  font-size: 32px;
  color: var(--text-main);
  letter-spacing: 2px;
}

.shade-date {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 1px;
}

.shade-close-btn {
  background: #18181d;
  border: 1px solid var(--border);
  color: var(--text-main);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
}

.quick-toggles-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.toggle-card {
  background: #15151a;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px 6px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-card.active {
  background: #24242d;
  border-color: var(--accent);
}

.toggle-icon {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: bold;
  color: var(--text-main);
  margin-bottom: 4px;
}

.toggle-card.active .toggle-icon {
  color: var(--accent);
}

.toggle-name {
  font-size: 10px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.brightness-row {
  background: #15151a;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 10px 14px;
  margin-bottom: 16px;
}

.brightness-label {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  display: block;
  margin-bottom: 6px;
}

.brightness-slider {
  width: 100%;
  accent-color: var(--accent);
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.notification-card {
  background: #15151a;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px 14px;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.notif-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 2px;
}

.notif-body {
  font-size: 11px;
  color: var(--text-sub);
  line-height: 15px;
}

.shade-footer {
  text-align: center;
  padding-top: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  cursor: pointer;
  letter-spacing: 1.5px;
}

.shade-footer:hover {
  color: var(--accent);
}

.add-mode-modal {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(14px);
  z-index: 70;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.add-mode-box {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 20px;
  width: 100%;
  max-width: 320px;
  animation: popIn 0.25s var(--spring-physics);
}

@keyframes popIn {
  from { opacity: 0; transform: scale(0.92); }
  to { opacity: 1; transform: scale(1); }
}

.box-title {
  font-family: var(--font-mono);
  font-weight: bold;
  font-size: 13px;
  letter-spacing: 1.5px;
  color: var(--text-main);
}

.box-desc {
  font-size: 12px;
  color: var(--text-sub);
  margin: 8px 0 14px 0;
}

.add-mode-box input {
  width: 100%;
  background: #09090c;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 12px;
  color: var(--text-main);
  font-size: 14px;
  outline: none;
  margin-bottom: 16px;
}

.box-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 12px;
  cursor: pointer;
  padding: 6px 10px;
}

.confirm-btn {
  background: var(--accent);
  color: #fff;
  border: none;
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: bold;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
}

.toast {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%) translateY(50px);
  background: #1f1f24;
  border: 1px solid var(--border);
  color: var(--text-main);
  font-size: 12px;
  font-family: var(--font-mono);
  padding: 8px 16px;
  border-radius: 20px;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s var(--spring-physics);
  z-index: 100;
  white-space: nowrap;
}

.toast.show {
  transform: translateX(-50%) translateY(0);
  opacity: 1;
}
</style>