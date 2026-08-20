## 1. Cel
Wsparcie organizacji i uczestnictwa w życiu SDA MOST.

Apka mostu jako Strona internetowa na przeglądrkę i apka mobilna na telefony (pwa) daje:
wszystkim przeglądanie:
- kalendarz i wydarzenia,
- posty,
- treści (konstrukcja, galeria, duchowość, Msze i Spowiedź, kontakt)
dla zalogowanych:
- zapisy na wydarzenia
dla kadry:
- zarządzanie wydarzeniami, postami i treściami.


## 2. Użytkownicy
- **Mostowiak** — korzysta z aplikacji.
- **Podprzęsłowy** — odpowiada za podprzęsło.
- **Przęsłowy** — odpowiada za przęsło.
- **Sekstet / Septet** — najwyższe grono zarządzające mostem.

## 3. Zakres
- konta i członkostwo w moscie
- konstrukcja mostu
- serie wydarzeń i wydarzenia
- zapisy na wydarzenia
- posty

## 4. Ustalenia
- Rejestracja konta → oczekiwanie na weryfikację przez Sekstet → aktywne konto.
- Konto `PENDING` może próbować się zalogować i otrzymuje informację o oczekiwaniu na weryfikację.
- Konstrukcja MOST-u: Sekstet / Septet → Przęsło → Podprzęsło.
- Przęsło ma jednego Przęsłowego.
- Podprzęsło ma jednego Podprzęsłowego.
- Seria wydarzeń należy do elementu konstrukcji: MOST-u, Sekstetu / Septetu, Przęsła lub Podprzęsła.
- Wydarzenie należy do jednej Serii wydarzeń i po utworzeniu jest widoczne w kalendarzu.
- Zapisy na wydarzenie są niezależne od samego istnienia wydarzenia.
- Lista rezerwowa działa według FIFO i jest obsługiwana automatycznie.
- Posty są publiczne.

## 5. Otwarte kwestie
- Co dokładnie oznacza odrzucenie i dezaktywacja konta oraz czy konto można ponownie aktywować?
- Kto może tworzyć, edytować i odwoływać wydarzenia?
- Jak i kto powiniem odpowiadać za kalendarz, serie wydarzeń i wydarzenia?
- Czy robimy zapisywanie innej osoby?
- Czy post może edytować osoba inna niż jego autor?
- Kto może usuwać posty?
- Czy posty mogą zawierać załączniki, być zaplanowane lub przypięte?
- Czy robimy listy od zapisywania się na czytania, scholę, robienie kolacji, sprzątnie?
- Czy potrzebujemy rejestrowania obecności?

## 6. Stan projektu

### Backend — `most-api`
- Modular Monolith: Identity, Structure, Activities, Participation, Engagement, Communication.
- Rejestracja, logowanie i JWT.
- Konstrukcja MOST-u i przypisywanie funkcji.
- Serie wydarzeń, harmonogramy i wydarzenia.
- Zapisy i obecność.
- Punkty zaangażowania.
- Powiadomienia i subskrypcje.
- Testy integracyjne i migracje Flyway.

### Frontend — `most-web`
- React + Vite + TypeScript + PWA.
- Kalendarz, wydarzenia i zapisy połączone z API.
- Część zarządzania użytkownikami i Konstrukcją działa na mockach / `localStorage`.
- Frontend nie ma jeszcze spójnego modelu autoryzacji.

### Do spięcia
- Konstrukcja na froncie z backendowym modułem `Structure`.
- Zarządzanie użytkownikami z backendem.
- Docelowy model uprawnień.
- Część funkcji istniejących obecnie w backendzie może być zbędna.


## 7. Najbliższy krok
Ustalenie modelu apki i uprawnień z zespołem na podstawie otwartych kwestii.
