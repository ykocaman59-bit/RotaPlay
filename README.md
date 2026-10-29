# RotaPlay 🧭

Android için **CarPlay tarzı araç arayüzü**. Tam ekran yatay panel: navigasyon, saat, hız (GPS), müzik kontrolü, uygulama listesi ve animasyonlu açılış.

> Not: Apple CarPlay, Apple'ın kapalı protokolüdür; Android'de bire bir çalışmaz. RotaPlay, benzer deneyimi sunan bağımsız bir Android uygulamasıdır.

## APK nasıl alınır (GitHub Actions)
1. Bu klasörü GitHub'da yeni bir repoya yükle (`main` dalı).
2. **Actions** sekmesi → *Build APK* çalışır (yaklaşık 3–5 dk).
3. Bitince çalışmanın altındaki **Artifacts → RotaPlay-APK** dosyasını indir, telefona kur.
4. Sürüm olarak yayınlamak için: `git tag v1.0 && git push origin v1.0` → APK, *Releases* sayfasına da eklenir.

## Yerelde derleme
Android Studio ile aç ve çalıştır, ya da: `gradle assembleDebug`

## Özellikler
- Animasyonlu logo + açılış (halka çizilir, ok yaylanarak belirir)
- Navigasyon: adres yaz → Google Maps / Waze vb. varsayılan harita uygulamasıyla rota
- Saat/tarih, GPS'ten canlı hız
- Müzik: önceki / oynat-duraklat / sonraki + ses (Spotify, YouTube Music vs. ile çalışır)
- Tüm uygulamalar ızgarası, Telefon ve Harita kısayolları
- Araç ana ekranı (Home) olarak seçilebilir
