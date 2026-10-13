# FinTrack

Aplikasi pencatat keuangan: transaksi (pemasukan, pengeluaran, tagihan), analitik, dan anggaran.

## Struktur
- `app/`, `build.gradle`, `settings.gradle`, ... : aplikasi Android (Java, Room, ViewModel, LiveData, Material 3)
- `web/` : yang di-hosting di Vercel. Isinya hanya halaman unduh APK.
- `arsip/` : cadangan versi web lama (tidak ikut ter-deploy).

## Build APK
1. Buka folder ini di Android Studio, tunggu Gradle sync.
2. Build > Build Bundle(s) / APK(s) > Build APK(s)
3. Salin `app/build/outputs/apk/debug/app-debug.apk` ke `web/FinTrack.apk` (timpa file lama).

## Hosting (Vercel)
Project Settings > General > **Root Directory = `web`**, Framework Preset = Other.
Build Command dan Output Directory dikosongkan. Lalu deploy ulang.

Membuka link Vercel langsung menampilkan halaman unduh dan memulai unduhan `FinTrack.apk`.
`/unduh` dialihkan ke `/`.
