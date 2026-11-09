<script setup lang="ts">
import { ref } from 'vue'
import { GitHubUpdater } from '../services/updater'
import { ModesService } from '../services/modes'
import { ZenAudio } from '../services/audio'
import type { UpdateInfo } from '../types'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update-found', info: UpdateInfo): void
}>()

const repoInput = ref(GitHubUpdater.getRepo())
const soundEnabled = ref(ZenAudio.soundEnabled)
const hapticsEnabled = ref(ZenAudio.hapticsEnabled)
const checkStatus = ref('')
const isChecking = ref(false)

function onSaveRepo() {
  GitHubUpdater.setRepo(repoInput.value)
  ZenAudio.tap()
  checkStatus.value = 'Репозиторий сохранен!'
}

async function checkUpdateNow() {
  ZenAudio.tap()
  isChecking.value = true
  checkStatus.value = 'Связь с GitHub Releases...'
  const info = await GitHubUpdater.checkForUpdates()
  isChecking.value = false

  if (info.hasUpdate) {
    checkStatus.value = `Найдено обновление v${info.latestVersion}!`
    emit('update-found', info)
  } else {
    checkStatus.value = `У вас последняя версия (v${info.currentVersion})`
  }
}

function toggleSound() {
  soundEnabled.value = !soundEnabled.value
  ZenAudio.soundEnabled = soundEnabled.value
  if (soundEnabled.value) ZenAudio.tap()
}

function toggleHaptics() {
  hapticsEnabled.value = !hapticsEnabled.value
  ZenAudio.hapticsEnabled = hapticsEnabled.value
  ZenAudio.tap()
}

function close() {
  ZenAudio.back()
  emit('close')
}
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-sheet">
      <div class="sheet-handle" @click="close"></div>

      <div class="modal-title">НАСТРОЙКИ // ZEN LAUNCHER</div>

      <!-- Auto Update via GitHub Releases Section -->
      <div class="section-title">АВТООБНОВЛЕНИЕ ЧЕРЕЗ GITHUB RELEASES</div>
      <div class="card-box">
        <label class="field-label">GitHub репозиторий (owner/repo):</label>
        <div class="input-row">
          <input v-model="repoInput" type="text" placeholder="username/zen-launcher" />
          <button class="action-btn bounce-tap" @click="onSaveRepo">OK</button>
        </div>

        <button class="check-btn bounce-tap" :disabled="isChecking" @click="checkUpdateNow">
          {{ isChecking ? 'ПРОВЕРКА...' : 'ПРОВЕРИТЬ ОБНОВЛЕНИЯ СЕЙЧАС' }}
        </button>

        <div v-if="checkStatus" class="status-msg">
          {{ checkStatus }}
        </div>
      </div>

      <!-- Audio & Haptics Section -->
      <div class="section-title">ЗВУКИ И ТАКТИЛЬНОСТЬ (ZEN)</div>
      <div class="card-box">
        <div class="toggle-row" @click="toggleSound">
          <span>Звуки системы (Zen)</span>
          <span class="toggle-val">{{ soundEnabled ? 'ВКЛ' : 'ВЫКЛ' }}</span>
        </div>
        <div class="divider"></div>
        <div class="toggle-row" @click="toggleHaptics">
          <span>Тактильная виброотдача</span>
          <span class="toggle-val">{{ hapticsEnabled ? 'ВКЛ' : 'ВЫКЛ' }}</span>
        </div>
      </div>

      <!-- Samsung Modes Management -->
      <div class="section-title">РЕЖИМЫ SAMSUNG (MODES & ROUTINES)</div>
      <div class="card-box">
        <div v-for="mode in ModesService.state.allModes" :key="mode.id" class="mode-item">
          <span>{{ mode.name }} ({{ mode.tag }})</span>
          <button
            v-if="mode.isDeletable"
            class="del-btn bounce-tap"
            @click="ModesService.deleteMode(mode.id)"
          >
            Удалить
          </button>
        </div>
      </div>

      <button class="close-modal-btn bounce-tap" @click="close">ЗАКРЫТЬ</button>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(14px);
  z-index: 60;
  display: flex;
  align-items: flex-end;
}

.modal-sheet {
  width: 100%;
  max-height: 85%;
  background: #0d0d10;
  border-top: 1px solid var(--border);
  border-radius: 24px 24px 0 0;
  padding: 16px 20px 24px 20px;
  overflow-y: auto;
  animation: slideUp 0.35s var(--spring-physics);
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.sheet-handle {
  width: 36px;
  height: 4px;
  background: #2d2d32;
  border-radius: 2px;
  margin: 0 auto 16px auto;
  cursor: pointer;
}

.modal-title {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: bold;
  letter-spacing: 2px;
  color: var(--text-sub);
  margin-bottom: 16px;
}

.section-title {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 1.5px;
  color: var(--text-muted);
  margin: 14px 0 6px 0;
}

.card-box {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px 14px;
}

.field-label {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  display: block;
  margin-bottom: 6px;
}

.input-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.input-row input {
  flex: 1;
  background: #0a0a0d;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 8px 10px;
  color: var(--text-main);
  font-size: 13px;
  font-family: var(--font-mono);
  outline: none;
}

.action-btn {
  background: #222228;
  color: var(--text-main);
  border: 1px solid var(--border);
  padding: 0 14px;
  border-radius: 8px;
  cursor: pointer;
  font-family: var(--font-mono);
  font-size: 11px;
}

.check-btn {
  width: 100%;
  background: #202026;
  color: var(--text-main);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 9px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: bold;
  letter-spacing: 1px;
  cursor: pointer;
}

.status-msg {
  font-size: 12px;
  font-family: var(--font-mono);
  color: var(--accent);
  margin-top: 8px;
  text-align: center;
}

.toggle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  cursor: pointer;
  font-size: 13px;
}

.toggle-val {
  font-family: var(--font-mono);
  font-weight: bold;
  font-size: 12px;
  color: var(--text-sub);
}

.divider {
  height: 1px;
  background: var(--border);
  margin: 6px 0;
}

.mode-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  font-size: 13px;
}

.del-btn {
  background: transparent;
  border: 1px solid var(--accent);
  color: var(--accent);
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 11px;
  cursor: pointer;
}

.close-modal-btn {
  width: 100%;
  margin-top: 20px;
  background: var(--card);
  border: 1px solid var(--border);
  color: var(--text-main);
  padding: 12px;
  border-radius: 12px;
  font-family: var(--font-mono);
  font-weight: bold;
  cursor: pointer;
}
</style>
