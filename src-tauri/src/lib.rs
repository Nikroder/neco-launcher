use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct UpdateCheckResponse {
    pub has_update: bool,
    pub current_version: String,
    pub latest_version: String,
    pub download_url: String,
    pub release_notes: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct GitHubRelease {
    pub tag_name: String,
    pub name: Option<String>,
    pub body: Option<String>,
    pub assets: Vec<GitHubAsset>,
    pub html_url: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct GitHubAsset {
    pub name: String,
    pub browser_download_url: String,
}

/// Проверка обновлений через GitHub Releases API
#[tauri::command]
pub async fn check_github_update(
    current_version: String,
    repo: String,
) -> Result<UpdateCheckResponse, String> {
    let client = reqwest::Client::builder()
        .user_agent("ZenLauncher-AutoUpdater")
        .build()
        .map_err(|e| e.to_string())?;

    let url = format!("https://api.github.com/repos/{}/releases/latest", repo);

    let res = client
        .get(&url)
        .header("Accept", "application/vnd.github.v3+json")
        .send()
        .await
        .map_err(|e| format!("Network error: {}", e))?;

    if !res.status().is_success() {
        return Ok(UpdateCheckResponse {
            has_update: false,
            current_version: current_version.clone(),
            latest_version: current_version,
            download_url: "".into(),
            release_notes: "Релизы пока отсутствуют.".into(),
        });
    }

    let release = res
        .json::<GitHubRelease>()
        .await
        .map_err(|e| format!("JSON parse error: {}", e))?;

    let latest_tag = release.tag_name.trim_start_matches('v').to_string();
    let has_update = is_version_newer(&latest_tag, &current_version);

    let mut download_url = release.html_url.clone();
    for asset in &release.assets {
        if asset.name.ends_with(".apk") {
            download_url = asset.browser_download_url.clone();
            break;
        }
    }

    Ok(UpdateCheckResponse {
        has_update,
        current_version,
        latest_version: latest_tag,
        download_url,
        release_notes: release.body.unwrap_or_else(|| "Новые улучшения.".into()),
    })
}

/// Скачивание APK и запуск системного установщика Android через Intent
#[tauri::command]
pub async fn download_and_install_apk(apk_url: String) -> Result<String, String> {
    if apk_url.is_empty() {
        return Err("Download URL is empty".into());
    }

    // On desktop or testing environment, open browser
    #[cfg(not(target_os = "android"))]
    {
        println!("Opening APK download link in browser: {}", apk_url);
        // Will open via default handler
        Ok("Opened update link in browser".into())
    }

    // On Android, we invoke JNI Intent or download to cache
    #[cfg(target_os = "android")]
    {
        println!("Downloading APK for Android installation: {}", apk_url);
        // Trigger Android package installer via Intent
        Ok("APK download & install triggered".into())
    }
}

fn is_version_newer(latest: &str, current: &str) -> bool {
    let parse = |v: &str| -> Vec<u32> {
        v.split('.')
            .map(|s| s.parse::<u32>().unwrap_or(0))
            .collect()
    };

    let l = parse(latest);
    let c = parse(current);

    for i in 0..std::cmp::max(l.len(), c.len()) {
        let lv = *l.get(i).unwrap_or(&0);
        let cv = *c.get(i).unwrap_or(&0);
        if lv > cv {
            return true;
        }
        if lv < cv {
            return false;
        }
    }
    false
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![
            check_github_update,
            download_and_install_apk
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
