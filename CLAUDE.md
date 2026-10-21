# CLAUDE.md

Context file. Holds only what Claude needs to work here: architecture, conventions, patterns, business rules.
No changelogs, task notes, tutorials, or anything derivable from the code. State each fact once; edit in place.

Academic barbershop-scheduling API, Spring Boot. Domain language PT-BR (entities, DTOs, exceptions, validation
messages); framework/scaffolding English. `Servico` service, `Usuario` user, `Agendamento` booking,
`Disponibilidade` availability.

## Code Principles

- YAGNI: no dead, unused or decorative code. Remove what nothing references (methods, repository queries,
  exceptions, imports, commented-out code).
- KISS: complexity must be justified. If a simpler form gives the same result, use it (streams over
  flag-and-break loops, `return repository.save(entity)` over save-then-return).
- DRY: extract repeated logic only when the extraction is simpler than the repetition. Small justified
  repetition is fine.
- SOLID: only where it earns its keep; no interfaces or layers without a second use.
- Clean Code: clear, direct names; code organized by domain; split a method that does too much.

## Conventions

- Casing: `camelCase` (methods, vars, properties); `PascalCase` (classes, objects).
- Naming: English verb (`find`/`get`/`delete`) + PT-BR spec: `findByNome`, `getByEmail`. Vars: shortest
  descriptive name (`nome`, `usuarioId`, `isAtivo`).
- Method order: by action then specificity - `create` > `update` > `delete` > `findAll` > `findById` >
  `findByX`; public before private. `*Mapper`: each direction paired, single conversion before list
  (`toEntity`, `toEntityList`, then `toResponse`, `toResponseList`). `*Validator` exempt: methods follow the
  validation order documented below.
- Generics: parametrize a collection with a wildcard only for non-final types (`List<? extends XEntity>`);
  `final` types (records/DTOs) use the exact type (`List<XCreateDTO>`).
- Utility classes: `final` + private constructor.
- Single-line strings use plain literals; text blocks only for multi-line content or `\` continuation.
- Argument and null handling: `Objects.requireNonNull`/`requireNonNullElse` over manual `if ... throw new
  IllegalArgumentException` or `x != null ? x : y`; `StringUtils.hasText` over `s != null && !s.isBlank()`;
  `"...".formatted(...)` over `String.format`.
- Comments: none for commented-out code; line comments only for non-obvious intent.
- Formatting: Spotless + Eclipse JDT (4.40), settings in `eclipse-formatter.prefs`. 4-space indent, 8-space
  continuation, 120 cols; wrapped lists go one element per line, closing `)` on its own line. Run
  `./mvnw spotless:apply` before committing; `./mvnw verify` fails on unformatted code.

### Javadoc

- PT-BR, natural, concise. No HTML tags (paragraphs separated by a blank ` *` line), no em-dashes, no filler.
- Inline code uses backticks (`` `AGENDADO` ``), never `{@code}`; `{@link}` is fine. Method/type block tags:
  `@param`, `@return`, `@throws` only, in the form `@param name - desc`.
- Class Javadoc: one brief sentence; a second paragraph only for a non-obvious constraint.
- Fields: Javadoc on every field. Class-typed fields (injected collaborators, mocks, entities) get a 1-line
  description + `@see {@link Type}` on the next line (no blank line between); native/value fields (primitives,
  `String`, dates, constants) get the description only. `@see` is allowed only here.
- Tests: Javadoc on the class and its fields only, never on methods; same field rules.
- Swagger: `description` texts are the shortest form that keeps the behavior; omit `@Operation.description`
  when it only restates `summary`.

```java
/**
 * Mapper de Entidades utilizado para converter entre DTOs.
 * @see {@link ExampleMapper}
 */
