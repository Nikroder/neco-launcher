export interface ConnectedDevice {
  id: string
  name: string
  batteryLevel: number
  type: 'PHONE' | 'HEADPHONES' | 'WATCH' | 'STYLUS' | 'SPEAKER' | 'OTHER'
  isCharging?: boolean
}

export interface AppInfo {
  packageName: string
  label: string
  tag: string
  icon?: string
}

export interface LauncherMode {
  id: string
  name: string
  tag: string
  favorites: string[] // packageNames or labels
  isDeletable?: boolean
}

export interface MediaState {
  title: string
  artist: string
  isPlaying: boolean
  albumArt?: string
}

export interface UpdateInfo {
  hasUpdate: boolean
  currentVersion: string
  latestVersion: string
  releaseNotes: string
  downloadUrl: string
  publishedAt: string
  releaseName: string
}
