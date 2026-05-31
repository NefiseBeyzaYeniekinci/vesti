# Vesti Web Uygulaması Master Geliştirme Promptu

Bu master prompt, başka bir IDE'de (VS Code, WebStorm, vb.) veya herhangi bir AI asistanı (Cursor, Claude, Copilot vb.) kullanarak **Vesti** mobil uygulamamızla **birebir tasarımsal ve işlevsel paralellikte** çalışan, göz alıcı bir web sayfası (dashboard + marketplace + chat) oluşturmak üzere tasarlanmıştır.

Aşağıdaki metni kopyalayıp web projesini yazdıracağın AI asistanına doğrudan gönderebilirsin.

---

```text
Lütfen modern, estetik açıdan büyüleyici, hızlı ve mobil uygulamamızla birebir senkronize ve paralel çalışacak olan "Vesti" Web Projesini oluştur.

Vesti, kullanıcıların dijital gardıroplarını yönettikleri, hava durumuna göre yapay zeka (AI) destekli kombin/outfit önerileri aldıkları ve kendi aralarında kıyafet satışı yapabildikleri (peer-to-peer marketplace) hibrit bir moda platformudur.

Teknoloji yığınını, tasarım yönergelerini ve sayfa yapılarını aşağıda belirttim. Projeyi TypeScript ve modern bileşen standartlarına uygun olarak tasarla.

---

### 1. TEKNOLOJİ YIĞINI & KÜTÜPHANELER
- **Framework:** Next.js (App Router, React) - SEO dostu, hızlı ve entegre yönlendirme.
- **Tasarım & UI:** Tailwind CSS + Shadcn UI (Koyu Mod ve Açık Mod uyumlu, Off-White ağırlıklı lüks hissettiren tasarım).
- **İkonlar:** Lucide React (Mobil uygulamadaki Material Icons ile uyumlu olacak şekilde).
- **Global State Management:** Zustand (Kullanıcı JWT token, Gardırop ve Sepet durumları için).
- **API İletişimi:** Axios (API Gateway ile iletişim, istek kesiciler -interceptors- ile otomatik JWT ekleme).
- **Real-Time:** socket.io-client (Anlık mesajlaşma senkronizasyonu için).

---

### 2. TASARIM DİLİ & RENK PALETİ
Mobil uygulama renklerini web sayfasında tamamen korumalıyız. Tasarımda premium, temiz bir "Off-White" hissi, yumuşak gölgeler, yuvarlatılmış köşeler (rounded-2xl) ve cam morfolojisi (glassmorphic) efektleri kullan:

- **Primary (Ana Renk):** `#7986CB` (Classic Soft Indigo - Yumuşak İndigo)
- **Accent (Vurgu Rengi):** `#FF6F61` (Vivid Coral - Canlı Mercan)
- **Success Mood (Başarı/Nane Yeşili):** `#E8F5E9` (Mint Green - Nane Yeşili)
- **Text Main (Ana Yazı):** `#37474F` (Soft Charcoal - Yumuşak Kömür)
- **Dark Indigo (Kontrast/Koyu Arayüz Elemanları):** `#29294D` (Derin İndigo)
- **Background (Arka Plan):** `#F8F9FA` (Off-White - Kırık Beyaz)
- **Light Purple (Açık Mor/Kart Arka Planları):** `#EDE7F6` (Yumuşak Mor)

---

### 3. SAYFA YAPILARI & DETAYLI AKIŞLAR

Uygulamayı Next.js App Router yapısında şu sayfalar ve işlevlerle tasarla:

#### A. Onboarding / Karşılama Sayfası (`/onboarding`)
- Mobil uygulamadaki ilk açılış ekranıdır.
- Şık bir görsel karusel veya modern animasyonlar eşliğinde Vesti'nin 3 ana özelliğini tanıt: "Dijital Gardırop", "AI Kombin Danışmanı", "Pazar Yeri".
- "Kayıt Ol" ve "Giriş Yap" butonları.

#### B. Kimlik Doğrulama (`/login` ve `/register`)
- Mobil uygulama ile tamamen aynı backend veri tabanına bağlanır.
- Login: Email ve şifre doğrulaması. Başarılı girişte JWT token Zustand ve localStorage'a yazılır, kullanıcı `/` (dashboard) sayfasına yönlendirilir.
- Register: İsim, Email ve Şifre ile yeni hesap açma formu.

