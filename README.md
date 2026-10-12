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

## Catatan perubahan (perbaikan UI/UX dan input)
- Input nominal menerima format umum Indonesia seperti `1.000.000`, serta format desimal seperti `1.250,50`.
- Pengeluaran yang dipakai untuk progres anggaran dibatasi ke bulan berjalan.
- Status transaksi sekarang dapat diedit dari dialog edit transaksi.
- `web/FinTrack.apk` yang ada di arsip belum dibangun ulang dari perubahan ini. Setelah build APK baru, timpa file tersebut mengikuti langkah di atas sebelum deploy.

## Tema visual

Versi ini menggunakan tema terang modern yang terinspirasi dari visual iOS: latar abu-abu sangat muda, permukaan putih, aksen biru, kartu bersudut membulat, dan ikon navigasi dengan status aktif berwarna biru. Ini tetap aplikasi Android native berbasis Java/XML dan komponen Material 3; bukan aplikasi iOS.

File utama untuk penyesuaian tampilan:
- `app/src/main/res/values/colors.xml` — palet warna.
- `app/src/main/res/values/themes.xml` — tema dan gaya komponen.
- `app/src/main/res/layout/` — tata letak setiap halaman dan kartu.
- `app/src/main/res/drawable/` — latar kartu, avatar, dan dekorasi.
