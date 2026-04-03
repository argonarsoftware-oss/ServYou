# ServYou - Salon/Spa/Clinic Booking System

## Project Overview
Native Android app (Kotlin) with a PHP/MySQL backend hosted on a VPS.

## Architecture

### Android App (`app/`)
- **Language**: Kotlin
- **Min SDK**: 26 | **Target SDK**: 34
- **Build**: Gradle 8.5 with KSP
- **UI**: Material Design 3 with ViewBinding
- **Database**: Room (local cache) + Retrofit (API sync)
- **Package**: `com.servyou.app`

### Backend (`backend/`)
- **Stack**: PHP + MySQL (XAMPP-compatible)
- **API Base**: `https://servyou.argonar.co/api/`
- **Database**: `servyou_db` on MySQL
- **Auth**: Bearer token (`Authorization: Bearer <API_KEY>`)
- **Email**: PHP `mail()` for booking confirmations

### API Endpoints
```
GET    /api/bookings          — list all bookings
GET    /api/bookings/{id}     — get single booking
GET    /api/bookings/stats    — get booking counts
POST   /api/bookings          — create booking
PUT    /api/bookings/{id}     — update booking
PATCH  /api/bookings/{id}     — update status only
DELETE /api/bookings/{id}     — delete booking
```

## Infrastructure

### VPS
- **IP**: 139.177.187.26
- **OS**: Ubuntu 24.04
- **Web Server**: Apache2
- **Domain**: servyou.argonar.co (SSL via Let's Encrypt)
- **Web Root**: `/var/www/ServYou/backend`
- **Repo Root**: `/var/www/ServYou`

### CI/CD
- **GitHub Actions**: Builds debug + signed release APK on every push to `main`
- **Signing**: Keystore stored as GitHub Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`)
- **APK naming**: `ServYou-v{build_number}-release.apk`

### Auto-Deploy (Webhook)
- **Trigger**: Push to `main` with `[deploy]` in commit message
- **Webhook URL**: `https://servyou.argonar.co/webhook-deploy.php`
- **Secret**: Stored in `webhook-deploy.php` and GitHub webhook settings
- **Strategy**: `git fetch origin main` + `git reset --hard origin/main`
- Normal pushes without `[deploy]` are ignored

## Key Files
- `backend/config/config.php` — API key, CORS, email settings
- `backend/config/database.php` — MySQL connection
- `backend/webhook-deploy.php` — GitHub webhook auto-deploy handler
- `app/src/main/java/com/servyou/app/data/api/RetrofitClient.kt` — API base URL and auth key
- `.github/workflows/android-build.yml` — CI/CD build + signing
- `.github/workflows/generate-keystore.yml` — One-time keystore generation

## Commands

### Deploy to VPS
Commit with `[deploy]` in the message:
```
git commit -m "your change description [deploy]"
git push origin main
```

### Download latest APK
```
gh run download <run_id> --repo argonarsoftware-oss/ServYou --name servyou-release --dir .
```

### Test API
```
curl -H "Authorization: Bearer changeme-servyou-2026" https://servyou.argonar.co/api/bookings/stats
```
