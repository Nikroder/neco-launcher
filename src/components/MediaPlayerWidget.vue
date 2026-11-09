<script setup lang="ts">
import { ref } from 'vue'
import { ZenAudio } from '../services/audio'

const tracks = [
  { title: 'Ambient Serenity', artist: 'Solar Fields • Spotify' },
  { title: 'Weightless', artist: 'Marconi Union • YouTube Music' },
  { title: 'Deep Focus Echoes', artist: 'Brian Eno • Я.Музыка' },
]

const currentIdx = ref(0)
const isPlaying = ref(true)

const currentTrack = ref(tracks[currentIdx.value])

function togglePlay() {
  ZenAudio.tap()
  isPlaying.value = !isPlaying.value
}

function next() {
  ZenAudio.tick()
  currentIdx.value = (currentIdx.value + 1) % tracks.length
  currentTrack.value = tracks[currentIdx.value]
  isPlaying.value = true
}

function prev() {
  ZenAudio.tick()
  currentIdx.value = (currentIdx.value - 1 + tracks.length) % tracks.length
  currentTrack.value = tracks[currentIdx.value]
  isPlaying.value = true
}

function openMusicApp() {
  ZenAudio.tap()
}
</script>

<template>
  <div class="media-card" :class="{ playing: isPlaying }">
    <div class="media-left" @click="openMusicApp">
      <div class="media-disc">
        <div class="media-pulse-dot"></div>
      </div>
      <div class="media-info">
        <div class="media-title">{{ currentTrack.title }}</div>
        <div class="media-artist">{{ currentTrack.artist }}</div>
      </div>
    </div>

    <div class="media-controls">
      <button class="media-btn bounce-tap" @click.stop="prev">⏮</button>
      <button class="media-btn play-pause bounce-tap" @click.stop="togglePlay">
        {{ isPlaying ? '⏸' : '▶' }}
      </button>
      <button class="media-btn bounce-tap" @click.stop="next">⏭</button>
    </div>
  </div>
</template>

<style scoped>
.media-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 9px 14px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  transition: all 0.25s var(--spring-physics);
}

.media-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  cursor: pointer;
  overflow: hidden;
}

.media-disc {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #0e0e12;
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
}

.media-pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  transition: all 0.3s;
}

.playing .media-pulse-dot {
  box-shadow: 0 0 10px var(--accent);
  animation: pulse 1.4s infinite ease-in-out;
}

@keyframes pulse {
  0%, 100% { transform: scale(0.9); opacity: 0.7; }
  50% { transform: scale(1.3); opacity: 1; }
}

.media-info {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
}

.media-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-main);
  text-overflow: ellipsis;
  overflow: hidden;
}

.media-artist {
  font-size: 11px;
  color: var(--text-sub);
  text-overflow: ellipsis;
  overflow: hidden;
  margin-top: 2px;
}

.media-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.media-btn {
  background: transparent;
  border: none;
  color: var(--text-sub);
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 13px;
}

.media-btn.play-pause {
  background: #0e0e12;
  color: var(--text-main);
  width: 32px;
  height: 32px;
}
</style>
