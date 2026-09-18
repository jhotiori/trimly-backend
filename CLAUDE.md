# CLAUDE.md

Academic barbershop-scheduling API, Spring Boot.

Domain language: PT-BR (entities, DTOs, exceptions, validation messages). Framework/scaffolding: English.
- `Servico` - service
- `Usuario` - user
- `Agendamento` - appointment/booking
- `Disponibilidade` - availability

## Conventions

- Casing: `camelCase` (methods, vars, properties); `PascalCase` (classes, objects).
- Naming: English verb (`find`/`get`/`delete`) + language-specific spec: `findByNome`, `deleteByStatus`,
  `getByEmail`. Vars: shortest descriptive name (`nome`, `usuarioId`, `isAtivo`).
- Method order: by action then specificity - `create` > `update` > `delete` > `findAll` > `findById` >
  `findByX`; public before private. `*Mapper` classes: each direction paired, single conversion before list
  (`toEntity`, `toEntityList`, then `toResponse`, `toResponseList`). `*Validator` exempt: methods follow each
  class's own documented validation-execution order (see business-rule sections below).
- Formatting: Spotless + Eclipse JDT (4.40) owns backend style; settings in `eclipse-formatter.prefs`
  (overridden keys only, rest = Eclipse defaults). 4-space indent, 8-space continuation, 120-col wrap. Once a
  parameter/argument/annotation-argument/array list wraps: one element per line, closing `)` on its own line. A
  wrapped chain keeps its first call on the receiver's line, rest one per line. Existing line breaks are joined
  before rewrap - output independent of original formatting. Max 1 blank line; none right after a type's
  opening brace. Empty bodies: `{}`. Comments (Javadoc/block/line) left as written. Imports: Spotless
  `importOrder` (empty order = single sorted group) + `removeUnusedImports`, no wildcards. Run
  `./mvnw spotless:apply` before committing; `./mvnw verify` fails on unformatted code.
  `-DspotlessFiles=<regex>[,<regex>...]` formats only matching files (full path match).

### Javadoc

- No HTML tags (`<p>`, `<ul>`, `<li>`...). Separate paragraphs with a blank ` *` line only.
- Inline tags fine (`{@code ...}`, `{@link ...}`).
- Block tags (method/type Javadoc): `@param`, `@return`, `@throws` only. No `@author`, `@since`, `@see`,
  `@version` (field Javadoc scopes one exception for `@see`, below).
- Language: PT-BR, natural, concise, direct, clear.
- No em-dashes, no filler wording.
- Example:
```java
/**
 * Brief description of method.
 *
 * @param ParamName - ParamDesc
 * @throws ExceptionName - ExceptionThrowSituation
 * @return ReturnType - ReturnDesc
 */
```

#### Field Javadoc

- Injected collaborators (`*Service`, `*Mapper`, `*Validator`, sibling repositories, `PasswordEncoder`...):
  1-line PT-BR description + `@see {@link Type}` on next line, no blank ` *` line between.
- `@see`: allowed only in field Javadoc - the sole scoped exception to the
  `@param`/`@return`/`@throws`-only rule; never in method or type Javadoc.
- Self-explanatory fields (`String`, `Boolean`, numeric wrappers, primitives...): Javadoc, no `@see`.
- Example:
```java
public class ExampleController {
    /**
     * Mapper de Entidades utilizado para converter entre DTOs.
     * @see {@link ExampleMapper}
     */
    private final ExampleMapper mapper;
}
```

## Graphify

Knowledge graph at `graphify-out/`. For dependency/call-graph or codebase questions, use
`graphify query "<question>"` / `path "<A>" "<B>"` / `explain "<concept>"` before grep;
`graphify-out/wiki/index.md` for broad navigation. Run `graphify update .` after code changes (AST-only, no
API cost).

## Commands

```bash
./mvnw spring-boot:run                    # run API (dev profile, in-memory H2)
./mvnw compile                            # compile only
./mvnw spotless:apply                     # format all Java sources (Eclipse JDT)
./mvnw spotless:apply -DspotlessFiles='.*/Foo\.java'  # format only matching files
./mvnw spotless:check                     # check formatting (also runs in the verify phase)
```

- No tests: `src/test` was removed. Don't add test classes unless asked.
- Active profile: `spring.profiles.active` in `application.properties` (currently `develop`).
  - `develop` - H2 in-memory, H2 console at `/h2-console`, SQL logging on.
  - `production` - PostgreSQL at `localhost:5432/trimly`.
  - Flyway owns schema creation for both: migrations under `src/main/resources/db/migration`, named
    `V<n>__<description>.sql`. `ddl-auto=validate` in both profiles - Hibernate only checks entity mappings
    against the schema Flyway created, never generates/alters it. Runs through Spring Boot's stock Flyway
    autoconfiguration (`spring-boot-flyway` module, added explicitly to `pom.xml` since Boot 4 no longer pulls
    it in transitively with `flyway-core`): validate then migrate at startup;
    `entityManagerFactory` depends on the Flyway initializer, so Hibernate validation always follows
    migration. Config: `spring.flyway.*` in `application.properties` (`baseline-on-migrate=true`,
    `baseline-version=0`). `develop` H2 is in-memory - an edited migration simply reapplies on restart; a
    persisted Postgres that already ran it fails checksum validation until its schema is dropped/recreated.
