# CLAUDE.md

Context file. Holds only what Claude needs to work here: architecture, conventions, patterns, business rules.
No changelogs, task notes, tutorials, or anything derivable from the code. State each fact once; edit in place.

## Context

Domain files under `.claude/rules/` load when a matching path is touched; read one directly when a task needs it.

- [**Conventions**](.claude/rules/conventions.md): naming, method order, formatting, Javadoc.
- [**Configuration**](.claude/rules/configuration.md): env, profiles, banners, Flyway and seeds, tests, Sonar.
- [**Errors**](.claude/rules/errors.md): exception -> HTTP mapping, messages, log levels.
- [**Security**](.claude/rules/security.md): JWT, filter chain, authorization matrix.
- [**Business Rules**](.claude/rules/business-rules.md): update semantics, Agendamento and Disponibilidade validation.

## Project

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

## CodeGraph

- When `.codegraph/` exists, query the graph before `Grep`, `Glob` or `Read`: the `codegraph_explore` MCP tool, or
  `codegraph explore "<symbols or question>"` in the shell. Name the symbols in the query; before changing a shared
  signature, read its blast radius in the same result.
- Without `.codegraph/`, skip CodeGraph (indexing is the user's call).
- Fallback to `Grep`/`Read` only for non-indexed files (configs, SQL, Markdown, logs) or exact regex matches.

## Commands

```bash
./mvnw spring-boot:run                    # run API (developing profile, in-memory H2)
./mvnw compile
./mvnw spotless:apply                     # format; -DspotlessFiles='.*/Foo\.java' limits to matching files
./mvnw verify                             # tests + spotless check
docker compose --env-file .env -f docker/compose.yaml up -d                      # postgres + api
docker compose --env-file .env -f docker/compose.yaml --profile sonar up -d sonarqube   # http://localhost:9000
./mvnw test sonar:sonar -Dsonar.token=<token> -Dsonar.host.url=http://localhost:9000
```

## Architecture

3 tiers under `org.trimly.backend`: `model/`, `view/`, `controller/`, plus `infra/`. `model/` has one identical
layered structure per domain (`servico`, `usuario`, `agendamento`, `disponibilidade`); read one to understand the rest.
`auth` is a partial module (orchestration only: `AuthenticationService`, `TokenService`), driving `usuario`.

- `model/entity/<domain>/` - JPA `*Entity` + its enums (`UsuarioCargo`, `AgendamentoStatus`, `ServicoStatus`,
  `DisponibilidadeDiaSemana`).
- `model/repository/` - Spring Data interfaces + `AgendamentoSpecification` (dynamic filter).
- `model/service/<domain>/` - `*Service` (logic/orchestration, may call other services directly) +
  `*Validator` (`@Component`, validation rules, depends on repositories, not sibling services, to avoid
  bean cycles). `auth` has no validator.
- `model/integration/feriados/` - Feign client for BrasilAPI holidays.
- `model/exception/` - one class per error plus handlers, see [errors](.claude/rules/errors.md).
- `view/dto/<module>/` - `*CreateDTO`, `*UpdateDTO`, `*ResponseDTO`, `*Filter`; `view/dto/auth/`,
  `view/dto/exception/ErrorResponseDTO`. DTOs are records; entities/DTOs use `@Builder`.
- `view/mapper/` - 4 hand-written `*Mapper` `@Component`s (no MapStruct).
- `controller/` - thin `@RestController`s under `/api/<resource>` delegating to the service, plus
  `AuthenticationController` at `/api/auth` (`POST /register`, `POST /login`). Swagger annotations live here.
- `infra/` - cross-cutting beans and property records: `EndpointConfig` (base paths), `openapi/`, and `security/`
  (see [security](.claude/rules/security.md)).
