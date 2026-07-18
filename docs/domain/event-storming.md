ADR (Architecture Decision Record) albo dokument "Domain Discovery". Wnioski z Event Stormingu. Materiał do dyskusji z zespołem.

---

Event Storming – odkrycia dotyczące modelu domenowego MOST

Cel

Celem było zrozumienie rzeczywistego modelu biznesowego MOST-u, a nie projektowanie klas czy tabel bazy danych. Analiza została oparta na rzeczywistym funkcjonowaniu wspólnoty, stronie internetowej MOST-u oraz doświadczeniach członków.


---

Najważniejsze odkrycia

1. System nie jest aplikacją do Eventów

Początkowo zakładaliśmy model:

Activity
├── Event
└── Duty

Po analizie okazało się, że taki podział nie odpowiada rzeczywistości MOST-u.

MOST nie organizuje po prostu "eventów".

MOST prowadzi stałą działalność duszpasterską, która przejawia się w różnych formach.


---

2. Głównym pojęciem domenowym jest Program

Robocza nazwa:

Program

Program opisuje stałą inicjatywę prowadzoną przez MOST.

Przykłady:

Obiady mostowe

Lectio Divina

Tabor

CUD

Reflektor

MKF

Wieczorki Kulturalne

Rajdy

Kajaki

Biały Dunajec

MOST Intro

MOST Outro


Program:

istnieje przez wiele lat,

może zostać zarchiwizowany,

może zostać zastąpiony innym programem (np. KKO → CUD),

ma właściciela (przęsło),

definiuje sposób działania.



---

3. Program jest definicją, a nie konkretnym wydarzeniem

Przykład:

Program
└── Lectio Divina

nie oznacza spotkania.

Spotkaniami są:

Lectio
09.11

Lectio
16.11

Lectio
23.11

Analogicznie:

Rajdy

↓

Rajd Jesienny 2026

Rajd Mikołajkowy 2026

Program jest więc wzorcem, z którego powstają konkretne realizacje.


---

4. Program definiuje domyślne zasady

Program odpowiada za:

domyślny dzień tygodnia,

domyślną godzinę,

odpowiedzialne przęsło,

prowadzącego lub sposób jego wyznaczania,

czy wymagane są zapisy,

czy istnieje limit miejsc,

czy potrzebni są wolontariusze,

domyślny harmonogram,

sposób organizacji.


Przykład:

Program:
Lectio

dzień:
Poniedziałek

godzina:
20:00

zapisy:
Nie

wolontariusze:
Nie

prowadzący:
Podprzęsłowy Lectio

Natomiast konkretne spotkanie może nadpisać część tych ustawień (np. zostać odwołane lub mieć zastępczego prowadzącego).


---

5. Konkretna realizacja programu

Drugim ważnym pojęciem jest roboczo nazwane:

Occurrence

Przykłady:

Lectio 12.10.2026

Rajd Jesienny 2026

CUD


Occurrence posiada:

konkretną datę,

status,

miejsce,

ewentualne zmiany względem programu,

uczestników,

organizatorów,

komunikację,

harmonogram dnia.



---

6. Nie wszystkie Occurrence są jednakowo złożone

Można wyróżnić trzy poziomy złożoności.

Spotkanie

Przykłady:

Lectio

Tabor

Iloraz

Wieczorki


Najczęściej:

brak zapisów,

brak limitów,

brak organizacji.



---

Wydarzenie

Przykłady:

Rajd

Kajaki

Rekolekcje


Dochodzi:

zapis uczestników,

limit miejsc,

agenda,

organizacja.



---

Projekt

Przykłady:

Biały Dunajec

Sylwester


Dochodzi:

zespół organizacyjny,

transport,

logistyka,

noclegi,

większa liczba odpowiedzialnych.


Nie wiemy jeszcze, czy wymaga to osobnych agregatów, czy tylko dodatkowych modułów.


---

7. Odkrycie dotyczące wolontariatu

Początkowo model zakładał:

Duty

Po analizie okazało się, że rzeczywistość wygląda inaczej.

Przykłady:

przygotowanie kolacji,

sprzątanie po kolacji,

czytania,

psalm,

czytanie skrzynki intencji,

przyogowanie obiadów.


Wspólny proces wygląda następująco:

Powstaje potrzeba

↓

Koordynator szuka osób

↓

Osoby zostają przypisane

↓

Posługa zostaje wykonana

↓

Przyznawane są punkty

To nie jest klasyczny "dyżur".

Jest to proces koordynacji wolontariuszy.

Robocze nazwy:

VolunteerNeed

ServiceNeed



---

8. Uczestnictwo i wolontariat są różnymi procesami

Nie należy ich łączyć.

Uczestnik

Occurrence

↓

Enrollment


---

Wolontariusz

VolunteerNeed

↓

VolunteerAssignment

To dwa różne modele biznesowe.


---

9. Nie wszystko warto modelować

Poza zakresem aplikacji pozostają:

prywatne wyjścia znajomych,

spontaniczne spotkania organizowane bez udziału MOST-u,

nieformalne "Lectio po Lectio".


Choć powstają dzięki relacjom zbudowanym we wspólnocie, nie są oficjalnymi procesami MOST-u.


---

Kandydaci na główne pojęcia domenowe

Program
Occurrence
Enrollment
VolunteerNeed
VolunteerAssignment
CommunityMember
Membership
Group


---

Najważniejsze pytania otwarte

1. Czy Program rzeczywiście będzie głównym agregatem domenowym?


2. Jak powinny wyglądać granice agregatów?


3. Czy Occurrence jest osobnym agregatem?


4. Czy VolunteerNeed jest agregatem?


5. Czy projekty (np. Biały Dunajec) wymagają osobnego modelu, czy są jedynie bardziej rozbudowanymi Occurrence?




---

Co dalej?

Czego jeszcze brakuje? Obiady mostowe, coś jeszcze...?

Moim zdaniem następny krok to Aggregate Discovery.

Nie będziemy już odkrywać nowych pojęć, tylko dla każdego z kandydatów odpowiemy na pytania:

Kto jest właścicielem danych?

Jakie są niezmienniki (invariants)?

Jakie komendy może wykonać?

Jakie zdarzenia publikuje?

Gdzie powinny przebiegać granice transakcji?


To jest naturalny kolejny etap po Event Stormingu i właśnie wtedy powstanie właściwy model DDD.
