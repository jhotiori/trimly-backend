# CLAUDE.md

Trimly backend is an academic barbershop-scheduling API, built with Spring Boot.

Backend domain language is Portuguese (entities, DTOs, exceptions, validation
messages); framework/technical scaffolding stays English.
- `Servico` - service
- `Usuario` - user
- `Agendamento` - appointment/booking
- `Disponibilidade` - availability

## Conventions

- Casing: `camelCase` for methods, variables, properties; `PascalCase` for class
  names and objects.
- Naming: English verb (`find`, `get`, `delete`) + language-specific spec:
  `findByNome`, `deleteByStatus`, `getByEmail`. Variables: shortest name that stays
  descriptive (`nome`, `usuarioId`, `isAtivo`).
- Method ordering: by action then specificity (`create > update > findAll > findById > findByX > deleteById`); public methods first, private last.
- Formatting: Spotless with palantir-java-format owns backend style - 4-space indent,
  120-col wrap, single sorted import group, no wildcard imports (Javadoc left as
  written). Run `./mvnw spotless:apply` before committing; `./mvnw verify` fails on
  unformatted code.

### Javadoc

- No HTML tags (`<p>`, `<ul>`, `<li>`, ...). Separate paragraphs with a blank ` *`
  line only.
- Javadoc inline tags (`{@code ...}`, `{@link ...}`) are fine.
- Block tags in method and type Javadoc: only `@param`, `@return`, `@throws`. No
  `@author`, `@since`, `@see`, `@version`, etc. (field Javadoc scopes one exception
  for `@see`, see below).
- Language: PT-BR, in a natural, concise, direct na clear vocabulary.
- No em-dashes or filler wording.
- Example javadoc:
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

- Injected collaborators (`*Service`, `*Mapper`, `*Validator`, sibling repositories,
  `PasswordEncoder`, ...) carry a one-line PT-BR description followed by
  `@see {@link Type}` on the next line, with no blank ` *` line between them.
- `@see` is allowed only in field Javadoc: it is the single scoped exception to the
  "only `@param`, `@return`, `@throws`" block-tag rule above, and never appears in
  method or type Javadoc.
- Self-explanatory fields (`String`, `Boolean`, numeric wrappers, primitives and
  similar) get Javadoc, but no `@see` annotation.
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

Knowledge graph at `graphify-out/`. For dependency/call-graph or codebase questions,
use `graphify query "<question>"` / `path "<A>" "<B>"` / `explain "<concept>"` before
grep; `graphify-out/wiki/index.md` for broad navigation. Run `graphify update .` after
code changes (AST-only, no API cost).

## Commands

```bash
./mvnw spring-boot:run                    # run API (dev profile, in-memory H2)
./mvnw test                               # all tests
./mvnw test -Dtest=ClassName              # one test class
./mvnw test -Dtest=ClassName#methodName   # one test method
./mvnw compile                            # compile only
./mvnw spotless:apply                     # format all Java sources (palantir-java-format)
./mvnw spotless:check                     # check formatting (also runs in the verify phase)
```

- Tests are paused: `maven.test.skip=true` in `pom.xml` skips test compilation and
  execution while the auth rework lands (the test sources still target the old package
  layout). Re-enable per command with `./mvnw -Dmaven.test.skip=false test`, or delete the
  property once the suite is fixed.

- Active profile: `spring.profiles.active` in `application.properties` (currently `develop`).
  - `develop` - H2 in-memory, H2 console at `/h2-console`, SQL logging on.
  - `production` - PostgreSQL at `localhost:5432/trimly`.
  - Flyway owns schema creation for both profiles: migrations live under
    `src/main/resources/db/migration`, named `V<n>__<description>.sql`. `ddl-auto=validate`
    in both profiles, so Hibernate only checks its entity mappings against the schema
    Flyway created - it never generates or alters schema itself. Migrations run through
    Spring Boot's stock Flyway autoconfiguration (the `spring-boot-flyway` module, added
    explicitly to `pom.xml` since Boot 4 no longer pulls it in transitively with
    `flyway-core`): it runs validate then migrate at startup and makes
    `entityManagerFactory` depend on the Flyway initializer, so Hibernate validation
    always follows migration. Config is `spring.flyway.*` in `application.properties`
    (`baseline-on-migrate=true`, `baseline-version=0`).
