<script setup lang="ts">
import { ref } from 'vue'
import { GitHubUpdater } from '../services/updater'
import { ZenAudio } from '../services/audio'
import type { UpdateInfo } from '../types'

const props = defineProps<{
  updateInfo: UpdateInfo | null
}>()

const emit = defineEmits<{
  (e: 'dismiss'): void
}>()

const isInstalling = ref(false)

async function onInstall() {
  if (!props.updateInfo?.downloadUrl) return
  ZenAudio.tap()
  isInstalling.value = true
  await GitHubUpdater.installApk(props.updateInfo.downloadUrl)
  isInstalling.value = false
}

function onDismiss() {
  ZenAudio.back()
  emit('dismiss')
}
</script>

<template>
  <div v-if="updateInfo && updateInfo.hasUpdate" class="update-banner">
    <div class="update-header">
      <div class="update-title">
        <span class="update-badge">UPDATE</span>
        <span>ДОСТУПНА ВЕРСИЯ v{{ updateInfo.latestVersion }}</span>
      </div>
      <button class="close-btn" @click="onDismiss">✕</button>
    </div>

    <div class="update-notes">
      {{ updateInfo.releaseNotes }}
    </div>

    <div class="update-actions">
      <button class="install-btn bounce-tap" :disabled="isInstalling" @click="onInstall">
        {{ isInstalling ? 'ЗАГРУЗКА APK...' : 'СКАЧАТЬ И ОБНОВИТЬ APK' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.update-banner {
  background: #141418;
  border: 1px solid var(--accent);
  border-radius: 16px;
  padding: 12px 14px;
  margin-top: 8px;
  animation: slideIn 0.3s var(--spring-physics);
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.update-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.update-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: bold;
  letter-spacing: 1px;
  color: var(--text-main);
}

.update-badge {
  background: var(--accent);
  color: #fff;
  font-size: 9px;
  padding: 2px 5px;
  border-radius: 6px;
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 12px;
}

.update-notes {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 6px;
  max-height: 48px;
  overflow-y: auto;
  line-height: 1.4;
}

.update-actions {
  margin-top: 10px;
}

.install-btn {
  width: 100%;
  background: var(--text-main);
  color: #000;
  border: none;
  border-radius: 10px;
  padding: 8px 12px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: bold;
  letter-spacing: 1px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.install-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
