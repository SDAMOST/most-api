# Architektura Systemu — SDA MOST

## 1. Stos Technologiczny

| Obszar | Technologie |
|--------|-------------|
| **Backend** | Java 21, Spring Boot 4.1.0 |
| **Baza Danych** | PostgreSQL (relacyjna) |
| **ORM / Data** | Hibernate, Spring Data JPA |
| **Testy** | JUnit 5, Testcontainers |

## 2. Decyzje Architektoniczne (ADR)
- **Modularny Monolit** (zamiast Mikroserwisów) – Mały zespół, prostszy deployment, logiczny podział wspierany przez `Spring Modulith`.
- **Podejście DDD** – Logika rezyduje w Agregatach, nie w "grubych" Serwisach.
- **Event-Driven Architecture (wewnątrz monolitu)** – Moduły komunikują się przez Zdarzenia Domenowe (Domain Events), co zmniejsza sprzężenie. 

## 3. Zależności Modułów
Kierunek zależności (lewe nie znają prawych, prawe znają lewe):
`Identity` -> `Structure` -> `Activities` -> `Participation` -> `Service` -> `Engagement` -> `Communication`

## 4. Przepływ Sterowania (Command -> Event)
Wzorzec komunikacji dla wszystkich akcji:
1. API przyjmuje DTO i emituje `Command`.
2. `Handler` (Application Service) znajduje `Agregat` i wywołuje na nim metodę biznesową.
3. `Agregat` weryfikuje reguły biznesowe, mutuje stan i emituje `DomainEvent`.
4. Transakcja DB zapisuje stan agregatu.
5. Inne moduły reagują na `DomainEvent` poprzez `@EventListener` i wykonują `Policy` (akcję następczą).

## 5. Struktura Repozytorium
- `/src/main/java/pl/salezjanie/most/{module}`