#### C. Dashboard / Anasayfa (`/`)
- Üstte "Hoş Geldin, [Kullanıcı]" kartı.
- **Hava Durumu ve AI Öneri Özeti Kartı:** Bulunulan konuma ait sıcaklık bilgisi (Weather API'den gelen veriyle) ve buna uygun giyilebilecek yapay zeka kombin önerisi ("Bugün hava serin, gardırobundaki Vintage Denim Ceketi tercih edebilirsin!").
- **Kısayollar Kartı:** Gardırobuna Git, AI Kombin Oluştur, Pazaryerini Keşfet.
- Gardıroptaki kıyafet sayısı ve son eklenen 3 ürünün mini önizlemesi.

#### D. Dijital Gardırop (`/wardrobe`)
- Kullanıcının kıyafetlerini listelediği ızgara (Grid) görünümü.
- **Kategori Filtreleri:** Ceket, Kazak, Pantolon, Ayakkabı, Diğer vb.
- Her kıyafet kartında görsel, kategori, renk, marka ve beden etiketleri yer almalı.
- **"Yeni Kıyafet Ekle" Modal Formu:** Sürükle-bırak görsel yükleme alanı, kategori seçimi, renk kutucukları, marka girdisi ve beden seçeneği. (POST `/api/wardrobe/upload` endpoint'ine `multipart/form-data` olarak gönderilir).
- Kartlar üzerinde "Kıyafeti Sil" (Çöp kutusu butonu) olmalı.

#### E. AI Kombin Oluşturucu (`/outfit`)
- İnteraktif bir sihirbaz arayüzü.
- Sol tarafta hava durumu simülatörü ve sıcaklık göstergesi.
- "Kombin Üret" butonuna basıldığında, gardıroptaki uygun kıyafetleri analiz ederek (AI API entegrasyonu ile) üst, alt ve ayakkabı kombinasyonunu görsel kartlar halinde sunan dinamik arayüz.

#### F. Pazar Yeri Feed (`/marketplace`)
- Diğer kullanıcıların satışa çıkardığı kıyafetlerin listelendiği modern alışveriş feed'i.
- **Arama Barı ve Filtreler:** Kategoriye, bedene, fiyata ve duruma (Yeni-Etiketli, Çok İyi, İyi, Kullanılmış) göre filtreleme.
- Kıyafet kartlarında görsel, satıcı profili, başlık, fiyat (TRY) ve beden bilgisi.
- Karta tıklandığında `/marketplace/product/[id]` sayfasına yönlendirme.

#### G. Ürün Detay Sayfası (`/marketplace/product/[id]`)
- Sol tarafta büyük kıyafet görseli, sağ tarafta ürün başlığı, fiyatı, durumu, boyutu ve satıcı bilgileri.
- **"Satıcıyla Sohbet Et" Butonu:** Tıklandığında anlık olarak o satıcıyla olan sohbet sayfasına (`/messages/chat/[sellerId]`) yönlendirir.
- **"Hemen Satın Al" Butonu:** Ürünü güvenli ödeme ekranına (`/checkout/[id]`) taşır.

#### H. Mesaj Kutusu ve Sohbet Ekranı (`/messages` & `/messages/chat/[userId]`)
- **Gelen Kutusu:** Kullanıcının aktif olan tüm sohbet odalarını listelediği sol panel (son mesaj, gönderici adı ve profil resmi ile).
- **Sohbet Alanı (Chat Window):** Seçilen kullanıcıyla anlık mesajlaşma ekranı.
- **Real-Time Sync:** Socket.io bağlantısı kurulmalı. Kullanıcı mesaj gönderdiğinde anında karşı tarafa iletilmeli ve veri tabanına yazılmalı. Mesaj balonları modern ve yumuşak mor/gri tonlarında olmalı.

#### I. Güvenli Ödeme Ekranı (`/checkout/[itemId]`)
- Satın alınmak üzere olan kıyafetin özeti (Görsel, başlık, fiyat, kargo).
- Kart Bilgileri Giriş Formu (Ad Soyad, Kart No, SKT, CVC) - mock ödeme simülasyonu.
- Ödeme başarılı olduğunda şık bir konfeti animasyonu ve "Gardırobuna Eklendi" tebriği.

#### J. Profil ve Ayarlar (`/profile`)
- Kullanıcı profil resmi, adı, e-posta adresi.
- Gardırop istatistikleri (Eklenen kıyafet, yapılan satış, alınan ürün sayıları).
- Uygulama ayarları ve "Çıkış Yap" butonu (Zustand ve local tokenları sıfırlayarak `/login` sayfasına yönlendirir).

---

### 4. API VE ENDPOINT ENTEGRASYON DETAYLARI
Tüm istekleri `http://localhost:8080` (API Gateway) adresi üzerinden gerçekleştirmelisin:

1. **Authentication Service:**
   - POST `/api/auth/login` -> Gönderilen: `{email, password}`. Dönen: `{token, user}`.
   - POST `/api/auth/register` -> Gönderilen: `{name, email, password}`. Dönen: `{token, user}`.
2. **Wardrobe Service:**
   - GET `/api/wardrobe/items` -> Kullanıcının gardırobunu çeker.
   - POST `/api/wardrobe/upload` -> `FormData` tipinde görsel ve `{category, color, brand, size}` yükler.
   - DELETE `/api/wardrobe/items/:id` -> Kıyafeti siler.
3. **Marketplace Service:**
   - GET `/api/marketplace/items` -> Tüm ilanları listeler.
   - GET `/api/marketplace/items/:id` -> İlan detayını getirir.
   - POST `/api/marketplace/items` -> Yeni satış ilanı oluşturur.
4. **Messaging Service & WebSockets:**
   - Socket adresi: `http://localhost:8080`
   - Kullanıcı bağlandığında `connection` eventi ile sokete dahil edilir. `socket.emit('message', { to, text })` ve `socket.on('message', callback)` entegrasyonu kurulmalıdır.
5. **Weather & AI Service:**
   - GET `/api/weather` -> Güncel hava durumunu getirir.
   - POST `/api/ai/outfit` -> Gardıroptaki ürün listesini ve hava durumunu alarak AI kombin önerisi üretir.

---

### 5. PROJEDEN BEKLENEN ESTETİK KALİTE
- **Mikro Animasyonlar:** Framer Motion kullanarak sayfa geçişlerinde, modal açılışlarında ve buton hover durumlarında yumuşak sönümleme ve kayma efektleri ekle.
- **Mobil Uyum (Responsive):** Büyük ekranlarda yan menülü veya geniş üst menülü, mobil ekran genişliklerinde ise tam olarak mobil uygulamadaki gibi bir **Alt Navigasyon Barı (Bottom Navigation Bar)** şeklinde değişen esnek bir grid tasarımı kullan.
- **Görsel Placeholder:** Yüklenemeyen resimler için şık bir gardırop ikonu önizlemesi sun.

Lütfen bu mimariyi temel alan şık, eksiksiz ve mobil uygulamamızla paralel çalışan web sitesi kod yapısını oluşturmaya başla!
```
