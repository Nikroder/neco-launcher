package com.zenlauncher.minimalist.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class UpdateResult(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?,
    val htmlUrl: String,
    val errorMessage: String? = null
)

object GitHubUpdateChecker {

    const val DEFAULT_REPO = "Nikroder/neco-launcher"

    fun getCurrentVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.4"
        } catch (e: Throwable) {
            "1.0.4"
        }
    }

    suspend fun checkForUpdates(context: Context, repo: String = DEFAULT_REPO): UpdateResult = withContext(Dispatchers.IO) {
        val currentVersion = getCurrentVersion(context)
        val cleanCurrent = currentVersion.removePrefix("v").trim()

        try {
            val url = URL("https://api.github.com/repos/$repo/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "NecoLauncher-Android")
            }

            val responseCode = conn.responseCode
            if (responseCode == 404) {
                return@withContext UpdateResult(
                    hasUpdate = false,
                    currentVersion = currentVersion,
                    latestVersion = currentVersion,
                    releaseTitle = "Релизы не найдены",
                    releaseNotes = "",
                    apkDownloadUrl = null,
                    htmlUrl = "https://github.com/$repo/releases",
                    errorMessage = "В репозитории $repo пока нет опубликованных релизов."
                )
            }

            if (responseCode !in 200..299) {
                return@withContext UpdateResult(
                    hasUpdate = false,
                    currentVersion = currentVersion,
                    latestVersion = currentVersion,
                    releaseTitle = "Ошибка сети",
                    releaseNotes = "",
                    apkDownloadUrl = null,
                    htmlUrl = "https://github.com/$repo/releases",
                    errorMessage = "Ошибка GitHub API (код $responseCode)"
                )
            }

            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val jsonText = reader.use { it.readText() }
            val root = JSONObject(jsonText)

            val rawTag = root.optString("tag_name", "0.0.0")
            val cleanTag = rawTag.removePrefix("v").trim()
            val name = root.optString("name", "Релиз $rawTag")
            val body = root.optString("body", "")
            val htmlUrl = root.optString("html_url", "https://github.com/$repo/releases")

            var apkUrl: String? = null
            val assets = root.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            val isNewer = isVersionNewer(cleanTag, cleanCurrent)

            UpdateResult(
                hasUpdate = isNewer,
                currentVersion = currentVersion,
                latestVersion = cleanTag,
                releaseTitle = name,
                releaseNotes = body,
                apkDownloadUrl = apkUrl,
                htmlUrl = htmlUrl
            )
        } catch (e: Throwable) {
            UpdateResult(
                hasUpdate = false,
                currentVersion = currentVersion,
                latestVersion = currentVersion,
                releaseTitle = "Ошибка",
                releaseNotes = "",
                apkDownloadUrl = null,
                htmlUrl = "https://github.com/$repo/releases",
                errorMessage = e.localizedMessage ?: "Не удалось подключиться к GitHub"
            )
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val lParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(lParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val l = lParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            android.util.Log.w("GitHubUpdateChecker", "Failed to open URL", e)
        }
    }
}
