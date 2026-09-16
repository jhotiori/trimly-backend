---
name: password-and-agendamento-validation
author: jhotiori
date: 2026-09-15
---

# TASK
Add three validation rules to the Trimly backend: (1) `UsuarioEntity.senha` must be at least 6 characters, enforced at both the DTO (Bean Validation) and service layers; (2) an `Agendamento` date cannot be more than 14 days ahead of the current date, enforced in `AgendamentoValidator`; (3) both new failures must surface to the API caller as clear, structured feedback, reusing the existing `TrimlyException`/`DomainExceptionHandler`/`GlobalExceptionHandler` pattern rather than a new error-handling mechanism.

# GOAL
A senha shorter than 6 characters and an agendamento more than 14 days out are both rejected before persistence, in every code path (HTTP and internal), with a response shape identical to the other domain/validation errors already produced by this API.

# PLAN
1. Add `@Size(min = 6)` to `senha` on `UsuarioCreateDTO` and `UsuarioUpdateDTO`, and a matching explicit check in `UsuarioValidator`, called from `UsuarioService.create`/`update`.
2. Add a `validateLimiteAntecedencia` rule to `AgendamentoValidator`, called from `AgendamentoService.create`/`update` alongside the existing horário/disponibilidade/conflito checks.
3. Confirm the new failures map to the existing `ErrorResponseDTO`/`@RestControllerAdvice` pipeline with no new handler code, and extend the Swagger documentation and example payloads already present on the affected controllers.

# SPECS

## SPEC-001 - Comprimento mínimo de senha

### Goal
- `senha` requires >= 6 characters on both user creation and update, validated at the DTO and the service layer.

### Problem
- SITUATION: `UsuarioCreateDTO.senha` (`view/dto/usuario/UsuarioCreateDTO.java`) only carries `@NotBlank`; `UsuarioUpdateDTO.senha` (`view/dto/usuario/UsuarioUpdateDTO.java`) carries no validation annotation at all. `UsuarioService.create` and `UsuarioService.update` (`model/service/usuario/UsuarioService.java`) call `passwordEncoder.encode(...)` directly on whatever `senha` they receive, with no length check of their own.
- IMPACT: a 1-character senha passes today's validation and gets BCrypt-persisted on both `POST /api/usuarios` and `PATCH /api/usuarios/{id}` (and on `POST /api/auth/register`, which also carries `@Valid UsuarioCreateDTO` into the same `UsuarioService.create`). Because the only guard is the DTO annotation, any future caller of `UsuarioService.create`/`update` that skips `@Valid` (tests, internal callers) has no protection at all.

### Expected
- EXPECTED: `@Size(min = 6)` on `senha` in both `UsuarioCreateDTO` and `UsuarioUpdateDTO`; `UsuarioValidator` exposes a `validateSenha(String senha)` method throwing `UsuarioException` (existing class, `model/exception/usuario/UsuarioException.java`, used the same way today by `validateCargoUpdate`) when `senha.length() < 6`; `UsuarioService.create` calls it unconditionally, `UsuarioService.update` calls it only when `request.senha()` is non-null and non-blank (same guard already used before `passwordEncoder.encode`).
- NOT EXPECTED: any change to `UsuarioCreateDTO`/`UsuarioUpdateDTO`'s other fields, to `UsuarioMapper`, or to the BCrypt encoding itself.

### Acceptance
- MUST: `UsuarioCreateDTO.senha` and `UsuarioUpdateDTO.senha` both carry `@Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")` (in addition to the existing `@NotBlank` on `UsuarioCreateDTO.senha`).
- MUST: `UsuarioValidator.validateSenha` throws `UsuarioException` for any `senha` with `length() < 6`, independent of the DTO annotations.
- MUST: `UsuarioService.create` and `UsuarioService.update` call `UsuarioValidator.validateSenha` before `passwordEncoder.encode`.
- MUST NOT: rely on the `@Size` annotation alone to guarantee the minimum length; the service-layer check must reject a too-short senha even if invoked without `@Valid`.
- MUST NOT: apply `validateSenha` when `UsuarioUpdateDTO.senha` is `null` (no-op field, same semantics as the other optional PATCH fields).

### Tasks
- [ ] Add `@Size(min = 6, ...)` to `senha` on `UsuarioCreateDTO`.
- [ ] Add `@Size(min = 6, ...)` to `senha` on `UsuarioUpdateDTO`.
- [ ] Add `UsuarioValidator.validateSenha(String senha)`.
- [ ] Call `usuarioValidator.validateSenha(request.senha())` in `UsuarioService.create`.
- [ ] Call `usuarioValidator.validateSenha(senha)` in `UsuarioService.update`, inside the existing `senha != null && !senha.isBlank()` block, before `passwordEncoder.encode`.

