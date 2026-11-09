import type { UpdateInfo } from '../types'

const DEFAULT_REPO = 'Nikroder/neco-launcher'
const CURRENT_VERSION = '1.0.4'

export class GitHubUpdaterService {
  private repoKey = 'zen_github_repo'

  getRepo(): string {
    return localStorage.getItem(this.repoKey) || DEFAULT_REPO
  }

  setRepo(repo: string) {
    localStorage.setItem(this.repoKey, repo.trim())
  }

  getCurrentVersion(): string {
    return CURRENT_VERSION
  }

  async checkForUpdates(): Promise<UpdateInfo> {
    const repo = this.getRepo()
    try {
      const response = await fetch(`https://api.github.com/repos/${repo}/releases/latest`, {
        headers: {
          Accept: 'application/vnd.github.v3+json',
        },
      })

      if (!response.ok) {
        if (response.status === 404) {
          return {
            hasUpdate: false,
            currentVersion: CURRENT_VERSION,
            latestVersion: CURRENT_VERSION,
            releaseNotes: 'Релизы в репозитории пока не найдены. Создайте первый Release с прикрепленным APK.',
            downloadUrl: '',
            publishedAt: '',
            releaseName: 'Нет релизов',
          }
        }
        throw new Error(`GitHub API error: ${response.status}`)
      }

      const data = await response.json()
      const rawTag = (data.tag_name || '0.0.0').replace(/^v/, '')
      const latestVer = rawTag
      const hasUpdate = this.isNewerVersion(latestVer, CURRENT_VERSION)

      // Find .apk asset
      let apkDownloadUrl = ''
      if (Array.isArray(data.assets)) {
        const apkAsset = data.assets.find((a: { name: string; browser_download_url: string }) =>
          a.name.endsWith('.apk')
        )
        if (apkAsset) {
          apkDownloadUrl = apkAsset.browser_download_url
        }
      }

      if (!apkDownloadUrl && data.html_url) {
        apkDownloadUrl = data.html_url
      }

      return {
        hasUpdate,
        currentVersion: CURRENT_VERSION,
        latestVersion: latestVer,
        releaseNotes: data.body || 'Новые улучшения и исправления ошибок.',
        downloadUrl: apkDownloadUrl,
        publishedAt: data.published_at || '',
        releaseName: data.name || `Релиз v${latestVer}`,
      }
    } catch (e: unknown) {
      console.warn('GitHub update check fallback:', e)
      return {
        hasUpdate: false,
        currentVersion: CURRENT_VERSION,
        latestVersion: CURRENT_VERSION,
        releaseNotes: 'Не удалось проверить обновления.',
        downloadUrl: '',
        publishedAt: '',
        releaseName: 'Ошибка сети',
      }
    }
  }

  private isNewerVersion(latest: string, current: string): boolean {
    const lParts = latest.split('.').map(n => parseInt(n, 10) || 0)
    const cParts = current.split('.').map(n => parseInt(n, 10) || 0)

    for (let i = 0; i < Math.max(lParts.length, cParts.length); i++) {
      const l = lParts[i] || 0
      const c = cParts[i] || 0
      if (l > c) return true
      if (l < c) return false
    }
    return false
  }

  async installApk(url: string): Promise<boolean> {
    if (!url) return false

    // Check if running inside Tauri mobile
    if (typeof window !== 'undefined' && '__TAURI_INTERNALS__' in window) {
      try {
        const { invoke } = await import('@tauri-apps/api/core')
        await invoke('download_and_install_apk', { apkUrl: url })
        return true
      } catch (err) {
        console.error('Tauri install failed, opening browser link:', err)
      }
    }

    // Web / Direct download fallback
    window.open(url, '_blank')
    return true
  }
}

export const GitHubUpdater = new GitHubUpdaterService()
