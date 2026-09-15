---
name: swagger-openapi-integration
author: jhotiori
date: 2026-09-15
---

# TASK
Integrate Swagger (OpenAPI) into the Trimly backend via springdoc-openapi, exposing Swagger UI for manual HTTP testing, and document (in PT-BR, inside the Swagger annotations themselves) the 5 controllers in `controller/` and the DTOs in `view/dto/`. Do not document services or entities. Do not wire JWT/bearer authentication into Swagger UI (security is `permitAll()` everywhere today).

# GOAL
Every HTTP route in the backend must be explorable and manually testable through Swagger UI, with summary, description, payload examples and every possible status/error documented in Portuguese (the Swagger-facing text), without changing the behavior of any endpoint.

# PLAN
1. Add the `springdoc-openapi-starter-webmvc-ui` dependency and confirm Swagger UI/API docs come up with no extra security configuration.
2. Configure minimal API metadata (title/description/version) in PT-BR.
3. Annotate every public method of the 5 controllers with `@Operation` and one `@ApiResponse` per possible status (success + each exception in the flow), with `ErrorResponseDTO` examples.
4. Annotate the fields of every DTO (`Create`/`Update`/`Response`/`Filter`, `ErrorResponseDTO`, `auth` DTOs) with `@Schema(description, example)`.
5. Verify the build, formatting, and manual usage of Swagger UI before considering this done.

# SPECS

## SPEC-001 - springdoc-openapi dependency

### Goal
- Expose Swagger UI and the OpenAPI JSON/YAML without requiring any extra security configuration.

### Problem
- SITUATION: the backend (Spring Boot 4.1.0, `spring-boot-starter-webmvc`) has no OpenAPI/Swagger dependency at all.
- IMPACT: HTTP testing depends on external tools (curl, Postman); no interactive UI reflects the current controllers.

