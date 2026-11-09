<script setup lang="ts">
import { ref } from 'vue'
import { ZenAudio } from '../services/audio'
import type { ConnectedDevice } from '../types'

const isExpanded = ref(false)

const phone = ref<ConnectedDevice>({
  id: 'phone',
  name: 'Galaxy S24 Ultra',
  batteryLevel: 87,
  type: 'PHONE',
  isCharging: false,
})

const accessories = ref<ConnectedDevice[]>([
  { id: 'buds', name: 'Galaxy Buds2 Pro', batteryLevel: 92, type: 'HEADPHONES' },
  { id: 'watch', name: 'Galaxy Watch6', batteryLevel: 65, type: 'WATCH' },
  { id: 'spen', name: 'S Pen', batteryLevel: 100, type: 'STYLUS' },
])

function toggleExpand() {
  ZenAudio.tap()
  isExpanded.value = !isExpanded.value
}

function getIcon(type: string): string {
  switch (type) {
    case 'HEADPHONES': return '🎧'
    case 'WATCH': return '⌚'
    case 'STYLUS': return '✏️'
    default: return '📱'
  }
}
</script>

<template>
  <div class="battery-card" :class="{ expanded: isExpanded }" @click="toggleExpand">
    <!-- Collapsed Header -->
    <div class="battery-header">
      <div class="battery-left">
        <div class="os-dot"></div>
        <span>BATTERY</span>
        <span class="battery-badge">+{{ accessories.length }} DEV</span>
      </div>
      <div class="battery-right">
        <div class="battery-dots">
          <div
            v-for="i in 5"
            :key="i"
            class="b-dot"
            :class="{ active: i <= Math.ceil(phone.batteryLevel / 20) }"
          ></div>
        </div>
        <span class="battery-percent">{{ phone.batteryLevel }}%</span>
        <span class="caret-icon">▾</span>
      </div>
    </div>

    <!-- Smoothly Unfolded Tray -->
    <div class="connected-tray">
      <div class="tray-title">ПОДКЛЮЧЕННЫЕ УСТРОЙСТВА</div>

      <!-- Phone Row -->
      <div class="device-row">
        <div class="device-info">
          <span>📱</span>
          <span>{{ phone.name }}</span>
        </div>
        <div class="device-stat">
          <div class="battery-dots">
            <div v-for="i in 5" :key="i" class="b-dot" :class="{ active: i <= Math.ceil(phone.batteryLevel / 20) }"></div>
          </div>
          <span class="battery-percent">{{ phone.batteryLevel }}%</span>
        </div>
      </div>

      <!-- Accessories Rows -->
      <div v-for="item in accessories" :key="item.id" class="device-row">
        <div class="device-info">
          <span>{{ getIcon(item.type) }}</span>
          <span>{{ item.name }}</span>
        </div>
        <div class="device-stat">
          <div class="battery-dots">
            <div v-for="i in 5" :key="i" class="b-dot" :class="{ active: i <= Math.ceil(item.batteryLevel / 20) }"></div>
          </div>
          <span class="battery-percent">{{ item.batteryLevel }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.battery-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 10px 14px;
  margin-top: 8px;
  cursor: pointer;
  transition: all 0.35s var(--spring-physics);
  overflow: hidden;
}

.battery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.battery-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--text-sub);
}

.os-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.battery-badge {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--accent);
  font-weight: bold;
  background: rgba(215, 25, 32, 0.12);
  padding: 2px 6px;
  border-radius: 8px;
}

.battery-right {
  display: flex;
  align-items: center;
}

.battery-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.b-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #2c2c30;
  transition: background 0.3s;
}

.b-dot.active {
  background: var(--text-main);
}

.battery-percent {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-main);
  margin-left: 8px;
}

.caret-icon {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
  margin-left: 6px;
  transition: transform 0.35s var(--spring-physics);
  display: inline-block;
}

.battery-card.expanded .caret-icon {
  transform: rotate(180deg);
}

.connected-tray {
  max-height: 0;
  opacity: 0;
  transform: translateY(-8px);
  transition: max-height 0.38s var(--spring-physics), opacity 0.28s ease, transform 0.35s var(--spring-physics);
  pointer-events: none;
}

.battery-card.expanded .connected-tray {
  max-height: 220px;
  opacity: 1;
  transform: translateY(0);
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border);
  pointer-events: auto;
}

.tray-title {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 1.5px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.device-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 2px;
}

.device-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-main);
  font-size: 13px;
}

.device-stat {
  display: flex;
  align-items: center;
}
</style>
