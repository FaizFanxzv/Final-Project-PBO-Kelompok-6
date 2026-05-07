# 🧟 LAST CHANCE FOR LIFE

**Turn-Based RPG Zombie Survival — Bertahan atau Binasa**

[![Version](https://img.shields.io/badge/versi-v2.0.0-FFB830?style=for-the-badge)](https://github.com/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk)](https://github.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-0096C7?style=for-the-badge)](https://github.com/)
[![Status](https://img.shields.io/badge/status-Final-2D7A45?style=for-the-badge)](https://github.com/)
[![PBO](https://img.shields.io/badge/Tugas%20Akhir-PBO-CC3300?style=for-the-badge)](https://github.com/)
[![Genre](https://img.shields.io/badge/Genre-Turn--Based%20RPG-7755BB?style=for-the-badge)](https://github.com/)

> *"Ketika dunia berakhir, hanya yang kuat yang berhak untuk bertahan."*

---

## 📖 Tentang Games

Wabah zombie melanda seluruh penjuru dunia. Kota-kota besar luluh lantak, peradaban runtuh dalam hitungan hari. Di tengah kekacauan ini, **5 Survivor** dengan kemampuan unik masing-masing bangkit untuk melawan gelombang zombie yang tak pernah berhenti.

Dari lingkungan **Kampus UNESA** hingga puncak **Frozen dan Mountain**, dari lebatnya **Dark Forest** hingga jantung markas **Zomboss** — setiap medan memiliki bahayanya sendiri. Pilih karaktermu, pilih mapmu, dan buktikan: kamu adalah **Last Chance for Life**.

---

## 🧍 Karakter Player

> Pilih karaktermu dengan bijak — setiap kemampuan unik menentukan strategi bertahan hidup.

| Karakter | Keunikan | Bonus Map |
|----------|----------|-----------|
| 🟦 **UnesaBoys** | ATK +10% saat bermain di Map Unesa | ✅ Map Unesa |
| 🟪 **UnesaGirls** | ATK +10% saat bermain di Map Unesa | ✅ Map Unesa |
| ⚔️ **Knight Prince** | Default ATK +5% dari awal (passive permanen) | ✅ Forest |
| 👁️ **Raymond** | InvisibleChance 30% dari awal, namun ATK -10 default | ✅ Frozen |
| 🩸 **Xavier** | Lifesteal: pulihkan HP 10% dari damage yang diberikan ke enemy | ✅ Mountain |

### Detail Kemampuan Pasif

```
UnesaBoys / UnesaGirls
└── Sinergi dengan Map Unesa → ATK +10% (berlaku selama di area Unesa)

Knight Prince
└── Passive: ATK selalu +5% dari nilai default tanpa syarat apapun

Raymond
└── Passive ON : InvisibleChance 30% (dodge) dari awal game
└── Trade-off  : ATK -10 dari nilai default

Xavier
└── Passive: Setiap serangan ke enemy → HP Xavier pulih 10% dari damage yang diberikan
```

---

## 🗺️ Maps

> Setiap map memiliki efek lingkungan yang mempengaruhi strategi bertahan hidup.

### 🎓 Map Unesa
```
Lokasi   : Kawasan Kampus UNESA
Efek     : Zombie damage +5% setiap wave (semakin lama semakin brutal)
Bonus    : UnesaBoys & UnesaGirls mendapat ATK +10%
Strategi : Ideal untuk pemain UnesaBoys/Girls yang ingin farming damage tinggi
```

### 🌲 Map Forest *(Default)*
```
Lokasi   : Hutan Gelap Tak Berujung
Efek     : Tidak ada efek tambahan — map standar
Bonus    : -
Strategi : Cocok untuk pemula, tidak ada penalti maupun bonus
```

### ❄️ Map Frozen
```
Lokasi   : Pegunungan Beku
Efek     : Player dapat terkena Freeze / Immobilize
           → Saat Freeze, zombie melaju bebas dan menyerang tanpa hambatan
Bonus    : -
Strategi : Butuh karakter dengan dodge tinggi seperti Raymond untuk menghindari freeze
```

### ⛰️ Map Mountain
```
Lokasi   : Pegunungan Terjal
Efek     : Area bahaya serangan zombie semakin luas dari biasanya
           → Jangkauan serangan zombie meningkat drastis
Bonus    : -
Strategi : Prioritaskan karakter dengan HP tinggi atau Lifesteal (Xavier)
```

### 💀 Map Zomboss *(Hardcore)*
```
Lokasi   : Markas Pusat Zomboss
Efek     : Boss Zomboss dapat diakses sejak Wave 1
           → Setiap 3 detik: HP Player terkikis 5%
           → Damage zombie +50% dari ATK Default (brutal)
Bonus    : -
Strategi : Hanya untuk pemain berpengalaman. Buff Last Chance sangat disarankan.
```

### Perbandingan Tingkat Kesulitan Map

| Map | Kesulitan | Efek Zombie | Efek Lingkungan | Rekomendasi Karakter |
|-----|-----------|-------------|-----------------|---------------------|
| 🌲 Forest | ⭐ Normal | Default | Tidak ada | Semua karakter |
| 🎓 Unesa | ⭐⭐ Sedang | +5%/wave | Buff Unesa Char | UnesaBoys / UnesaGirls |
| ⛰️ Mountain | ⭐⭐⭐ Sulit | Default | Area bahaya luas | Xavier (Lifesteal) |
| ❄️ Frozen | ⭐⭐⭐ Sulit | Default | Freeze Player | Raymond (Dodge) |
| 💀 Zomboss | ⭐⭐⭐⭐⭐ Ekstrem | +50% ATK | HP -5% per 3 detik | Knight Prince + Last Chance |

---

## 🧟 Sistem Zombie Wave

> Gelombang zombie semakin brutal seiring berjalannya waktu.

```
Wave 1  → Zombie menyerang setiap 10 detik
Wave 2  → Zombie menyerang setiap 9 detik
Wave 3  → Zombie menyerang setiap 8 detik
  ...
Wave 10 → Zombie menyerang setiap 3 detik ← PUNCAK KEGANASAN
```

**Mekanik Wave:**
- Zombie aktif **maju menyerang** player (tidak diam di tempat)
- Setiap zombie memberikan **damage tiap 3 detik** saat berhasil menjangkau player
- Interval serangan **memendek setiap wave** hingga minimum 3 detik di wave terakhir
- Jumlah zombie per wave **bertambah** setiap gelombang

---

## ⚡ Sistem Buff

> Pilih Buff untuk memperkuat karakter sebelum atau selama battle.

| # | Nama Buff | Efek |
|---|-----------|------|
| 1 | ⚔️ **God Slayer** | +65 ATK |
| 2 | 👊 **Punch Strike** | +20 ATK |
| 3 | 🛡️ **Steel Heart** | +40 Max HP |
| 4 | 🏰 **Strong Defense** | +65 Max HP |
| 5 | 🩸 **Lifesteal** | +15 ATK + serap 10% MaxHP setiap serangan |
| 6 | 👁️ **Invisible** | +15 Dodge Chance (maksimal 75%) |
| 7 | 💫 **Last Chance** | Bangkit sekali saat mati dengan 100% HP |

> 💡 **Tips Kombinasi:**
> - **Xavier + Lifesteal** → Sustain HP maksimal di segala map
> - **Raymond + Invisible** → Dodge chance mendekati 75%, hampir tak tersentuh
> - **Map Zomboss** → Wajib bawa **Last Chance** sebagai jaring pengaman

---

## ✅ Status Fitur

### Sistem Core

| Fitur | Status |
|-------|--------|
| Turn-based combat system | ✅ |
| 5 karakter player dengan passive unik | ✅ |
| Sistem zombie wave (interval 10s → 3s) | ✅ |
| Zombie aktif maju & memberikan damage | ✅ |
| Kontrol keys maju/mundur saat battle | 🔧 Diperbaiki |
| Sistem buff 7 jenis | ✅ |
| Invisible / dodge mechanic | ✅ |
| Lifesteal mechanic (Xavier & buff) | ✅ |

### Map System

| Fitur | Status |
|-------|--------|
| Map Forest (default) | ✅ |
| Map Unesa + sinergi karakter | ✅ |
| Map Frozen + efek freeze / immobilize | ✅ |
| Map Mountain + area bahaya luas | ✅ |
| Map Zomboss + HP drain + damage brutal | ✅ |

### UI & Experience

| Fitur | Status |
|-------|--------|
| Layar pilih karakter | ✅ |
| Layar pilih map | ✅ |
| Indikator HP real-time | ✅ |
| Notifikasi wave berikutnya | ✅ |
| Animasi zombie menyerang | ✅ |
| Save / Load progress | ✅ |

### Dalam Pengembangan

| Fitur | Status |
|-------|--------|
| BGM & sound effect per map | 🚧 |
| Cutscene intro & ending | 🚧 |
| Leaderboard skor | 🚧 |
| Animasi karakter unik per hero | 🚧 |

---

## 🚀 Cara Menjalankan

```bash
git clone https://github.com/USERNAME/LastChanceForLife.git
cd LastChanceForLife
mvn javafx:run
```

**Requirements:** JDK 25+, Maven 3.9+, JavaFX 25

### Kontrol

| Tombol | Aksi |
|--------|------|
| `←` / `→` | Gerak kiri / kanan |
| `↑` / `↓` | Gerak maju / mundur |
| `Z` / `Space` | Serang |
| `X` | Gunakan skill / buff |
| `ESC` | Pause / Menu |

---

## 🏗️ Arsitektur Project

```
lastchanceforlife/
├── engine/          → GameEngine, WaveManager, BattleController
├── entity/
│   ├── player/      → UnesaBoys, UnesaGirls, KnightPrince, Raymond, Xavier
│   └── enemy/       → Zombie, ZombieBoss, ZombossMinion
├── map/             → MapUnesa, MapForest, MapFrozen, MapMountain, MapZomboss
├── buff/            → BuffManager, BuffEffect (7 jenis buff)
├── combat/          → TurnManager, DamageCalculator, InvisibleSystem, LifestealSystem
├── save/            → SaveManager, GameState
└── ui/
    ├── MainApp.java            → Entry point (1280×720)
    ├── controller/SceneRouter  → Scene management
    ├── util/UIFactory          → Komponen reusable
    └── view/                   → CharacterSelect, MapSelect, Battle, GameOver
```

**Design Patterns yang digunakan:** Observer, Factory, Strategy, Template Method, State Machine, Singleton

---

## 🎨 Palette Warna — Dark Survival

| Nama | Hex | Digunakan untuk |
|------|-----|----------------|
| Background | `#0A0A0A` | Latar utama gelap |
| Panel | `#1A1A1A` | Card, panel |
| Danger Red | `#CC3300` | HP kritis, serangan |
| Safe Green | `#2D7A45` | HP penuh, heal |
| Warning Yellow | `#FFB830` | Buff aktif, warning |
| Zombie Green | `#4A7C59` | Elemen zombie |
| Frost Blue | `#0096C7` | Map Frozen, efek freeze |
| Mythic Purple | `#7755BB` | Buff langka |
| Teks Utama | `#EDE0C8` | Konten teks |

---

## 📝 Tim Pengembang

> Tugas Akhir — Pemrograman Berorientasi Objek
> Semester 2 | 2026
> **Kelompok 6 Paling COMELLL**

| Nama | NIM | Peran |
|------|-----|-------|
| Muhammad Faiz Risqullah Ramadhan | 25050974116 | Project Lead & Game Logic |
| Ahmad Khadik Mustawan 'Alwi | 25050974121 | UI/UX & Scene Management |
| Bunga Aulia Maharani | 25050974122 | Entity & Combat System |
| Varsaretha Najmi Rohadatul Aisy | 25050974124 | Entity & Combat System |
| Arafina A'azahra | 25050974130 | Entity & Combat System |

---

*Lihat `CHANGELOG.md` untuk riwayat versi lengkap.*

*Dibuat dengan ❤️ — Semoga dunia tidak benar-benar diserang zombie 🧟*