- Lombok annotation processing is wired via the `maven-compiler-plugin` config in
  `pom.xml`, not the default Lombok plugin binding.

## Architecture

Code is grouped into three tiers under `org.trimly.backend`: `model/`, `view/`, and
`controller/`. Within `model/` there is one layered structure per domain module
(`servico`, `usuario`, `agendamento`, `disponibilidade`); all four share the same
shape, so read one to understand the rest. `auth` is a fifth, deliberately partial
module: it is orchestration only (`AuthenticationService`, `TokenService`), with no
entity, repository, or validator of its own - it drives `usuario` and mints JWTs.

- `model/entity/` - JPA entities (`*Entity`); enums under `model/entity/enums/`, named
  `<Entity><Aspect>` (`UsuarioCargo`, `AgendamentoStatus`, `ServicoStatus`; `DiaSemana`
  is a calendar primitive, not entity-scoped).
- `model/repository/` - Spring Data JPA interfaces (plus `AgendamentoSpecification`).
- `model/service/<domain>/` - one folder per domain (`servico`, `usuario`, `agendamento`,
  `disponibilidade`, plus `auth`), each holding that domain's `*Service` and its
  `*Validator`. Services carry the business logic and orchestration and call each other
  directly (e.g. `AgendamentoService` -> `UsuarioService`, `ServicoService`,
  `DisponibilidadeService`; `AuthenticationService` -> `UsuarioService`), not via
  controllers. `*Validator` `@Component`s hold the validation rules a service applies
  (e.g. `AgendamentoValidator`) and depend on repositories, not on sibling services, to
  avoid bean cycles. `auth` has no validator.
- `model/exception/` - `TrimlyException` (a `RuntimeException`), `EntityNotFoundException`,
  the `<module>/` domain exceptions, and `handlers/` (see below). `auth/` sits apart:
  `AuthException` extends `RuntimeException` directly, not `TrimlyException`, so auth
  failures never fall into the domain 404/409/422 groups.
- `view/dto/<module>/` - `*CreateDTO`, `*UpdateDTO`, `*ResponseDTO`, `*Filter`, plus
  `view/dto/exception/ErrorResponseDTO` and `view/dto/auth/` (`AuthLoginRequestDTO`,
  `AuthResponseDTO`; registration reuses `UsuarioCreateDTO`). Update DTOs use nullable
  fields for PATCH semantics; services apply only non-null/non-blank fields onto the
  existing entity (see `ServicoService.update`). All DTOs and entities carry `@Builder`.
- `view/mapper/` - the four `*Mapper` `@Component`s that hand-convert entity <-> DTO (no
  MapStruct), in one flat package.
- `controller/` - thin `@RestController`s under `/api/<resource>`, delegating straight
  to the matching service. All four domain modules have a controller, plus
  `AuthenticationController` at `/api/auth` (`POST /register`, `POST /login`).
- `config/` stays at the repository root; `config/security/` holds the Spring Security
  wiring (`SecurityConfig`, `SecurityFilter`, `CorsConfig`, `PasswordConfig`).

## Exception to HTTP status

Three `@RestControllerAdvice` classes under `model/exception/handlers/`, all returning an
`ErrorResponseDTO` (`status`, `error`, `message`) and logging the full exception
server-side (domain and auth at `WARN`, unexpected at `ERROR`):

- `AuthenticationExceptionHandler` (`@Order(HIGHEST_PRECEDENCE)`) maps the `AuthException`
  family -> 401 with a fixed message that does not say whether it was the e-mail or the
  password.
- `DomainExceptionHandler` maps the `TrimlyException` family by class, via grouped
  `@ExceptionHandler` methods (no status field on the exceptions, no class->status map):
  - `EntityNotFoundException` -> 404
  - `AgendamentoConflitoException`, `ServicoNomeDuplicadoException`,
    `UsuarioEmailExistenteException` -> 409
  - every other `TrimlyException` -> 422
- `GlobalExceptionHandler` is the fallback: any non-domain `Exception` -> 500 with a
  fixed generic message.
- A new domain exception inherits 422 by default; it needs to be added to a group only to
  return 404 or 409. A new auth exception extends `AuthException` and is 401 with no
  further wiring.
