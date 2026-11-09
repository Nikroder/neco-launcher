<script setup lang="ts">
import { ref, computed } from 'vue'
import { ModesService } from '../services/modes'
import { ZenAudio } from '../services/audio'
import type { AppInfo } from '../types'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'launch-app', name: string): void
}>()

const searchQuery = ref('')

const defaultApps: AppInfo[] = [
  { packageName: 'browser', label: 'Браузер', tag: 'CH' },
  { packageName: 'clock', label: 'Будильник', tag: 'AL' },
  { packageName: 'gallery', label: 'Галерея', tag: 'GL' },
  { packageName: 'voice', label: 'Диктофон', tag: 'RC' },
  { packageName: 'notes', label: 'Заметки', tag: 'NT' },
  { packageName: 'calculator', label: 'Калькулятор', tag: 'CL' },
  { packageName: 'calendar', label: 'Календарь', tag: 'CD' },
  { packageName: 'camera', label: 'Камера', tag: 'CM' },
  { packageName: 'contacts', label: 'Контакты', tag: 'CT' },
  { packageName: 'music', label: 'Музыка', tag: 'MS' },
  { packageName: 'settings', label: 'Настройки', tag: 'ST' },
  { packageName: 'weather', label: 'Погода', tag: 'WT' },
  { packageName: 'messages', label: 'Сообщения', tag: 'MS' },
  { packageName: 'phone', label: 'Телефон', tag: 'PH' },
  { packageName: 'files', label: 'Файлы', tag: 'FL' },
  { packageName: 'gmail', label: 'Gmail', tag: 'GM' },
  { packageName: 'slack', label: 'Slack', tag: 'SL' },
  { packageName: 'spotify', label: 'Spotify', tag: 'SP' },
  { packageName: 'strava', label: 'Strava', tag: 'ST' },
  { packageName: 'telegram', label: 'Telegram', tag: 'TG' },
  { packageName: 'youtube', label: 'YouTube', tag: 'YT' },
  { packageName: 'zoom', label: 'Zoom', tag: 'ZM' },
]

const filteredApps = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return defaultApps
  return defaultApps.filter(a => a.label.toLowerCase().includes(q))
})

const activeMode = computed(() => ModesService.getActiveMode())

function onInput() {
  ZenAudio.tick()
}

function onAppClick(app: AppInfo) {
  ZenAudio.open()
  emit('launch-app', app.label)
  emit('close')
}

function togglePin(app: AppInfo, event: Event) {
  event.stopPropagation()
  ZenAudio.tap()
  ModesService.toggleFavorite(app.label)
}

function isPinned(app: AppInfo): boolean {
  return activeMode.value.favorites.includes(app.label)
}

function close() {
  ZenAudio.back()
  emit('close')
}
</script>

<template>
  <div class="app-drawer" :class="{ open: isOpen }">
    <div class="drawer-handle" @click="close"></div>

    <!-- Search Input -->
    <div class="search-box">
      <span class="search-icon">🔍</span>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Поиск приложений..."
        @input="onInput"
      />
      <span v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">✕</span>
    </div>

    <!-- Header info -->
    <div class="drawer-info">
      <span>ВСЕ ПРИЛОЖЕНИЯ ({{ filteredApps.length }})</span>
      <span class="mode-tag">РЕЖИМ: {{ activeMode.name.toUpperCase() }}</span>
    </div>

    <!-- Apps scroll list -->
    <div class="apps-list">
      <div
        v-for="app in filteredApps"
        :key="app.packageName"
        class="drawer-item bounce-tap"
        @click="onAppClick(app)"
      >
        <div class="app-icon-mono">{{ app.tag }}</div>
        <div class="app-label">{{ app.label }}</div>
        <div
          class="pin-btn"
          :class="{ pinned: isPinned(app) }"
          @click="togglePin(app, $event)"
          title="Закрепить в текущем режиме"
        >
          {{ isPinned(app) ? '★' : '☆' }}
        </div>
      </div>
    </div>

    <div class="home-indicator" @click="close"></div>
  </div>
</template>

<style scoped>
.app-drawer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.96);
  backdrop-filter: blur(24px);
  z-index: 50;
  display: flex;
  flex-direction: column;
  transform: translateY(100%);
  transition: transform 0.38s var(--spring-physics);
  padding: 16px 20px;
}

.app-drawer.open {
  transform: translateY(0);
}

.drawer-handle {
  width: 36px;
  height: 4px;
  background: #2d2d32;
  border-radius: 2px;
  margin: 4px auto 14px auto;
  cursor: pointer;
}

.search-box {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-main);
  font-size: 15px;
  font-family: var(--font-sans);
  width: 100%;
}

.search-icon {
  font-size: 14px;
  color: var(--text-sub);
}

.clear-btn {
  color: var(--text-muted);
  cursor: pointer;
  font-size: 12px;
}

.drawer-info {
  display: flex;
  justify-content: space-between;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1.5px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.mode-tag {
  color: var(--accent);
}

.apps-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-right: 4px;
}

.apps-list::-webkit-scrollbar {
  width: 3px;
}

.apps-list::-webkit-scrollbar-thumb {
  background: #2a2a30;
  border-radius: 2px;
}

.drawer-item {
  display: flex;
  align-items: center;
  padding: 9px 12px;
  border-radius: 12px;
  cursor: pointer;
  gap: 12px;
}

.app-icon-mono {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #161619;
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 12px;
  color: var(--text-sub);
}

.app-label {
  font-size: 15px;
  color: var(--text-main);
  flex: 1;
}

.pin-btn {
  font-size: 18px;
  color: var(--text-muted);
  padding: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.pin-btn.pinned {
  color: var(--accent);
}

.home-indicator {
  width: 110px;
  height: 4px;
  background: #2d2d32;
  border-radius: 2px;
  margin: 10px auto 4px auto;
  cursor: pointer;
}
</style>
