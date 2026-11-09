<script setup lang="ts">
import { ref, computed } from 'vue'
import { ModesService } from '../services/modes'
import { ZenAudio } from '../services/audio'
import type { AppInfo } from '../types'

const props = defineProps<{
  isOpen: boolean
  initialLetter?: string | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'launch-app', name: string): void
}>()

const searchQuery = ref('')
const activeWaveLetter = ref<string | null>(null)
const isWaveDragging = ref(false)

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

const alphabet = computed(() => {
  const set = new Set<string>()
  for (const app of filteredApps.value) {
    set.add(app.label.charAt(0).toUpperCase())
  }
  return Array.from(set).sort()
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

function scrollToLetter(letter: string) {
  if (activeWaveLetter.value !== letter) {
    activeWaveLetter.value = letter
    ZenAudio.tick()
  }
  const el = document.getElementById('letter-' + letter)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function onWaveTouchMove(e: TouchEvent) {
  if (e.touches.length === 0) return
  isWaveDragging.value = true
  const touch = e.touches[0]
  const target = document.elementFromPoint(touch.clientX, touch.clientY)
  if (target && target.getAttribute('data-letter')) {
    const letter = target.getAttribute('data-letter')!
    scrollToLetter(letter)
  }
}

function onWaveTouchEnd() {
  isWaveDragging.value = false
  setTimeout(() => {
    activeWaveLetter.value = null
  }, 400)
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
        placeholder="Поиск приложений (Niagara Waterfall)..."
        @input="onInput"
      />
      <span v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">✕</span>
    </div>

    <!-- Header info -->
    <div class="drawer-info">
      <span>ВОДОПАД ПРИЛОЖЕНИЙ ({{ filteredApps.length }})</span>
      <span class="mode-tag">РЕЖИМ: {{ activeMode.name.toUpperCase() }}</span>
    </div>

    <!-- Apps Waterfall with Niagara Wave Alphabet Strip -->
    <div class="drawer-body">
      <!-- Apps scroll list -->
      <div class="apps-list">
        <template v-for="(app, idx) in filteredApps" :key="app.packageName">
          <!-- Letter header divider if starting new letter -->
          <div
            v-if="idx === 0 || app.label.charAt(0).toUpperCase() !== filteredApps[idx - 1].label.charAt(0).toUpperCase()"
            :id="'letter-' + app.label.charAt(0).toUpperCase()"
            class="letter-section-header"
          >
            {{ app.label.charAt(0).toUpperCase() }}
          </div>

          <div
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
        </template>
      </div>

      <!-- Niagara Wave Alphabet Slider -->
      <div
        v-if="alphabet.length > 2 && !searchQuery"
        class="niagara-alphabet-slider"
        @touchmove.prevent="onWaveTouchMove"
        @touchend="onWaveTouchEnd"
      >
        <div
          v-for="char in alphabet"
          :key="char"
          class="alphabet-char"
          :class="{ active: activeWaveLetter === char }"
          :data-letter="char"
          @mouseenter="scrollToLetter(char)"
          @click="scrollToLetter(char)"
        >
          {{ char }}
        </div>
      </div>
    </div>

    <!-- Floating Niagara Large Letter Bubble -->
    <div v-if="activeWaveLetter" class="floating-letter-bubble">
      <span>{{ activeWaveLetter }}</span>
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
  background: rgba(0, 0, 0, 0.97);
  backdrop-filter: blur(24px);
  z-index: 50;
  display: flex;
  flex-direction: column;
  transform: translateY(100%);
  transition: transform 0.38s var(--spring-physics);
  padding: 16px 14px 16px 20px;
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
  margin-right: 6px;
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-main);
  font-size: 14px;
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
  margin-right: 6px;
}

.mode-tag {
  color: var(--accent);
}

.drawer-body {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

.apps-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-right: 8px;
}

.apps-list::-webkit-scrollbar {
  width: 0;
  display: none;
}

.letter-section-header {
  font-family: var(--font-mono);
  font-weight: bold;
  font-size: 11px;
  color: var(--accent);
  padding: 8px 12px 2px 12px;
  letter-spacing: 2px;
}

.drawer-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 12px;
  cursor: pointer;
  gap: 12px;
  transition: background 0.15s ease;
}

.drawer-item:hover {
  background: rgba(255, 255, 255, 0.04);
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

/* Niagara Wave Alphabet Slider */
.niagara-alphabet-slider {
  width: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  user-select: none;
  touch-action: none;
  padding: 6px 0;
  cursor: pointer;
}

.alphabet-char {
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
  width: 100%;
  text-align: center;
  transition: all 0.15s ease;
  padding: 1px 0;
}

.alphabet-char.active {
  color: var(--accent);
  font-weight: bold;
  transform: translateX(-12px) scale(1.4);
}

/* Floating Letter Bubble */
.floating-letter-bubble {
  position: absolute;
  top: 50%;
  right: 50px;
  transform: translateY(-50%);
  width: 64px;
  height: 64px;
  background: #000;
  border: 1.5px solid var(--accent);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: bold;
  color: var(--text-main);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  pointer-events: none;
  animation: popLetter 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 60;
}

@keyframes popLetter {
  from { opacity: 0; transform: translateY(-50%) scale(0.7); }
  to { opacity: 1; transform: translateY(-50%) scale(1); }
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
