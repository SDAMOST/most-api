# Model Domenowy — SDA MOST

## 1. Słownik (Ubiquitous Language)
- **MOST** – Wspólnota akademicka (Młodzieżowy Ośrodek Służby Twórczej).
- **Przęsło** – Główny dział MOST-u (Formacyjne, Turystyczne, Gospodarcze).
- **Podprzęsło** – Konkretna inicjatywa w ramach przęsła (Lectio, Rajdy, Obiady).
- **Initiative (Program)** – Definicja stałej inicjatywy (np. "Lectio", "Rajdy").
- **Occurrence** – Konkretna realizacja z datą (np. "Lectio 12.10.2026").
- **Enrollment** – Zapis uczestnika na Occurrence.
- **Attendance** – Potwierdzona obecność na Occurrence.

## 2. Moduły (Bounded Contexts)

### Identity (Tożsamość)
- **Agregat:** `CommunityMember`
- **Kluczowe Reguły:** Aktywacja konta emituje `MemberActivated`.

### Structure (Struktura MOST-u)
- **Agregat:** `OrganizationUnit` (Przęsła / Podprzęsła).
- **Encja:** `LeadershipAssignment`.
- **Kluczowe Reguły:** Przypisania ról (Kadra, Przęsłowy, Podprzęsłowy) są ograniczone czasowo (from, to).

### Activities (Inicjatywy i Kalendarz)
- **Agregat:** `Initiative` (Właściciel: podprzęsło).
- **Agregat:** `Occurrence` (Cykl życia: `PLANNED` -> `PUBLISHED` -> `COMPLETED`/`CANCELLED`).
- **Encja:** `ScheduleRule`.
- **Kluczowe Reguły:** Zmiana terminu (Reschedule) zapisuje log (stara data, nowa data, powód). Generator tworzy Occurrence wg ScheduleRule.

### Participation (Zapisy i Obecność)
- **Agregat:** `Enrollment`.
- **Agregat:** `Attendance`.
- **Kluczowe Reguły:** Brak możliwości zapisu po starcie Occurrence.

### Engagement (Punkty)
- **Agregat:** `PointsLedger` (Konto punktowe).
- **Encja:** `PointsTransaction`.
- **Kluczowe Reguły:** Maksymalnie 4 pkt/m-c za przęsło normalne, brak limitu dla gospodarczego. Suma to wynik agregacji, nie pole w bazie.

### Communication (Powiadomienia)
- **Kluczowe Reguły:** Subskrypcja (Opt-in) na powiadomienia o wybranch Inicjatywach (Lectio, Rajdy, itp.).