- Lombok annotation processing: wired via `maven-compiler-plugin` config in `pom.xml`, not the default Lombok
  plugin binding.

## Architecture

3 tiers under `org.trimly.backend`: `model/`, `view/`, `controller/`. `model/`: one layered structure per
domain module (`servico`, `usuario`, `agendamento`, `disponibilidade`) - all share the same shape, read one to
understand the rest. `auth` = 5th, deliberately partial module: orchestration only (`AuthenticationService`,
`TokenService`), no entity/repository/validator of its own - drives `usuario`, mints JWTs.

- `model/entity/` - JPA entities (`*Entity`); enums under `model/entity/enums/`, named `<Entity><Aspect>`
  (`UsuarioCargo`, `AgendamentoStatus`, `ServicoStatus`; `DiaSemana` = calendar primitive, not entity-scoped).
- `model/repository/` - Spring Data JPA interfaces (+ `AgendamentoSpecification`).
- `model/service/<domain>/` - one folder per domain (`servico`, `usuario`, `agendamento`, `disponibilidade`,
  `auth`), each holding that domain's `*Service` + `*Validator`. Services carry business logic/orchestration,
  call each other directly (`AgendamentoService` -> `UsuarioService`, `ServicoService`,
  `DisponibilidadeService`; `AuthenticationService` -> `UsuarioService`), not via controllers. `*Validator`
  `@Component`s hold validation rules a service applies (`AgendamentoValidator`), depend on repositories not
  sibling services (avoids bean cycles). `auth` has no validator.
- `model/exception/` - `TrimlyException` (`RuntimeException`), `EntityNotFoundException`, `<module>/` domain
  exceptions, `handlers/` (below). `auth/` sits apart: `AuthException` extends `RuntimeException` directly, not
  `TrimlyException` - auth failures never fall into the domain 404/409/422 groups.
- `view/dto/<module>/` - `*CreateDTO`, `*UpdateDTO`, `*ResponseDTO`, `*Filter`, + `view/dto/exception/ErrorResponseDTO`,
  `view/dto/auth/` (`AuthLoginRequestDTO`, `AuthResponseDTO`; registration reuses `UsuarioCreateDTO`). Update
  DTOs: nullable fields for PATCH semantics; services apply only non-null/non-blank fields onto the existing
  entity (see `ServicoService.update`). All DTOs/entities: `@Builder`.
- `view/mapper/` - the 4 `*Mapper` `@Component`s, hand-convert entity <-> DTO (no MapStruct), one flat package.
- `controller/` - thin `@RestController`s under `/api/<resource>`, delegate straight to the matching service.
  All 4 domain modules have a controller, + `AuthenticationController` at `/api/auth`
  (`POST /register`, `POST /login`).
- `config/` at repo root; `config/security/` - Spring Security wiring (`SecurityConfig`, `SecurityFilter`,
  `CorsConfig`, `PasswordConfig`).

## Exception -> HTTP status

3 `@RestControllerAdvice` classes under `model/exception/handlers/`, all return `ErrorResponseDTO` (`status`,
`error`, `message`), log the full exception server-side (domain/auth: `WARN`, unexpected: `ERROR`):