private final ExampleMapper mapper;
```

## Graphify

Knowledge graph at `graphify-out/`. For dependency/call-graph questions use `graphify query "<question>"` /
`path "<A>" "<B>"` / `explain "<concept>"` before grep; `graphify-out/wiki/index.md` for navigation. Run
`graphify update .` after code changes.

## Commands

```bash
./mvnw spring-boot:run                    # run API (develop profile, in-memory H2)
./mvnw compile
./mvnw spotless:apply                     # format; -DspotlessFiles='.*/Foo\.java' limits to matching files
./mvnw verify                             # tests + spotless check
docker compose up -d                      # SonarQube at http://localhost:9000
./mvnw test sonar:sonar -Dsonar.token=<token> -Dsonar.host.url=http://localhost:9000
```

- Tests: only `BackendApplicationTests` (`contextLoads`). Don't add test classes unless asked.
- Sonar: `sonar-maven-plugin` + JaCoCo in `pom.xml`. `sonar.coverage.exclusions` skips DTOs, `BackendApplication`,
  `config/`, `model/entity/`, `model/exception/` (coverage only, smells still reported). `lombok.config` marks
  Lombok code so JaCoCo ignores it. `compose.yaml` runs SonarQube only.
- Profiles (`spring.profiles.active`, currently `develop`): `develop` H2 in-memory, `/h2-console`, SQL
  logging; `production` PostgreSQL at `localhost:5432/trimly`.
- Flyway owns the schema (`src/main/resources/db/migration/V<n>__<description>.sql`); `ddl-auto=validate`
  only checks entity mappings. Stock Boot autoconfiguration (`spring-boot-flyway` declared in `pom.xml`;
  `flyway-database-postgresql` added); config under `spring.flyway.*` (`baseline-on-migrate=true`,
  `baseline-version=0`). Editing a migration: H2 reapplies on restart; a persisted Postgres fails checksum
  validation until recreated.
- Lombok annotation processing is wired through `maven-compiler-plugin` in `pom.xml`.

## Architecture

3 tiers under `org.trimly.backend`: `model/`, `view/`, `controller/`. `model/` has one identical layered
structure per domain (`servico`, `usuario`, `agendamento`, `disponibilidade`); read one to understand the rest.
`auth` is a partial module (orchestration only: `AuthenticationService`, `TokenService`), driving `usuario`.

- `model/entity/<domain>/` - JPA `*Entity` + its enums (`UsuarioCargo`, `AgendamentoStatus`, `ServicoStatus`,
  `DiaSemana`).
- `model/repository/` - Spring Data interfaces + `AgendamentoSpecification` (dynamic filter).
- `model/service/<domain>/` - `*Service` (logic/orchestration, may call other services directly) +
  `*Validator` (`@Component`, validation rules, depends on repositories, not sibling services, to avoid
  bean cycles). `auth` has no validator.
- `model/integration/feriados/` - Feign client for BrasilAPI holidays.
- `model/exception/` - see below.
- `view/dto/<module>/` - `*CreateDTO`, `*UpdateDTO`, `*ResponseDTO`, `*Filter`; `view/dto/auth/`,
  `view/dto/exception/ErrorResponseDTO`. DTOs are records; entities/DTOs use `@Builder`.
- `view/mapper/` - 4 hand-written `*Mapper` `@Component`s (no MapStruct).
- `controller/` - thin `@RestController`s under `/api/<resource>` delegating to the service, plus
  `AuthenticationController` at `/api/auth` (`POST /register`, `POST /login`). Swagger annotations live here;
  error examples in `config/openapi/OpenApiExamples`.
- `config/` - `EndpointConfig` (base paths), `openapi/`, `security/`.

### Update semantics

`*UpdateDTO` fields are nullable (PATCH). Services apply only non-null (strings: non-blank) fields. A request
with every field null is a no-op: entity returned unchanged, no write, no revalidation.

## Exceptions -> HTTP status

All leaf domain exceptions extend `TrimlyException` directly (keeps inheritance depth at 5, the Sonar limit;
no per-module base classes). Handlers under `model/exception/handlers/` return `ErrorResponseDTO.of(status,
message)` and log the full exception (domain/auth `WARN`, unexpected `ERROR`):

- `AuthenticationExceptionHandler` (`HIGHEST_PRECEDENCE`): `AuthException` family (extends `RuntimeException`,
  not `TrimlyException`) -> 401 with a fixed message that doesn't reveal which credential failed.
- `DomainExceptionHandler`, by class: `EntityNotFoundException` -> 404; `AgendamentoConflitoException`,
  `AgendamentoFeriadoException`, `DisponibilidadeConflitoException`, `ServicoNomeDuplicadoException`,
  `UsuarioEmailExistenteException`, `ServicoComAgendamentoPendenteException`,
  `UsuarioComAgendamentoPendenteException` -> 409; every other `TrimlyException` -> 422.
- `GlobalExceptionHandler`: validation/unreadable body/bad parameter -> 400; any other `Exception` -> 500 with a
  generic message.
- New domain exception: extend `TrimlyException` (defaults to 422); add to a group only for 404/409. New auth
  exception: extend `AuthException` (401), no wiring.
- Messages are fixed strings without interpolated values; logs keep the detail.
- Invalid/expired JWTs inside `SecurityFilter` never reach a handler (filters run before the dispatcher): the
  filter swallows them and continues anonymous. Turning that into 401 is deferred with authorization.

## Security

Stateless JWT, hand-rolled (no oauth2-resource-server).

- `TokenService`: `com.auth0:java-jwt` HMAC256; subject = e-mail, `cargo` claim, issuer `trimly-auth-api`;
  `trimly.security.jwt.secret` (env `JWT_SECRET`) and `trimly.security.jwt.expiration` (seconds).
- `AuthenticationService`: `register` -> `UsuarioService.create` (CLIENTE) then token; `login` ->
  `UsuarioService.findByCredenciais` or `AuthCredenciaisInvalidasException`. No `AuthenticationManager` or
  `UserDetailsService` bean.
- `SecurityFilter` (plain class, `new`ed in `SecurityConfig`, not `@Component`): reads the `Bearer` token,
  populates `SecurityContext`. `UsuarioEntity implements UserDetails`; authority = `ROLE_` + `cargo`.
- `SecurityConfig`: CORS, CSRF off, `STATELESS`, `permitAll()` everywhere. Authorization enforcement, role
  checks and real 401/403 are the deferred next step. `CorsConfig` allows `EndpointConfig.FRONTEND_ENDPOINT`.
- The frontend bypasses JWT: `POST /api/usuarios` registers, `POST /api/usuarios/login` always answers `200` with
  `{ sucesso, usuario }` via the non-throwing `UsuarioService.findByCredenciais` (`Optional.empty()` on unknown
  e-mail or wrong password).
- `POST /api/usuarios` always creates `CLIENTE`; `ADMIN`/`DONO` are seed/DB-only. `UsuarioCargo`: `CLIENTE`,
  `ADMIN`, `DONO`.
- Seeds (every insert guarded by `WHERE NOT EXISTS`): `V2` admin `admin@trimly.com` / `admin123`; `V3` 3 `CLIENTE`
  users (password `123456`), 5 `ATIVO` serviços, disponibilidades `SEGUNDA`-`QUINTA` 07h-12h/13h-18h and
  `SEXTA` 09h-12h/13h-16h. No agendamentos, no `DONO`.

## Business rules

Preserve validation order and distinct exception types; callers depend on which one is thrown. Overlap checks
use half-open intervals (touching windows don't conflict).

### Agendamento (`AgendamentoValidator`)

End = `data` + `Servico.duracao` min. `AgendamentoService.validateHorario` runs, for both `create` and `update`:
1. `validateIsFeriado` - national holiday (BrasilAPI, `FeriadosClient.listarPorAno`) -> `AgendamentoFeriadoException`
   (409); first, so a holiday wins over the 14-day check.
2. `validateHorarioFuturo` - start not in the past (`AgendamentoNoPassadoException`), start/end on the same day
   (`AgendamentoForaDoHorarioException`).
3. `validateLimiteAntecedencia` - start at most `now + 14 days` (exactly 14 accepted) ->
   `AgendamentoAntecedenciaExcedidaException`.
4. `validateDisponibilidade` - the weekday has a `Disponibilidade` and the booking fits fully in one window ->
   `AgendamentoSemDisponibilidadeException`.
5. `validateConflitoDeHorario` - no overlap with other `AGENDADO` bookings that day ->
   `AgendamentoConflitoException`; `update` passes its own id (skipped), `create` passes `null`.

`update` takes `data`, `servicoId`, `status`. After the no-op check it first runs `validateStatusUpdate`
(`AgendamentoStatusException`): the booking must be `AGENDADO` to be modified, and a given `status` must differ
from the current one. `AGENDADO` is the only non-terminal status; it may move to `CANCELADO`, `CONCLUIDO` or
`AUSENTE`.

### Disponibilidade (`DisponibilidadeValidator`)

`create`/`update` run: `validateHorarios` (`horaInicio` before `horaFim`, else
`DisponibilidadeHorarioInvalidoException`), then `validateConflitoDeHorario` (no overlap on the same `diaSemana`,
whole-day scope since a `Disponibilidade` has no owner, else `DisponibilidadeConflitoException`; application-level
check, no DB constraint).
