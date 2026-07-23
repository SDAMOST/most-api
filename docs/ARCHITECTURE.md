# AI Agent Rules for SDA MOST Project

- **Architecture**: Modular Monolith. Strictly adhere to Bounded Contexts.
- **Packages**: `pl.salezjanie.most.{module}.{layer}`. Layers are: `domain`, `infrastructure`, `application`, `api`.
- **Stack**: Java 25 (Records, Virtual Threads), Spring Boot 4.1.0, Spring Data JPA, PostgreSQL.
- **Style**: Use DDD Lite. Domain entities should not be anemic (put business logic inside). Use Records for DTOs and Events. Use EDA.
- **Language**: Write all code, variables, classes, and comments strictly in English.
- **Action**: Always read `/docs/DOMAIN.md` and `/docs/ARCHITECTURE.md` before generating code. Do not invent new modules. Do not generate 500 lines at once; work step by step.