- `AuthenticationExceptionHandler` (`@Order(HIGHEST_PRECEDENCE)`): `AuthException` family -> 401, fixed
  message (doesn't reveal whether it was the e-mail or the password).
- `DomainExceptionHandler`: `TrimlyException` family by class, grouped `@ExceptionHandler` methods (no status
  field on the exceptions, no class->status map):
  - `EntityNotFoundException` -> 404
  - `AgendamentoConflitoException`, `AgendamentoFeriadoException`, `DisponibilidadeConflitoException`,
    `ServicoNomeDuplicadoException`, `UsuarioEmailExistenteException`, `ServicoComAgendamentoPendenteException`,
    `UsuarioComAgendamentoPendenteException` -> 409
  - every other `TrimlyException` -> 422
- `GlobalExceptionHandler` - fallback: any non-domain `Exception` -> 500, fixed generic message.
- A new domain exception defaults to 422; add to a group only to return 404/409. A new auth exception extends
  `AuthException` -> 401, no further wiring.
- Invalid/expired JWTs thrown inside `SecurityFilter` never reach a handler (filters run before the
  `DispatcherServlet`) - the filter currently swallows them, continues anonymous. Routing filter failures to a
  401 is deferred with the rest of authorization enforcement.
- Domain-exception messages carry no interpolated values (id/e-mail/name/enum) - fixed strings. Server logs
  keep the full detail.

## Security

Stateless JWT, hand-rolled (no `spring-security-oauth2-resource-server` - starter on classpath but unused;
manual filter approach chosen instead).

- `TokenService` (`model/service/auth/`): mints/verifies tokens with `com.auth0:java-jwt` (HMAC256). Subject =
  e-mail + `cargo` claim; issuer `trimly-auth-api`; expiry from `trimly.security.jwt.expiration` (seconds).
  Secret: `trimly.security.jwt.secret` (env `JWT_SECRET`, dev fallback).
- `AuthenticationService` (`model/service/auth/`): `register` -> `UsuarioService.create` (e-mail uniqueness,
  BCrypt, `cargo = CLIENTE`) then mints token; `login` loads by e-mail, checks password via
  `PasswordEncoder.matches` directly - no `AuthenticationManager`, no `UserDetailsService` bean.
- `SecurityFilter` (`config/security/`, plain class, `new`ed in `SecurityConfig` - not `@Component`, avoids
  double registration): reads the `Bearer` token, resolves `UsuarioEntity`, populates `SecurityContext`.
  `UsuarioEntity implements UserDetails` for now; `getAuthorities()` = `cargo` name prefixed `ROLE_`.
- `SecurityConfig`: CSRF disabled, `STATELESS`, still `permitAll()` on every request - authorization not
  enforced yet. Locking it down, role checks, and turning filter/login failures into real 401/403: deferred
  next step.
- `PasswordConfig` - BCrypt `PasswordEncoder` bean. `CorsConfig` - allows the frontend origin
  (`EndpointConfig.FRONTEND_ENDPOINT`).
- Frontend doesn't use the JWT stack: registers via `POST /api/usuarios`, signs in via
  `POST /api/usuarios/login` - always `200` with `{ sucesso, usuario }`, never `401` (backed by the
  non-throwing `UsuarioService.findByCredenciais`: unknown e-mail or wrong password = `Optional.empty()`, no
  `@RestControllerAdvice` runs). Password comparison stays in the service; controller only branches on the
  `Optional`.
- First `ADMIN` seeded by Flyway (`V2__seed_admin_user.sql`, `admin@trimly.com` / `admin123`).
  `POST /api/usuarios` always creates `CLIENTE` - `ADMIN`/`DONO` stay seed-/DB-only until an admin-gated
  endpoint exists.
- `V3__seed_base_entities.sql`: local dataset - 3 `CLIENTE` users (password `123456`), 5 `ATIVO` serviços,
  disponibilidades `SEGUNDA`-`QUINTA` 07h-12h/13h-18h + `SEXTA` 09h-12h/13h-16h. No agendamentos, no `DONO`.
  Every insert guarded by `WHERE NOT EXISTS`.
- `UsuarioCargo`: `CLIENTE`, `ADMIN`, `DONO`.

## Agendamento (booking) business rules

Rules in `AgendamentoValidator`. Preserve validation order + distinct exception types - callers depend on
which one is thrown.

End time = `data` + `Servico.duracao` min (`calculateFimAgendamento`).

`create`/`update` both run, in order:
1. `validateIsFeriado` - start date must not match a national holiday (BrasilAPI,
   `FeriadosClient.listarPorAno`, 1 call/request), else `AgendamentoFeriadoException` (409). Runs before every
   other check, so a holiday is reported even when the date is also past the 14-day window.
2. `validateHorarioFuturo` - start/end must fall on the same calendar day, else
   `AgendamentoForaDoHorarioException`.
3. `validateLimiteAntecedencia` - start must not be after `LocalDateTime.now().plusDays(14)` (exactly 14 days
   ahead = accepted), else `AgendamentoAntecedenciaExcedidaException` (422).
4. `validateDisponibilidade` - day of week must have a `Disponibilidade`, booking must fit fully inside one of
   its windows, else `AgendamentoSemDisponibilidadeException`.
5. `validateConflitoDeHorario` - no interval overlap with other `AGENDADO` bookings same day, else
   `AgendamentoConflitoException`. `update`: booking's own id passed (skipped); `create`: `null`.

`update` applies `data`, `servicoId`, `status` (`AgendamentoUpdateDTO`). All-null request = no-op: unchanged
booking returned `200`, no write, no re-validation. Otherwise first runs `validateStatusUpdate(entity, dto.status)`
(throws `AgendamentoStatusException`): booking must be `AGENDADO` to be modified at all, and if `status` is
present it may not equal the current status. `AGENDADO` = only non-terminal status; from it, `status` may move
to `CANCELADO`, `CONCLUIDO`, or `AUSENTE`, all terminal.

## Disponibilidade (availability) business rules

Rules in `DisponibilidadeValidator`. `create`/`update` both run, in order:
1. `validateHorarios` - `horaInicio` must be before `horaFim`, else `DisponibilidadeHorarioInvalidoException`.
2. `validateConflitoDeHorario` - no overlap with another `Disponibilidade` on the same `diaSemana`, else
   `DisponibilidadeConflitoException` (409). Same half-open interval check as Agendamento, so touching windows
   (`08:00-12:00`/`12:00-13:00`) don't conflict. `update`: entity's own id passed (skipped); `create`: `null`.
   Scope = whole day, since a `Disponibilidade` has no owner - an application-level check, no DB constraint
   behind it.

An `update` request with every field null = no-op: no write, no re-validation.
