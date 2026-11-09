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
const showAddModeModal = ref(false)
const newModeName = ref('')
const updateInfo = ref<UpdateInfo | null>(null)
const toastMsg = ref('')
let toastTimer: number | null = null

function showToast(msg: string) {
  toastMsg.value = msg
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMsg.value = ''
  }, 2000)
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
  <div class="launcher-shell">
    <!-- Top System Status Bar -->
    <div class="status-bar">
      <div class="status-left">
        <span class="status-dot"></span>
        <span class="status-carrier">N.ECO</span>
      </div>
      <div class="status-right">
        <span>5G</span>
        <span>87%</span>
      </div>
    </div>

    <!-- Main Screen -->
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
  width: 100%;
  height: 100%;
  background: var(--bg);
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: 38px;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  font-family: var(--font-mono);
  color: var(--text-sub);
  z-index: 10;
}

.status-left,
.status-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.main-content {
  flex: 1;
  padding: 8px 22px 14px 22px;
  display: flex;
  flex-direction: column;
}

.spacer {
  flex: 1;
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