### Plan
- Add to `backend/pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.1</version>
</dependency>
```
- This is the version compatible with Spring Boot 4 / Spring Framework 7 (verified against springdoc's official documentation). Do not add `springdoc-openapi-starter-webflux-ui`: this application is WebMVC, not reactive.
- Do not edit `SecurityConfig`: it is currently `STATELESS`, with CSRF disabled and `permitAll()` on every route, so springdoc's default paths (`/v3/api-docs/**`, `/swagger-ui/**`) already pass through with no security configuration change.

### Scenario
- GIVEN the dependency added to `pom.xml`
- WHEN `./mvnw spring-boot:run` (`develop` profile, in-memory H2)
- THEN the application starts with no errors
- AND `GET /swagger-ui.html` redirects to `/swagger-ui/index.html` and renders the UI
- AND `GET /v3/api-docs` returns valid OpenAPI JSON
- AND `GET /v3/api-docs.yaml` returns the equivalent YAML
- AND none of these routes require an authentication header

### Acceptance
- MUST: `pom.xml` contains exactly the dependency above (groupId/artifactId/version), with no exclusions or duplicate springdoc artifacts.
- MUST: Swagger UI and `/v3/api-docs` are reachable right after the build, with no change to `SecurityConfig`.
- MUST NOT: add `springdoc-openapi-starter-webflux-ui` or any reactive variant.
- MUST NOT: change `permitAll()`/CSRF/stateless in `SecurityConfig` as part of this task.

### Tasks
- [ ] Add the dependency to `backend/pom.xml`.
- [ ] Run `./mvnw spring-boot:run` and confirm `/swagger-ui.html` and `/v3/api-docs`.

### Constraints
- DO: keep the dependency version pinned at `3.1.1`.
- DO NOT: touch `config/security/SecurityConfig` or any other file under `config/security/`.

## SPEC-002 - API metadata

### Goal
- API title, description and version in PT-BR, visible in Swagger UI and in `/v3/api-docs`.

### Plan
- Create a minimal `@Configuration` (e.g. `config/OpenApiConfig`) with an `@Bean OpenAPI` defining `Info` (title, description, version), OR the equivalent via `springdoc.*`/`info.*` properties in `application.properties`, whichever is simpler to maintain.
- No groups (`GroupedOpenApi`), no `SecurityScheme`/`@SecurityRequirement` (Swagger authentication is out of scope, see the general CONSTRAINTS section).
- Class/bean Javadoc follows the project's rules (PT-BR, no HTML tags, only `@param`/`@return`/`@throws`/`@see` per field-Javadoc scope).

### Acceptance
- MUST: `info.title`, `info.description` and `info.version` are in PT-BR in the generated OpenAPI document.
- MUST NOT: declare a bearer/JWT `SecurityScheme` or any global `@SecurityRequirement`.
- MUST NOT: add `GroupedOpenApi` or tags beyond the 5 already used by the controllers (see SPEC-003 to SPEC-007), unless demonstrably necessary to organize the UI.

### Tasks
- [ ] Define minimal API metadata (title/description/version) in PT-BR.

### Constraints
- DO NOT: configure any security scheme in the OpenAPI document (deferred, see the CONSTRAINTS section).

## SPEC-003 - AgendamentoController documentation

### Goal
- Every endpoint of `AgendamentoController` (`/api/agendamentos`) documented in Swagger with summary, description, and every status/error it can produce.

### Problem
- SITUATION: `AgendamentoController` has no `@Tag`/`@Operation`/`@ApiResponse`; the rules in `AgendamentoValidator` (validation order, distinct exceptions) are not reflected anywhere in Swagger.
- IMPACT: a manual tester cannot tell, without reading `AgendamentoValidator`, when to expect 404, 409 or 422 on each endpoint.

### Plan
- `@Tag(name = "Agendamentos", description = "Gestão de agendamentos da barbearia")` on the class.
- `@Operation(summary, description)` in PT-BR on every method.
- One `@ApiResponse` per possible status, with `@Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = @ExampleObject(...))` on errors and `AgendamentoResponseDTO`/`List<AgendamentoResponseDTO>` on success.
- Existing Javadoc stays as-is; Swagger annotations coexist with it on the same method.

### Scenario
- SCEN-001 `POST /api/agendamentos` (create)
  - GIVEN a valid `AgendamentoCreateDTO` (`data`, `horario`, `usuarioId`, `servicoId`)
  - WHEN the request is sent
  - THEN 201 with `AgendamentoResponseDTO`
  - AND 400 if bean validation (`@Valid`) fails (Spring's default payload shape, not `ErrorResponseDTO` - see SPEC-009)
  - AND 404 (`EntityNotFoundException`) if `usuarioId` or `servicoId` do not exist
  - AND 422 (`AgendamentoNoPassadoException`) if `data`+`horario` are already in the past
  - AND 422 (`AgendamentoForaDoHorarioException`) if the booking crosses midnight
  - AND 422 (`AgendamentoSemDisponibilidadeException`) if no `Disponibilidade` covers the time slot
  - AND 409 (`AgendamentoConflitoException`) if it overlaps another `AGENDADO` booking
  - AND 500 for any unmapped exception
- SCEN-002 `PATCH /api/agendamentos/{id}` (update)
  - GIVEN an `AgendamentoUpdateDTO` with optional fields (`data`, `status`, `servicoId`); all null is a no-op
  - WHEN the request is sent
  - THEN 200 with `AgendamentoResponseDTO` (no write if every field is null)
  - AND 400 on `@Valid` failure
  - AND 404 (`EntityNotFoundException`) if `id` or `servicoId` do not exist
  - AND 422 (`AgendamentoStatusException`) if the booking is not `AGENDADO`, or if `status` repeats the current status
  - AND 422/409 under the same scheduling/availability/conflict rules as SCEN-001 when `data` changes
  - AND 500 for any unmapped exception
- SCEN-003 `GET /api/agendamentos` (findAll)
  - GIVEN optional query params `status`, `data`, `usuarioId`, `servicoId`
  - WHEN the request is sent
  - THEN 200 with a list of `AgendamentoResponseDTO` (empty if nothing matches)
  - AND 400 if `status` is not a valid `AgendamentoStatus` enum value (Spring's default conversion error)
  - AND 500 for any unmapped exception
- SCEN-004 `GET /api/agendamentos/{id}` (findById)
  - GIVEN a path `id`
  - WHEN the request is sent
  - THEN 200 with `AgendamentoResponseDTO`
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 500 for any unmapped exception
- SCEN-005 `DELETE /api/agendamentos/{id}` (deleteById)
  - GIVEN a path `id`
  - WHEN the request is sent
  - THEN 204 with no body
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 500 for any unmapped exception

### Expected
- EXPECTED: all 5 endpoints have `@Operation` and one `@ApiResponse` per status from SCEN-001 to SCEN-005, with an `ErrorResponseDTO` example on each error.
- NOT EXPECTED: any change to the signature, path, HTTP verb or behavior of any method; no `@SecurityRequirement` on any endpoint.

### Acceptance
- MUST: every error `@ApiResponse` references `ErrorResponseDTO` with a `status`/`error`/`message` example consistent with the exception.
- MUST: `AgendamentoCreateDTO`/`AgendamentoUpdateDTO` examples use future dates and times compatible with the seeded availabilities (`SEGUNDA`-`QUINTA` 07:00-12:00/13:00-18:00, `SEXTA` 09:00-12:00/13:00-16:00).
- MUST NOT: add any new validation, business rule, or log statement.

### Tasks
- [ ] `@Tag` on the `AgendamentoController` class.
- [ ] `@Operation` + `@ApiResponse`s on `create`.
- [ ] `@Operation` + `@ApiResponse`s on `update`.
- [ ] `@Operation` + `@ApiResponse`s on `findAll` (document the 4 optional `@RequestParam`s).
- [ ] `@Operation` + `@ApiResponse`s on `findById`.
- [ ] `@Operation` + `@ApiResponse`s on `deleteById`.

### Constraints
- DO: reuse `ErrorResponseDTO` as the `schema` of every error `@ApiResponse`.
- DO NOT: document `AgendamentoService`, `AgendamentoValidator`, or `AgendamentoEntity`.

## SPEC-004 - DisponibilidadeController documentation

### Goal
- Every endpoint of `DisponibilidadeController` (`/api/disponibilidades`) documented in Swagger with summary, description, and every status/error it can produce.

### Problem
- SITUATION: `DisponibilidadeController` has no OpenAPI annotations; `findByDiaSemana` uses `DiaSemana.fromString`, which throws `IllegalArgumentException` (not a `TrimlyException`) for an invalid day.
- IMPACT: without documenting this detail, a manual tester would expect 400/422 for an invalid `diaSemana`, but the current behavior falls through to `GlobalExceptionHandler` (500).

### Plan
- `@Tag(name = "Disponibilidades", description = "Gestão das janelas de atendimento da barbearia")` on the class.
- `@Operation` on every method; `@ApiResponse` per status, including the 500 case in `findByDiaSemana` for an invalid value (document the current behavior, do not change it).

### Scenario
- SCEN-001 `POST /api/disponibilidades` (create)
  - GIVEN a valid `DisponibilidadeCreateDTO` (`diaSemana`, `horaInicio`, `horaFim`)
  - WHEN the request is sent
  - THEN 201 with `DisponibilidadeResponseDTO`
  - AND 400 on `@Valid` failure
  - AND 422 (`DisponibilidadeHorarioInvalidoException`) if `horaInicio` is not before `horaFim`
  - AND 409 (`DisponibilidadeConflitoException`) if it overlaps another window on the same `diaSemana`
  - AND 500 for any unmapped exception
- SCEN-002 `PATCH /api/disponibilidades/{id}` (update)
  - GIVEN a `DisponibilidadeUpdateDTO` with optional fields; all null is a no-op
  - WHEN the request is sent
  - THEN 200 with `DisponibilidadeResponseDTO`
  - AND 400 on `@Valid` failure
  - AND 404 (`EntityNotFoundException`) if `id` does not exist
  - AND 422/409 under the same rules as SCEN-001 when any field changes
  - AND 500 for any unmapped exception
- SCEN-003 `GET /api/disponibilidades` (findAll)
  - WHEN the request is sent
  - THEN 200 with a list of `DisponibilidadeResponseDTO`
  - AND 500 for any unmapped exception
- SCEN-004 `GET /api/disponibilidades/{id}` (findById)
  - GIVEN a path `id`
  - THEN 200 with `DisponibilidadeResponseDTO`
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 500 for any unmapped exception
- SCEN-005 `GET /api/disponibilidades/dia/{diaSemana}` (findByDiaSemana)
  - GIVEN a path `diaSemana` (`SEGUNDA`...`DOMINGO`, case-insensitive)
  - THEN 200 with a list of `DisponibilidadeResponseDTO`
  - AND 500 if `diaSemana` does not match any valid day (`IllegalArgumentException` from `DiaSemana.fromString`, currently with no dedicated handler - document as-is, do not change the behavior)
- SCEN-006 `DELETE /api/disponibilidades/{id}` (deleteById)
  - GIVEN a path `id`
  - THEN 204 with no body
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 500 for any unmapped exception

### Expected
- EXPECTED: all 6 endpoints have `@Operation` and `@ApiResponse` per status from SCEN-001 to SCEN-006; the `findByDiaSemana` description explicitly states that an invalid `diaSemana` currently returns 500, not 400.
- NOT EXPECTED: fixing `findByDiaSemana`'s behavior for an invalid day (out of scope for this task); any behavior change on any endpoint.

### Acceptance
- MUST: `diaSemana`/`horaInicio`/`horaFim` examples consistent with the seeded dataset (`SEGUNDA`-`QUINTA` 07:00-12:00/13:00-18:00, `SEXTA` 09:00-12:00/13:00-16:00).
- MUST NOT: add new error handling for `DiaSemana.fromString`.

### Tasks
- [ ] `@Tag` on the `DisponibilidadeController` class.
- [ ] `@Operation` + `@ApiResponse`s on `create`.
- [ ] `@Operation` + `@ApiResponse`s on `update`.
- [ ] `@Operation` + `@ApiResponse`s on `findAll`.
- [ ] `@Operation` + `@ApiResponse`s on `findById`.
- [ ] `@Operation` + `@ApiResponse`s on `findByDiaSemana` (document the invalid-day 500).
- [ ] `@Operation` + `@ApiResponse`s on `deleteById`.

### Constraints
- DO: document `findByDiaSemana`'s real behavior, even though it is not ideal (500 instead of 400).
- DO NOT: change `DiaSemana.fromString` or add a handler for `IllegalArgumentException`.

## SPEC-005 - ServicoController documentation

### Goal
- Every endpoint of `ServicoController` (`/api/servicos`) documented in Swagger with summary, description, and every status/error it can produce.

### Plan
- `@Tag(name = "Serviços", description = "Gestão dos serviços oferecidos pela barbearia")` on the class.
- `@Operation` on every method; `@ApiResponse` per status.

### Scenario
- SCEN-001 `POST /api/servicos` (create)
  - GIVEN a valid `ServicoCreateDTO` (`nome`, `valor`, `duracao`)
  - THEN 201 with `ServicoResponseDTO`
  - AND 400 on `@Valid` failure
  - AND 409 (`ServicoNomeDuplicadoException`) if `nome` already exists
  - AND 500 for any unmapped exception
- SCEN-002 `PATCH /api/servicos/{id}` (update)
  - GIVEN a `ServicoUpdateDTO` with optional fields (`nome`, `valor`, `duracao`, `status`); all null is a no-op
  - THEN 200 with `ServicoResponseDTO`
  - AND 400 on `@Valid` failure
  - AND 404 (`EntityNotFoundException`) if `id` does not exist
  - AND 409 (`ServicoNomeDuplicadoException`) if the new `nome` already exists
  - AND 500 for any unmapped exception
- SCEN-003 `GET /api/servicos` (findAll)
  - THEN 200 with a list of `ServicoResponseDTO`
  - AND 500 for any unmapped exception
- SCEN-004 `GET /api/servicos/{id}` (findById)
  - GIVEN a path `id`
  - THEN 200 with `ServicoResponseDTO`
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 500 for any unmapped exception
- SCEN-005 `GET /api/servicos/status/{status}` (findByStatus)
  - GIVEN a path `status` (`ServicoStatus`)
  - THEN 200 with a list of `ServicoResponseDTO`
  - AND 400 if `status` does not match any enum value (Spring's default conversion error, `MethodArgumentTypeMismatchException`)
  - AND 500 for any unmapped exception
- SCEN-006 `DELETE /api/servicos/{id}` (deleteById)
  - GIVEN a path `id`
  - THEN 204 with no body
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 409 (`ServicoComAgendamentoPendenteException`) if a linked `AGENDADO` booking exists
  - AND 500 for any unmapped exception

### Expected
- EXPECTED: all 6 endpoints have `@Operation` and `@ApiResponse` per status from SCEN-001 to SCEN-006.
- NOT EXPECTED: any behavior change on any endpoint.

### Acceptance
- MUST: `nome`/`valor`/`duracao` examples are plausible (e.g. `"Corte masculino"`, `valor: 35.00`, `duracao: 30`), consistent with the 5 seeded `ATIVO` services.
- MUST NOT: add new validation.

### Tasks
- [ ] `@Tag` on the `ServicoController` class.
- [ ] `@Operation` + `@ApiResponse`s on `create`.
- [ ] `@Operation` + `@ApiResponse`s on `update`.
- [ ] `@Operation` + `@ApiResponse`s on `findAll`.
- [ ] `@Operation` + `@ApiResponse`s on `findById`.
- [ ] `@Operation` + `@ApiResponse`s on `findByStatus`.
- [ ] `@Operation` + `@ApiResponse`s on `deleteById`.

### Constraints
- DO NOT: document `ServicoService` or `ServicoEntity`.

## SPEC-006 - UsuarioController documentation

### Goal
- Every endpoint of `UsuarioController` (`/api/usuarios`) documented in Swagger, including `login`'s unconventional behavior.

### Problem
- SITUATION: `POST /api/usuarios/login` always responds 200 with `UsuarioLoginResponseDTO { sucesso, usuario }`; `usuario` is null when the credentials do not match. There is no 401 in this flow (`UsuarioService.findByCredenciais` returns an empty `Optional`, without throwing).
- IMPACT: documenting this endpoint as a conventional 401 flow would mislead a manual tester; Swagger must make clear that a login failure is also a 200.

### Plan
- `@Tag(name = "Usuários", description = "Cadastro e consulta de usuários")` on the class.
- `@Operation` on every method; `@ApiResponse` per status, with `login`'s description spelling out the two possible 200 bodies (`sucesso: true/false`).

### Scenario
- SCEN-001 `POST /api/usuarios` (create)
  - GIVEN a valid `UsuarioCreateDTO` (`nome`, `email`, `senha`), always created with `cargo = CLIENTE`
  - THEN 201 with `UsuarioResponseDTO`
  - AND 400 on `@Valid` failure
  - AND 409 (`UsuarioEmailExistenteException`) if `email` already exists
  - AND 500 for any unmapped exception
- SCEN-002 `POST /api/usuarios/login` (login)
  - GIVEN a `UsuarioLoginRequestDTO` (`email`, `senha`)
  - THEN 200 with `UsuarioLoginResponseDTO { sucesso: true, usuario: <UsuarioResponseDTO> }` when the credentials match
  - AND 200 with `UsuarioLoginResponseDTO { sucesso: false, usuario: null }` when `email` does not exist or `senha` does not match (never 401 here)
  - AND 400 on `@Valid` failure
  - AND 500 for any unmapped exception
- SCEN-003 `PATCH /api/usuarios/{id}` (update)
  - GIVEN a `UsuarioUpdateDTO` with optional fields (`nome`, `email`, `senha`, `cargo`); all null is a no-op
  - THEN 200 with `UsuarioResponseDTO`
  - AND 400 on `@Valid` failure
  - AND 404 (`EntityNotFoundException`) if `id` does not exist
  - AND 409 (`UsuarioEmailExistenteException`) if the new `email` already exists
  - AND 500 for any unmapped exception
- SCEN-004 `GET /api/usuarios` (findAll)
  - THEN 200 with a list of `UsuarioResponseDTO` (no `senha`)
  - AND 500 for any unmapped exception
- SCEN-005 `GET /api/usuarios/{id}` (findById)
  - GIVEN a path `id`
  - THEN 200 with `UsuarioResponseDTO`
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 500 for any unmapped exception
- SCEN-006 `DELETE /api/usuarios/{id}` (deleteById)
  - GIVEN a path `id`
  - THEN 204 with no body
  - AND 404 (`EntityNotFoundException`) if it does not exist
  - AND 409 (`UsuarioComAgendamentoPendenteException`) if a linked `AGENDADO` booking exists
  - AND 500 for any unmapped exception

### Expected
- EXPECTED: `login` documented with two 200 examples (success and failure) and no 401 `@ApiResponse`.
- NOT EXPECTED: any 401 `@ApiResponse` on `UsuarioController` (this controller never throws `AuthException`).

### Acceptance
- MUST: `login`'s description states explicitly, in PT-BR, that the response is always 200 and that a credentials failure is signaled by `sucesso: false`.
- MUST: an `email` example using `admin@trimly.com` (seeded user) in at least one success example.
- MUST NOT: suggest or document a 401 for this `login`.

### Tasks
- [ ] `@Tag` on the `UsuarioController` class.
- [ ] `@Operation` + `@ApiResponse`s on `create`.
- [ ] `@Operation` + `@ApiResponse`s on `login` (two 200 examples).
- [ ] `@Operation` + `@ApiResponse`s on `update`.
- [ ] `@Operation` + `@ApiResponse`s on `findAll`.
- [ ] `@Operation` + `@ApiResponse`s on `findById`.
- [ ] `@Operation` + `@ApiResponse`s on `deleteById`.

### Constraints
- DO NOT: document `UsuarioService`, `UsuarioEntity`, or the `AuthenticationController`/JWT flow within this controller.

## SPEC-007 - AuthenticationController documentation

### Goal
- Every endpoint of `AuthenticationController` (`/api/auth`) documented in Swagger, making clear this is the JWT path currently unused by the frontend.

### Plan
- `@Tag(name = "Autenticação", description = "Cadastro e login via JWT (não utilizado pelo frontend atualmente)")` on the class.
- `@Operation` on every method; `@ApiResponse` per status, including the fixed 401 from `AuthenticationExceptionHandler`.

### Scenario
- SCEN-001 `POST /api/auth/register` (register)
  - GIVEN a valid `UsuarioCreateDTO`
  - THEN 201 with `AuthResponseDTO { token }`
  - AND 400 on `@Valid` failure
  - AND 409 (`UsuarioEmailExistenteException`) if `email` already exists
  - AND 500 for any unmapped exception
- SCEN-002 `POST /api/auth/login` (login)
  - GIVEN an `AuthLoginRequestDTO { email, senha }`
  - THEN 200 with `AuthResponseDTO { token }`
  - AND 400 on `@Valid` failure
  - AND 401 (`AuthCredenciaisInvalidasException`/`AuthTokenInvalidoException`, via `AuthenticationExceptionHandler`) with a fixed message that does not reveal whether the email or the password was wrong
  - AND 500 for any unmapped exception

### Expected
- EXPECTED: the class/tag description mentions that this flow is independent from `UsuarioController`'s login and is not used by the frontend today.
- NOT EXPECTED: any `@SecurityRequirement`/bearer scheme in Swagger for consuming the returned `token` (authorization is not enforced on any endpoint today; see the general CONSTRAINTS section).

### Acceptance
- MUST: the 401 is documented with the real, fixed, generic message from `AuthenticationExceptionHandler` (without revealing which credential failed).
- MUST NOT: add an "Authorize" button/scheme in Swagger UI to use the `token`.

### Tasks
- [ ] `@Tag` on the `AuthenticationController` class.
- [ ] `@Operation` + `@ApiResponse`s on `register`.
- [ ] `@Operation` + `@ApiResponse`s on `login`.

### Constraints
- DO NOT: configure a bearer/JWT `SecurityScheme` in the OpenAPI document (see SPEC-002 and the general CONSTRAINTS).

## SPEC-008 - DTO documentation

### Goal
- Every `record` in `view/dto/` has `@Schema(description, example)` on each component, in PT-BR, consistent with the existing validation messages.

### Plan
- Annotate every record component with `@Schema(description = "...", example = "...")`, without duplicating the text of the existing `jakarta.validation` messages (`@NotNull(message = ...)` etc.) - the `@Schema` description complements the field (explains it), the validation message already covers the rule.
- Cover: `agendamento/` (`AgendamentoCreateDTO`, `AgendamentoUpdateDTO`, `AgendamentoResponseDTO`, `AgendamentoFilter`), `disponibilidade/` (`DisponibilidadeCreateDTO`, `DisponibilidadeUpdateDTO`, `DisponibilidadeResponseDTO`), `servico/` (`ServicoCreateDTO`, `ServicoUpdateDTO`, `ServicoResponseDTO`), `usuario/` (`UsuarioCreateDTO`, `UsuarioUpdateDTO`, `UsuarioResponseDTO`, `UsuarioLoginRequestDTO`, `UsuarioLoginResponseDTO`), `auth/` (`AuthLoginRequestDTO`, `AuthResponseDTO`), `exception/ErrorResponseDTO`.
- Examples use realistic values from the seeded dataset: `email: "admin@trimly.com"`, `diaSemana` in `SEGUNDA`-`SEXTA`, `HH:mm` times within the seeded windows, `duracao` in minutes plausible for a barbershop service.

### Scenario
- GIVEN a request DTO (`*CreateDTO`, `*UpdateDTO`, `*Filter`, `AuthLoginRequestDTO`, `UsuarioLoginRequestDTO`)
- WHEN Swagger UI renders the schema
- THEN every field shows a PT-BR description and a valid example value
- AND optional fields of `*UpdateDTO` make clear, in the description, that they are optional (PATCH semantics)
- GIVEN a response DTO (`*ResponseDTO`, `AuthResponseDTO`, `UsuarioLoginResponseDTO`, `ErrorResponseDTO`)
- WHEN Swagger UI renders the schema
- THEN every field shows a PT-BR description and a realistic example value (e.g. `ErrorResponseDTO` with `status: 404, error: "Not Found", message: "..."`)

### Acceptance
- MUST: every component of every record listed in the Plan has `@Schema(description, example)`.
- MUST: enum examples (`AgendamentoStatus`, `ServicoStatus`, `DiaSemana`, `UsuarioCargo`) use a valid enum value.
- MUST NOT: literally duplicate the text of existing `@NotNull`/`@NotBlank`/`@Positive`/`@Email`/`@FutureOrPresent` messages as the `@Schema` `description`.
- MUST NOT: add, remove or rename fields, or change any existing `jakarta.validation` annotation.

### Tasks
- [ ] `@Schema` on the components of `agendamento/*` (`AgendamentoCreateDTO`, `AgendamentoUpdateDTO`, `AgendamentoResponseDTO`, `AgendamentoFilter`).
- [ ] `@Schema` on the components of `disponibilidade/*` (`DisponibilidadeCreateDTO`, `DisponibilidadeUpdateDTO`, `DisponibilidadeResponseDTO`).
- [ ] `@Schema` on the components of `servico/*` (`ServicoCreateDTO`, `ServicoUpdateDTO`, `ServicoResponseDTO`).
- [ ] `@Schema` on the components of `usuario/*` (`UsuarioCreateDTO`, `UsuarioUpdateDTO`, `UsuarioResponseDTO`, `UsuarioLoginRequestDTO`, `UsuarioLoginResponseDTO`).
- [ ] `@Schema` on the components of `auth/*` (`AuthLoginRequestDTO`, `AuthResponseDTO`).
- [ ] `@Schema` on the components of `exception/ErrorResponseDTO`.

### Constraints
- DO NOT: document entities (`model/entity/`) or services.
- DO NOT: change any existing record/parameter Javadoc beyond adding the `@Schema` annotation next to the parameter.

## SPEC-009 - Verification and Definition of Done

### Goal
- Ensure the integration compiles, is formatted, and is usable end-to-end in Swagger UI before considering the task done.

### Plan
- Compile, format, and manually validate every endpoint through Swagger UI with realistic payloads; confirm that the shape of the `@Valid` failure 400 (`MethodArgumentNotValidException`) is Spring Boot's default (no `@RestControllerAdvice` in `model/exception/handlers/` intercepts it today) and document it as such, without trying to normalize it into `ErrorResponseDTO`.

### Acceptance
- MUST: `./mvnw compile` with no errors.
- MUST: `./mvnw spotless:apply` has been run; every touched Java file stays formatted with palantir-java-format and keeps the project's Javadoc rules (PT-BR, no HTML, only `@param`/`@return`/`@throws`, `@see` only in field Javadoc).
- MUST: with `./mvnw spring-boot:run`, Swagger UI loads at `/swagger-ui.html` and every endpoint across the 5 controllers is exercisable with the documented examples.
- MUST: no "Authorize" button/scheme (bearer/JWT) present in the UI - the absence is intentional and documented, not a TODO.
- MUST NOT: `./mvnw verify` fails due to formatting.

### Tasks
- [ ] Run `./mvnw compile`.
- [ ] Run `./mvnw spotless:apply` on the touched files.
- [ ] Run `./mvnw spring-boot:run` and manually exercise every endpoint through Swagger UI.
- [ ] Confirm that the `@Valid` failure 400 uses Spring Boot's default shape, and document that in the description of the affected endpoints (do not introduce a new handler to standardize it into `ErrorResponseDTO`).

### Constraints
- DO NOT: introduce a new `@RestControllerAdvice` for `MethodArgumentNotValidException` as part of this task; only document the default shape already produced by Spring Boot.

# CONSTRAINTS
- All Swagger-facing text (`@Operation` summary/description, `@ApiResponse` description, `@Schema` description/example, `@ExampleObject`) is in PT-BR, following the domain vocabulary already used in the code (`Agendamento`, `Usuario`, `Servico`, `Disponibilidade`).
- Swagger annotations (`@Operation`, `@ApiResponse`, `@Schema` - English because they are library types) coexist with the existing PT-BR Javadoc on the same method/type; they are not the same thing and do not replace each other.
- Do not wire JWT/bearer into Swagger UI: no `SecurityScheme`, no `@SecurityRequirement`, no "Authorize" button - deferred, not a pending TODO for this task.
- Do not document `model/service/` or `model/entity/`; scope is only `controller/` and `view/dto/`.
- Do not change `config/security/SecurityConfig` or any other file under `config/security/`.
- Formatting: `./mvnw spotless:apply` (palantir-java-format, 4-space indent, 120 columns, single sorted import group, no wildcards) on every touched file; `./mvnw verify` must not fail due to formatting.
- Javadoc: no HTML tags, only `@param`/`@return`/`@throws` on method/type (`@see` only in field Javadoc), PT-BR - rules unchanged by this task, only preserved.
- No change in behavior, signature, path or HTTP verb on any controller; annotations and documentation configuration only.
