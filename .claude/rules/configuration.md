---
paths:
  - "src/main/resources/**"
  - "src/test/**"
  - "pom.xml"
  - "docker/**"
  - ".env.example"
---

# Configuration

## Environment and profiles

- `application.properties` imports an optional `.env` (ignored; `.env.example` is tracked and lists every variable).
  Every environment value is `${VAR:fallback}`: `SPRING_PROFILES_ACTIVE`, `DB_URL`, `DB_USER`, `DB_PASSWORD`,
  `JWT_SECRET`, `JWT_ISSUER`, `JWT_EXPIRY` (seconds), `CORS_ORIGIN`. Process env beats `.env`. A new variable goes into
  `.env.example`, under a section header comment.
- Profiles (default `developing`): `developing` H2 in-memory, `/h2-console`, SQL logging; `debugging` (group, includes
  `developing`) adds DEBUG app/security/SQL and bind logging; `production` PostgreSQL from `DB_*` and `JWT_SECRET`
  with no fallbacks, console off; `testing` H2 in-memory, console off, test classpath only. Shared settings stay in
  `application.properties`. Group members load after the group, so `developing` overrides `debugging` on shared keys.
- Banners: one per profile in `resources/banners/<profile>.txt` (`testing` under `src/test`), selected by
  `spring.banner.location` in each profile file; `debugging` sets `trimly.banner` because `developing` loads last.

## Database

- Flyway owns the schema (`src/main/resources/db/migration/V<n>__<description>.sql`); `ddl-auto=validate` only checks
  entity mappings. Stock Boot autoconfiguration (`spring-boot-flyway` declared in `pom.xml`;
  `flyway-database-postgresql` added); config under `spring.flyway.*` (`baseline-on-migrate=true`,
  `baseline-version=0`). Checksums live in each database's `flyway_schema_history`: H2 is rebuilt on every start, but
  a persisted Postgres fails validation after a migration is edited or renamed until `flyway repair` or a recreate.
- Seeds (every insert guarded by `WHERE NOT EXISTS`): `V2__seed_admin_user` admin `admin@trimly.com` / `admin123`;
  `V3__seed_sample_data` 3 `CLIENTE` and 2 `ADMIN` users (password `123456`), 5 `ATIVO` serviços, disponibilidades
  `SEGUNDA`-`QUINTA` 07h-12h/13h-18h and `SEXTA` 09h-12h/13h-16h. No agendamentos, no `DONO`. Both seeds apply in
  every profile, including `production`: change them before a real deployment.

## Build, tests and tooling

- Tests: run on the `testing` profile. Controller tests use `@AutoConfigureMockMvc(addFilters = false)`: the security
  chain is off, so they cover behavior only, not authorization. Don't add test classes unless asked.
- Sonar: `sonar-maven-plugin` + JaCoCo in `pom.xml`. `sonar.coverage.exclusions` skips DTOs, `BackendApplication`,
  `infra/`, `model/entity/`, `model/exception/` (coverage only, smells still reported). `lombok.config` marks Lombok
  code so JaCoCo ignores it.
- `docker/compose.yaml` runs `postgres`, `api` (built by `docker/Dockerfile`: JDK 17 build stage, JRE 17 runtime) and
  `sonarqube` (profile `sonar`). Run from the repo root with `--env-file .env`. Containers are named
  `trimly-backend-<name>`.
- Lombok annotation processing is wired through `maven-compiler-plugin` in `pom.xml`.
- Maven profiles: `dev` (`activeByDefault`) holds `h2` and `spring-boot-h2console`, so `spring-boot:run`, `verify`
  and Sonar keep H2; `production` is empty and, activated explicitly, disables `dev`. Production builds
  (`docker/Dockerfile`, `setup.sh`) use `-Pproduction`; the jar then has no H2.

## BrasilAPI (feriados)

- `FeriadosClient` (`brasilapi-feriados`): timeouts in `application.properties`, connect `2000` ms, read `3000` ms.
- `@Cacheable(CacheConfig.FERIADOS_CACHE)` on `FeriadosClient.listarPorAno`: one in-memory cache per year
  (`infra/CacheConfig`, `ConcurrentMapCacheManager`), shared by `FeriadoService` and `AgendamentoValidator`, never
  expires (holidays of a year are fixed), lost on restart. A failed call is not cached.
