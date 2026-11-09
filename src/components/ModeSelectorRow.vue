<script setup lang="ts">
import { ModesService } from '../services/modes'
import { ZenAudio } from '../services/audio'

const emit = defineEmits<{
  (e: 'add-mode'): void
}>()

function selectMode(id: string) {
  ZenAudio.tap()
  ModesService.selectMode(id)
}

function onAddClick() {
  ZenAudio.tap()
  emit('add-mode')
}
</script>

<template>
  <div class="mode-row">
    <div
      v-for="mode in ModesService.state.allModes"
      :key="mode.id"
      class="mode-pill bounce-tap"
      :class="{ active: mode.id === ModesService.state.activeModeId }"
      @click="selectMode(mode.id)"
    >
      {{ mode.name.toUpperCase() }}
    </div>

    <!-- Add Mode Button -->
    <div class="add-mode-btn bounce-tap" @click="onAddClick" title="Создать режим">
      +
    </div>
  </div>
</template>

<style scoped>
.mode-row {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
  align-items: center;
}

.mode-row::-webkit-scrollbar {
  display: none;
}

.mode-pill {
  background: var(--surface);
  border: 1px solid var(--border);
  padding: 6px 12px;
  border-radius: 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.8px;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  transition: all 0.22s var(--spring-physics);
}

.mode-pill.active {
  background: var(--card);
  color: var(--text-main);
  font-weight: 600;
  border-color: #383840;
}

.mode-pill.active::before {
  content: '';
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent);
}

.add-mode-btn {
  background: var(--surface);
  border: 1px solid var(--border);
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}
</style>
