# DBZBANTEN LSPosed Game AdBlock

Konversi dari modul Magisk `DBZBANTEN Game AdBlock v3.0` menjadi modul LSPosed.

## Cara kerja

Modul ini memakai LSPosed/Xposed untuk hook `java.net.InetAddress` pada aplikasi yang dipilih dalam **LSPosed Manager**. Jika aplikasi melakukan resolusi hostname yang cocok dengan daftar `ad_domains.txt`, hostname tersebut diarahkan ke `0.0.0.0`.

**Penting:** versi LSPosed ini tidak menggunakan `iptables`, sehingga tidak membutuhkan Magisk module/service.sh. Pemilihan game dilakukan melalui scope LSPosed.

## Instalasi

1. Buka project ini di Android Studio.
2. Build `app` dengan `assembleRelease`.
3. Install APK hasil build.
4. Aktifkan modul di LSPosed Manager.
5. Masuk ke **Modules → DBZBANTEN LSPosed Game AdBlock → Scope**.
6. Pilih game yang ingin diblokir iklannya.
7. Force-stop game lalu buka kembali.

## Daftar domain

Edit:
`app/src/main/assets/ad_domains.txt`

Satu domain per baris. Subdomain juga dicocokkan. Contoh `doubleclick.net` akan mencakup `foo.doubleclick.net`.

## Batasan

- Hanya aplikasi yang dipilih pada scope LSPosed yang terkena hook.
- Aplikasi yang memakai DNS resolver sendiri, DNS-over-HTTPS, QUIC, native resolver, hard-coded IP, atau library networking tertentu dapat melewati hook ini.
- Mengarahkan DNS ke `0.0.0.0` adalah best-effort blocking; beberapa aplikasi mungkin menangani kegagalan koneksi secara berbeda.
- Browser tidak otomatis dikecualikan karena LSPosed menggunakan scope aplikasi. Jangan centang browser jika tidak ingin memengaruhinya.

## GitHub Actions

Workflow tersedia di `.github/workflows/build.yml` dan akan membangun APK release pada push/tag.
