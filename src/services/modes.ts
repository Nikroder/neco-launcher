import { reactive, watch } from 'vue'
import type { LauncherMode } from '../types'

const DEFAULT_MODES: LauncherMode[] = [
  {
    id: 'default',
    name: 'Обычный',
    tag: 'STD',
    favorites: ['Телефон', 'Telegram', 'Браузер', 'Камера', 'Заметки', 'Музыка'],
    isDeletable: false,
  },
  {
    id: 'work',
    name: 'Работа',
    tag: 'WRK',
    favorites: ['Slack', 'Gmail', 'Календарь', 'Zoom', 'Заметки', 'Файлы'],
    isDeletable: false,
  },
  {
    id: 'relax',
    name: 'Отдых',
    tag: 'RLX',
    favorites: ['YouTube', 'Spotify', 'Книги', 'Кинопоиск', 'Telegram', 'Игры'],
    isDeletable: false,
  },
  {
    id: 'sleep',
    name: 'Сон',
    tag: 'SLP',
    favorites: ['Часы', 'Будильник', 'Звуки сна', 'Заметки'],
    isDeletable: false,
  },
  {
    id: 'sport',
    name: 'Спорт',
    tag: 'SPT',
    favorites: ['Strava', 'Тренировки', 'Музыка', 'Шагомер', 'Таймер'],
    isDeletable: false,
  },
]

class ModesManager {
  public state = reactive({
    allModes: DEFAULT_MODES,
    activeModeId: 'default',
  })

  constructor() {
    this.load()
    watch(
      () => this.state,
      () => this.save(),
      { deep: true }
    )
  }

  private load() {
    const savedModes = localStorage.getItem('zen_launcher_modes')
    if (savedModes) {
      try {
        this.state.allModes = JSON.parse(savedModes)
      } catch (e) {
        this.state.allModes = DEFAULT_MODES
      }
    }
    const savedActive = localStorage.getItem('zen_active_mode_id')
    if (savedActive && this.state.allModes.some(m => m.id === savedActive)) {
      this.state.activeModeId = savedActive
    }
  }

  private save() {
    localStorage.setItem('zen_launcher_modes', JSON.stringify(this.state.allModes))
    localStorage.setItem('zen_active_mode_id', this.state.activeModeId)
  }

  getActiveMode(): LauncherMode {
    return this.state.allModes.find(m => m.id === this.state.activeModeId) || this.state.allModes[0]
  }

  selectMode(id: string) {
    if (this.state.allModes.some(m => m.id === id)) {
      this.state.activeModeId = id
    }
  }

  addCustomMode(name: string) {
    const id = 'custom_' + Date.now()
    const tag = name.slice(0, 3).toUpperCase()
    this.state.allModes.push({
      id,
      name,
      tag,
      favorites: ['Телефон', 'Telegram', 'Браузер'],
      isDeletable: true,
    })
    this.selectMode(id)
  }

  deleteMode(id: string) {
    const idx = this.state.allModes.findIndex(m => m.id === id)
    if (idx !== -1 && this.state.allModes[idx].isDeletable) {
      this.state.allModes.splice(idx, 1)
      if (this.state.activeModeId === id) {
        this.selectMode('default')
      }
    }
  }

  toggleFavorite(appName: string) {
    const active = this.getActiveMode()
    const idx = active.favorites.indexOf(appName)
    if (idx !== -1) {
      active.favorites.splice(idx, 1)
    } else {
      if (active.favorites.length < 6) {
        active.favorites.push(appName)
      }
    }
  }
}

export const ModesService = new ModesManager()