### Constraints
- DO: keep the `@Size` validation message and the `UsuarioException` message in PT-BR, matching the tone of the existing messages in both classes.
- DO NOT: introduce a new exception subclass for this rule; `UsuarioException` already covers generic user business-rule violations (see `validateCargoUpdate`).

## SPEC-002 - Limite de antecedência do agendamento

### Goal
- An agendamento's `data`/`horario` cannot be scheduled more than 14 days ahead of the current date.

### Problem
- SITUATION: `AgendamentoValidator` (`model/service/agendamento/AgendamentoValidator.java`) only checks that the start is not in the past (`validateHorarioFuturo`, comparing against `LocalDateTime.now()`) and that start/end fall on the same day (`AgendamentoForaDoHorarioException`); there is no upper bound on how far in the future `data` can be. `AgendamentoCreateDTO.data` (`LocalDate`) only carries `@FutureOrPresent`; `AgendamentoUpdateDTO.data` (`LocalDateTime`) only carries `@FutureOrPresent`. Neither DTO nor `AgendamentoService.create`/`update` (`model/service/agendamento/AgendamentoService.java`) reject a date arbitrarily far in the future.
- IMPACT: a client can book (or move) an agendamento years ahead, which the business does not want to allow.

### Expected
- EXPECTED: `AgendamentoValidator` gains `validateLimiteAntecedencia(LocalDateTime inicioAgendamento)`, using the same `LocalDateTime.now()` pattern already used in `validateHorarioFuturo` (no `Clock` bean exists in this codebase; do not introduce one), throwing a new `AgendamentoAntecedenciaExcedidaException` (extends `AgendamentoException`, same package `model/exception/agendamento/`) when `inicioAgendamento.isAfter(LocalDateTime.now().plusDays(14))`. `AgendamentoService.create` and `AgendamentoService.update` call it for every create/update that reaches validation, in the same position for both methods: right after `validateHorarioFuturo`, before `validateDisponibilidade` (preserves the existing "temporal checks first" ordering documented in `AgendamentoValidator`'s class Javadoc and in `CLAUDE.md`).
- NOT EXPECTED: any change to `validateHorarioFuturo`, `validateDisponibilidade`, `validateConflitoDeHorario`, or to their call order relative to each other; no change to how `inicioAgendamento`/`fimAgendamento` are computed in `AgendamentoService`.

### Acceptance
- MUST: a `data`/`horario` combination whose start is more than 14 days after `LocalDateTime.now()` is rejected on both `POST /api/agendamentos` and `PATCH /api/agendamentos/{id}` (when `data` changes).
- MUST: a start exactly at `LocalDateTime.now().plusDays(14)` is accepted (boundary is `isAfter`, not `isAfter-or-equal`).
- MUST: `AgendamentoValidator.validateLimiteAntecedencia` is called with the same `inicioAgendamento` already computed for `validateHorarioFuturo` in both `create` and `update` (no duplicate date parsing).
- MUST NOT: change the DTOs' existing `@FutureOrPresent` annotations or add a `@Future`/date-range Bean Validation annotation to replace this check; the 14-day rule is a business rule with a moving boundary (`now() + 14 days`), which Bean Validation cannot express declaratively here, so it belongs in `AgendamentoValidator` like the other rules in this class.

### Tasks
- [ ] Add `AgendamentoAntecedenciaExcedidaException` under `model/exception/agendamento/`, extending `AgendamentoException`.
- [ ] Add `AgendamentoValidator.validateLimiteAntecedencia(LocalDateTime inicioAgendamento)`.
- [ ] Call it in `AgendamentoService.create`, right after `agendamentoValidator.validateHorarioFuturo(...)`.
- [ ] Call it in `AgendamentoService.update`, right after `agendamentoValidator.validateHorarioFuturo(...)`.
- [ ] Update `AgendamentoValidator`'s class Javadoc (and the validation-order description in `CLAUDE.md`) to list the new step in order.

### Constraints
- DO: reuse `LocalDateTime.now()` directly, matching `validateHorarioFuturo`; do not add a `Clock` bean or any other time-source abstraction.
- DO NOT: add `AgendamentoAntecedenciaExcedidaException` to `DomainExceptionHandler`'s 404 or 409 groups; it must fall through to the default 422 mapping for `TrimlyException`.

## SPEC-003 - Feedback consistente via validator e exceptions

### Goal
- Both new validation failures (senha curta, agendamento fora do limite de antecedência) reach the API caller as a structured `ErrorResponseDTO`, through the exception pipeline that already exists, with no parallel error-handling mechanism.

### Problem
- SITUATION: `GlobalExceptionHandler.handleValidacao` (`model/exception/handlers/GlobalExceptionHandler.java`) already catches `MethodArgumentNotValidException` (Bean Validation failures on any `@Valid`-annotated body, including the new `@Size` on `senha`) and returns `ErrorResponseDTO{status: 400, error: "Bad Request", message: <joined field messages>}`. `DomainExceptionHandler.handleRegraDeDominio` already catches any `TrimlyException` not explicitly grouped into 404/409 and returns `ErrorResponseDTO{status: 422, error: "Unprocessable Content", message: <exception message>}`. Both `UsuarioException` (SPEC-001) and the new `AgendamentoAntecedenciaExcedidaException` (SPEC-002) are `TrimlyException` subtypes, so no gap exists in this pipeline: no new `@RestControllerAdvice`/`@ExceptionHandler` is needed for either new rule.
- IMPACT: without this spec, it would be unclear whether the new failures need bespoke wiring; they do not, but the Swagger documentation of `UsuarioController`/`AgendamentoController`/`AuthenticationController` (already fully annotated per `swagger-openapi-integration.md`) does not yet mention these two new failure cases, so a caller reading the API docs would not know they exist.

### Expected
- EXPECTED: a too-short senha on `POST /api/usuarios`, `PATCH /api/usuarios/{id}`, or `POST /api/auth/register` returns `400` with `ErrorResponseDTO.message` including the `@Size` message (joined alphabetically with any other failing field message, same behavior as today for other `@NotBlank`/`@Email` failures). An agendamento beyond the 14-day limit on `POST /api/agendamentos` or `PATCH /api/agendamentos/{id}` returns `422` with `ErrorResponseDTO.message` equal to the new exception's message.
- NOT EXPECTED: a `500` or an unhandled-exception stack trace for either failure; no new response DTO or handler class.

### Acceptance
- MUST: `UsuarioException` thrown by `validateSenha` and `AgendamentoAntecedenciaExcedidaException` thrown by `validateLimiteAntecedencia` are both resolved by the existing `DomainExceptionHandler.handleRegraDeDominio` fallback (422), with no code change to `DomainExceptionHandler`.
- MUST: the `@Size` violation on `senha` is resolved by the existing `GlobalExceptionHandler.handleValidacao` (400), with no code change to `GlobalExceptionHandler`.
- MUST: `OpenApiExamples` (`config/openapi/OpenApiExamples.java`) constants `USUARIO_CRIACAO_INVALIDA` and `USUARIO_ATUALIZACAO_INVALIDA` are updated to include a senha-too-short example message; a new example constant is added for the 422 agendamento-antecedência case and referenced from `AgendamentoController`'s `create`/`update` `@ApiResponse(responseCode = "422")`.
- MUST NOT: add a new `@RestControllerAdvice`, a new error DTO, or any bespoke response shape for either new failure.

### Tasks
- [ ] Verify (manually, via `./mvnw spring-boot:run`) that a senha with fewer than 6 characters returns `400` with the `ErrorResponseDTO` shape on `POST /api/usuarios`, `PATCH /api/usuarios/{id}`, and `POST /api/auth/register`.
- [ ] Verify that an agendamento more than 14 days out returns `422` with the `ErrorResponseDTO` shape on `POST /api/agendamentos` and `PATCH /api/agendamentos/{id}`.
- [ ] Update `USUARIO_CRIACAO_INVALIDA`/`USUARIO_ATUALIZACAO_INVALIDA` examples in `OpenApiExamples` to reflect the new `@Size` rule.
- [ ] Add an `OpenApiExamples` constant for the agendamento-antecedência-excedida case and wire it into `AgendamentoController.create`/`update`'s `422` `@ApiResponse`.

### Constraints
- DO: keep every new/updated Swagger example and exception message in PT-BR, consistent with the rest of the codebase.
- DO NOT: change the 400/422/409/404/500 status mapping strategy documented in `CLAUDE.md`'s "Exception to HTTP status" section for any existing exception while implementing this spec.

# CONSTRAINTS
- Specification only: no implementation in this task.
- Preserve `AgendamentoValidator`'s existing validation order (`validateStatusUpdate` on update, then `validateHorarioFuturo`, then the new `validateLimiteAntecedencia`, then `validateDisponibilidade`, then `validateConflitoDeHorario`) in both `AgendamentoService.create` and `update`.
- Reuse `TrimlyException`/`UsuarioException`/`AgendamentoException`/`DomainExceptionHandler`/`GlobalExceptionHandler` as-is; do not introduce a parallel validation or error-handling mechanism.
- All new validation messages, Javadoc, and Swagger text are PT-BR, following this repository's existing vocabulary and formatting rules (`./mvnw spotless:apply`, no HTML in Javadoc, `@param`/`@return`/`@throws` only).
