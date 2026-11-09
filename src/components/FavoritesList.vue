<script setup lang="ts">
import { computed } from 'vue'
import { ModesService } from '../services/modes'
import { ZenAudio } from '../services/audio'

const emit = defineEmits<{
  (e: 'open-drawer'): void
  (e: 'open-settings'): void
  (e: 'launch-app', name: string): void
}>()

const activeMode = computed(() => ModesService.getActiveMode())
const favorites = computed(() => activeMode.value.favorites.slice(0, 6))

function onAppClick(appName: string) {
  ZenAudio.open()
  emit('launch-app', appName)
}

function onOpenDrawer() {
  ZenAudio.drawer()
  emit('open-drawer')
}

function onOpenSettings() {
  ZenAudio.tap()
  emit('open-settings')
}
</script>

<template>
  <div class="favorites-container">
    <!-- Header -->
    <div class="favorites-header">
      <span>ИЗБРАННЫЕ // {{ activeMode.name.toUpperCase() }}</span>
      <span>{{ String(favorites.length).padStart(2, '0') }} / 06</span>
    </div>

    <!-- Animated List -->
    <TransitionGroup name="fav-fade" tag="div" class="favorites-list">
      <div
        v-for="(appName, idx) in favorites"
        :key="appName"
        class="fav-item bounce-tap"
        @click="onAppClick(appName)"
      >
        <div class="fav-left">
          <span class="fav-index">{{ String(idx + 1).padStart(2, '0') }}</span>
          <span class="fav-name">{{ appName }}</span>
        </div>
        <div class="fav-dot"></div>
      </div>
    </TransitionGroup>

    <!-- Empty State -->
    <div
      v-if="favorites.length === 0"
      class="empty-fav-card bounce-tap"
      @click="onOpenDrawer"
    >
      + Закрепите приложения для режима «{{ activeMode.name }}»
    </div>

    <!-- Navigation Footer -->
    <div class="launcher-footer">
      <div class="btn-all-apps bounce-tap" @click="onOpenDrawer">
        <span class="red-dots">•••</span>
        <span>ВСЕ ПРИЛОЖЕНИЯ</span>
      </div>
      <div class="btn-settings bounce-tap" @click="onOpenSettings">
        N.ECO // PREFS
      </div>
    </div>
  </div>
</template>

<style scoped>
.favorites-container {
  display: flex;
  flex-direction: column;
}

.favorites-header {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1.5px;
  color: var(--text-muted);
  margin-bottom: 7px;
  display: flex;
  justify-content: space-between;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.fav-item {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 11px 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.fav-left {
  display: flex;
  align-items: center;
  gap: 13px;
}

.fav-index {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 1px;
}

.fav-name {
  font-size: 15px;
  color: var(--text-main);
  font-weight: 400;
  letter-spacing: 0.3px;
}

.fav-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--border);
}

.empty-fav-card {
  background: var(--card);
  border: 1px dashed var(--border);
  border-radius: 14px;
  padding: 18px;
  text-align: center;
  font-size: 13px;
  color: var(--text-sub);
  cursor: pointer;
}

.launcher-footer {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-all-apps {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 7px 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1.2px;
  color: var(--text-sub);
}

.red-dots {
  color: var(--accent);
  font-weight: bold;
  letter-spacing: 2px;
}

.btn-settings {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 8px 10px;
  border-radius: 12px;
}

/* Staggered Transitions */
.fav-fade-enter-active,
.fav-fade-leave-active {
  transition: all 0.28s var(--spring-physics);
}

.fav-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fav-fade-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