- Invalid/expired JWTs thrown inside `SecurityFilter` do not reach any handler (filters
  run before the `DispatcherServlet`); the filter currently swallows them and continues
  anonymous. Routing filter failures to a 401 response is deferred with the rest of
  authorization enforcement.
- Thrown domain-exception messages carry no interpolated values (no id, e-mail, name, or
  enum value); they are fixed strings. Server logs keep the full original detail.

## Security

Stateless JWT, hand-rolled (no `spring-security-oauth2-resource-server` - the starter is
on the classpath but unused; the manual filter approach was chosen instead).

- `TokenService` (`model/service/auth/`) mints and verifies tokens with `com.auth0:java-jwt`
  (HMAC256). Subject = e-mail, plus a `cargo` claim; issuer `trimly-auth-api`; expiry from
  `trimly.security.jwt.expiration` seconds. Secret from `trimly.security.jwt.secret`
  (env `JWT_SECRET`, with a dev fallback).
- `AuthenticationService` (`model/service/auth/`): `register` delegates to
  `UsuarioService.create` (keeps e-mail uniqueness, BCrypt, `cargo = CLIENTE`) then mints a
  token; `login` loads by e-mail and checks the password with `PasswordEncoder.matches`
  directly - no `AuthenticationManager`, no `UserDetailsService` bean.
- `SecurityFilter` (`config/security/`, plain class, `new`ed in `SecurityConfig` - not a
  `@Component`, to avoid double registration) reads the `Bearer` token, resolves the
  `UsuarioEntity`, and populates the `SecurityContext`. `UsuarioEntity implements
  UserDetails` for now; `getAuthorities()` returns the `cargo` name prefixed with `ROLE_`.
- `SecurityConfig` disables CSRF, is `STATELESS`, and still `permitAll()` on every
  request - authorization is not enforced yet. Locking it down, role checks, and turning
  filter/login failures into real 401/403 responses are the deferred next step.
- `PasswordConfig` - the BCrypt `PasswordEncoder` bean. `CorsConfig` - allows the
  frontend origin (`EndpointConfig.FRONTEND_ENDPOINT`).
- The frontend does not use the JWT stack at all. It registers through
  `POST /api/usuarios` and signs in through `POST /api/usuarios/login`, which always answers
  `200` with `{ sucesso, usuario }` - never `401` - backed by the non-throwing
  `UsuarioService.findByCredenciais` (an unknown e-mail or a wrong password is
  `Optional.empty()`, so no `@RestControllerAdvice` runs for a login mismatch). The password
  comparison stays in the service; the controller only branches on the `Optional`.
- First `ADMIN` is seeded by Flyway (`V2__seed_admin_user.sql`, `admin@trimly.com` /
  `ADMIN0000`). `POST /api/usuarios` always creates a `CLIENTE`, so `ADMIN`/`DONO` accounts
  stay seed- or DB-only until an admin-gated endpoint exists.
- `UsuarioCargo`: `CLIENTE`, `ADMIN`, `DONO`.

## Agendamento (booking) business rules

Rules live in `AgendamentoValidator`. Preserve the validation order and the distinct
exception types - callers/tests depend on which one is thrown.

End time = `data` + `Servico.duracao` minutes (`calculateFimAgendamento`).

`create` and `update` both run, in order:
1. `validateHorarioFuturo` - start and end must fall on the same calendar day, else
   `AgendamentoForaDoHorarioException`.
2. `validateDisponibilidade` - the day of week must have a `Disponibilidade`, and the
   booking must fit fully inside one of its windows, else
   `AgendamentoSemDisponibilidadeException`.
3. `validateConflitoDeHorario` - no interval overlap with other `AGENDADO` bookings the
   same day, else `AgendamentoConflitoException`. On `update` the booking's own id is
   passed so it is skipped; on `create` it is `null`.

`update` applies `data`, `servicoId`, and `status` (from `AgendamentoUpdateDTO`). An
update request with every field null is a no-op: the unchanged booking is returned with
`200`, no write, no re-validation. Otherwise `update` first runs
`validateStatusUpdate(entity, dto.status)` (throws `AgendamentoStatusException`): the
booking must be in `AGENDADO` to be modified at all, and when `status` is present it may
not equal the current status. `AGENDADO` is the only non-terminal status; from it
`status` may move to `CANCELADO`, `CONCLUIDO`, or `AUSENTE`, all terminal.
