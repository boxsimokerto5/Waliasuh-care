# Informasi Keystore & Konfigurasi Signature Build

File ini berisi informasi penandatanganan (signature key) permanen untuk aplikasi **Waliasuh Care**.

---

### 1. Informasi Signature Key (Permanen & Konsisten)
Kunci signature telah digenerate secara permanen sehingga **tidak akan pernah berubah** pada proses build berikutnya. Hal ini sangat penting agar update aplikasi di masa mendatang dapat diinstal di perangkat pengguna atau diunggah ke Google Play Store tanpa konflik tanda tangan (*signature mismatch*).

- **Keystore File:** `my-upload-key.jks`
- **Cadangan Base64:** `release.keystore.base64` (tersimpan permanen di repository)
- **Key Alias:** `upload`
- **Store Password:** `waliasuhcare2026`
- **Key Password:** `waliasuhcare2026`
- **Masa Berlaku:** 10.000 Hari (~27 Tahun)
- **Kepemilikan (DName):** `CN=Waliasuh Care, OU=SRT 1 Kediri, O=Sekolah Rakyat, L=Kediri, ST=Jawa Timur, C=ID`

---

### 2. File Alur Otomatisasi (CI/CD GitHub Actions)
File workflow terletak di:
`/.github/workflows/build.yml`

#### Cara Kerja:
1. **Trigger Otomatis:** Berjalan setiap kali ada `push` atau `pull_request` ke cabang `main`/`master`.
2. **Trigger Manual:** Dapat dijalankan kapan saja secara manual melalui tab **Actions** di GitHub ➔ Pilih workflow **Build Android (APK & AAB)** ➔ Klik **Run workflow**.
3. **Penyusunan Keystore:** Workflow secara otomatis membaca `release.keystore.base64` sehingga kunci signature yang sama persis selalu digunakan.
4. **Hasil Build:**
   - **APK Release (Signed):** Siap diinstal langsung di HP Android.
   - **AAB Release (Signed):** Siap diunggah ke Google Play Console.
5. **Unduh Output:** Hasil build dapat langsung diunduh dari tab **Artifacts** di halaman selesai eksekusi GitHub Actions.

---

### 3. Perintah Build Manual (Lokal / Terminal)
Jika Anda ingin membuild sendiri secara lokal melalui terminal:

```bash
# Build APK Release
STORE_PASSWORD=waliasuhcare2026 KEY_PASSWORD=waliasuhcare2026 gradle :app:assembleRelease

# Build AAB (App Bundle untuk Google Play)
STORE_PASSWORD=waliasuhcare2026 KEY_PASSWORD=waliasuhcare2026 gradle :app:bundleRelease
```
