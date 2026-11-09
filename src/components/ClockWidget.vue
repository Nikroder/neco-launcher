<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ZenAudio } from '../services/audio'

const timeStr = ref('20:49')
const dateStr = ref('')

const daysRu = ['ВОСКРЕСЕНЬЕ', 'ПОНЕДЕЛЬНИК', 'ВТОРНИК', 'СРЕДА', 'ЧЕТВЕРГ', 'ПЯТНИЦА', 'СУББОТА']
const monthsRu = ['ЯНВАРЯ', 'ФЕВРАЛЯ', 'МАРТА', 'АПРЕЛЯ', 'МАЯ', 'ИЮНЯ', 'ИЮЛЯ', 'АВГУСТА', 'СЕНТЯБРЯ', 'ОКТЯБРЯ', 'НОЯБРЯ', 'ДЕКАБРЯ']

function updateClock() {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  timeStr.value = `${h}:${m}`

  const dayName = daysRu[now.getDay()]
  const dayNum = now.getDate()
  const monthName = monthsRu[now.getMonth()]
  dateStr.value = `${dayName}, ${dayNum} ${monthName}`
}

let timer: number | null = null

onMounted(() => {
  updateClock()
  timer = window.setInterval(updateClock, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function onClick() {
  ZenAudio.tap()
}
</script>

<template>
  <div class="clock-widget bounce-tap" @click="onClick">
    <div class="os-badge">
      <div class="os-dot"></div>
      <div class="os-title">N.ECO // OS</div>
    </div>
    <div class="clock-digits">{{ timeStr }}</div>
    <div class="date-label">{{ dateStr }}</div>
  </div>
</template>

<style scoped>
.clock-widget {
  padding: 4px 0;
  cursor: pointer;
}

.os-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.os-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 8px var(--accent);
}

.os-title {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2.5px;
  color: var(--text-muted);
}

.clock-digits {
  font-family: var(--font-mono);
  font-size: 66px;
  font-weight: 300;
  letter-spacing: -3px;
  line-height: 1;
  color: var(--text-main);
}

.date-label {
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: var(--text-sub);
  margin-top: 5px;
}
</style>
