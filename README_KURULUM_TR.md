# Ticaret İmparatorluğu — GitHub'a bağlı Android test uygulaması

Bu paket **Android Studio / Gradle kaynak projesidir, APK değildir**. GitHub Actions üzerinden ücretsiz test APK derlemesi yapmayı hedefler; derleme bu ortamda çalıştırılamamıştır ve doğrulanmamıştır.

## Nasıl çalışır?

- Oyun web adresi: https://mehmetmaden42.github.io/Ticaret-imparatorlu-u/
- Uygulama açılınca bu adresteki güncel HTML yüklenir.
- Oyun içindeki **↻ Güncelle** düğmesi WebView ağ önbelleğini temizleyip güncel sayfayı tekrar yükler. **Yeni APK indirip kurmaz**.
- Web sitesinin GitHub'a gönderilen yeni sürümleri Pages yayınlandıktan sonra uygulamada görüntülenir.
- Kaydedilmiş oyun verileri uygulama içindeki tarayıcı yerel depolamasındadır (`localStorage`). **Eski V53 APK kayıtlarına erişemez**.
- Oyun menüsündeki **Kaydı yedekle** düğmesi Android `AndroidGameSave.saveBase64` köprüsünü kullanır; Android 10+ Downloads/TicaretImparatorlugu klasörüne yazılır. **Kaydı geri yükle** JSON dosyası seçer.

## GitHub'a ekleme (telefon üzerinden de mümkün)

1. Önce V53 APK içindeki **Kaydı yedekle** ile JSON dosyanızı saklayın.
2. Bu ZIP içindeki `app/`, `.github/`, `build.gradle`, `settings.gradle`, `gradle.properties` dosya ve klasörlerini **Ticaret-imparatorlu-u** deposunun köküne, yollarını koruyarak ekleyin. ZIP dosyasının sadece kendisini yüklemek yeterli değildir.
3. GitHub > **Actions** > **Android Test APK** > **Run workflow** ile derleme başlatın. Tamamlandığında ilgili workflow'un **Artifacts** bölümünden APK indirilebilir.
4. Bu APK, farklı uygulama kimliğiyle (`com.maden.ticaretimparatorlugu.live`) kurulur; var olan V53 APK'yi değiştirmez. İlk açılışta JSON kaydı geri yükleyin.

## Kritik sınırlamalar

- GitHub'a otomatik yazma erişimi olmadığı için bu dosyalar depoya henüz gönderilmedi.
- Buradaki GitHub Actions test APK'sı varsayılan **debug imzasını** kullanır; her yeni GitHub Actions makinesinde debug imzası farklı olabilir. Bu yüzden APK'yı sık sık yeniden kurup güncellemek için uygun değildir. Mağaza dağıtımı veya sürdürülebilir APK güncellemeleri için kalıcı ve gizli release imza anahtarı ile ayrı bir imzalama süreci kurulmalıdır.
- V53'ün orijinal imza anahtarı mevcut olmadığından onun üzerine yeni APK'yı güncelleme olarak kurmak garanti edilemez.
- İnternet yokken GitHub Pages'teki içerik erişilemeyebilir; bu paket tam çevrimdışı sürüm değildir.
- GitHub Pages depo herkese açıktır. Bu paket kaynak dosyalarına özel anahtar veya API şifresi koymayın.
- Derleme çalışabilirliği GitHub Actions'ta test edilmemiştir. Hata olursa Actions günlüklerinden düzeltilir.
