---
name: swagger-description-simplification
author: jhotiori
date: 2026-09-18
---

# TASK
Simplify every Swagger-facing description in the backend, added by `swagger-openapi-integration.md`: `@Operation(description = ...)` and `@ApiResponse(description = ...)` on the 5 controllers (`controller/`), `@Schema(description = ...)` on the DTOs (`view/dto/**`), and `@Tag(description = ...)` + `Info.description` in `config/openapi/OpenApiConfig.java`. Tighten wording only - shorter sentences, less clause-stacking, no filler - while keeping every business rule the `@Operation` descriptions currently list. `@Operation(summary = ...)` stays untouched.

# GOAL
Every Swagger description is as short as it can be without dropping a rule, a status explanation, or a field explanation already present, in the same natural, professional PT-BR, with zero behavior change.

# PLAN
1. Tighten `@Operation`/`@ApiResponse` descriptions on `AgendamentoController`.
2. Tighten `@Operation`/`@ApiResponse` descriptions on `DisponibilidadeController`.
3. Tighten `@Operation`/`@ApiResponse` descriptions on `ServicoController`.
4. Tighten `@Operation`/`@ApiResponse` descriptions on `UsuarioController`.
5. Tighten `@Operation`/`@ApiResponse` descriptions on `AuthenticationController`.
6. Sweep `@Schema(description = ...)` across `view/dto/**`, editing only fields that are actually bloated/overly technical.
7. Sweep `@Tag(description = ...)` on the 5 controllers and `Info.description` in `OpenApiConfig`.
8. Verify the build and re-read every touched description against its original before considering this done.

# SPECS

## SPEC-001 - AgendamentoController description simplification

### Goal
- Shorter `@Operation`/`@ApiResponse` descriptions on `AgendamentoController`, same business rules.

### Problem
- SITUATION: `create`'s and `update`'s `@Operation(description = ...)` are multi-sentence PT-BR paragraphs stacking every business rule in long clauses (e.g. `create`: "Cria um agendamento em AGENDADO para o usuário e o serviço informados, com fim calculado pela duração do serviço. As regras são: início no futuro, início e fim no mesmo dia, início em no máximo 14 dias, uma janela de disponibilidade que comporte todo o horário e nenhuma sobreposição com outro agendamento em AGENDADO. Uma data anterior a hoje é barrada pela validação do corpo (400); hoje com horário já passado retorna 422."); the `findAll` 400 `@ApiResponse` description is a borderline technical run-on ("Filtro com valor inválido, como status fora do enum ou data fora do formato yyyy-MM-dd").
- IMPACT: slower to scan in Swagger UI than the rule list requires.

### Plan
- Rewrite `create`'s description with shorter sentences, same rules, e.g.: "Cria um agendamento em AGENDADO para o usuário e serviço informados; fim = início + duração do serviço. Regras: início no futuro, início e fim no mesmo dia, início em até 14 dias, disponibilidade que cubra todo o horário, sem sobreposição com outro AGENDADO. Data passada é barrada no corpo (400); hoje com horário já passado retorna 422."
- Rewrite `update`'s description analogously, keeping the AGENDADO-only/no-repeat-status/reused-rules content.
- Tighten the `findAll` 400 `@ApiResponse` description, e.g.: "Filtro inválido: status fora do enum ou data fora do formato yyyy-MM-dd."
- Review every other `@ApiResponse(description = ...)` on `create`, `update`, `findAll`, `findById`, `deleteById`; tighten only where wording can shrink without losing meaning.
- Do not touch any `summary`, `responseCode`, `content`, `examples`, or `schema` attribute.

### Scenario
- GIVEN the current `create` `@Operation(description = ...)` (3 sentences, clause-stacked)
- WHEN simplified
- THEN it states the same rules (future start, same-day start/end, <=14 days, full-window availability, no AGENDADO overlap, past-date 400 vs today-passed 422) in fewer/shorter sentences
- AND no rule is dropped or generalized away
- GIVEN the `findAll` 400 `@ApiResponse` description
- WHEN simplified
- THEN it keeps both causes (invalid enum status, invalid date format) in fewer words

### Acceptance
- MUST: every rule present in the original `create`/`update` descriptions (start-in-future, same-day, 14-day limit, availability window, conflict, past-date 400 vs today-passed 422 on create; AGENDADO-only, no-repeat-status, reused rules on update) is still present after simplification.
- MUST: character/word count of every touched description is <= its original.
- MUST NOT: change `summary`, `responseCode`, `content`, `examples`, or any non-`description` attribute on any annotation.
- MUST NOT: add a rule, status, or clause not already present.

### Tasks
- [ ] Simplify `@Operation(description)` on `create`.
- [ ] Simplify `@Operation(description)` on `update`.
- [ ] Review/simplify `@ApiResponse(description)` on `create` (201/400/404/409/422/500).
- [ ] Review/simplify `@ApiResponse(description)` on `update` (200/400/404/409/422/500).
- [ ] Review/simplify `@ApiResponse(description)` on `findAll` (200/400/500), including the borderline 400.
- [ ] Review/simplify `@ApiResponse(description)` on `findById` (200/400/404/500).
- [ ] Review/simplify `@ApiResponse(description)` on `deleteById` (204/400/404/500).

### Constraints
- DO: keep PT-BR, professional tone, every existing rule/status explanation.
- DO NOT: touch `@Operation(summary = ...)`, `@Parameter`, `@Content`, `@Schema(implementation = ...)`, `@ExampleObject`, or `OpenApiExamples` constants.

## SPEC-002 - DisponibilidadeController description simplification

### Goal
- Shorter `@Operation`/`@ApiResponse` descriptions on `DisponibilidadeController`, same business rules.

### Problem
- SITUATION: `create`'s description mixes the ordering rule, the conflict rule, and a parenthetical example in one run-on sentence ("Cria uma janela de atendimento semanal. Início deve ser anterior ao fim, e a janela não pode se sobrepor a outra do mesmo dia; janelas que só se tocam (08:00-12:00 e 12:00-13:00) não conflitam."); `findByDiaSemana`'s description explains a non-obvious behavior (invalid day -> 500, not 400) in a similarly stacked sentence.
- IMPACT: the ordering/conflict/touching-windows distinction and the 500-vs-400 caveat take longer to parse than needed.

### Plan
- Rewrite `create`'s description, e.g.: "Cria uma janela de atendimento semanal. Início deve anteceder o fim; sem sobreposição com outra janela do mesmo dia (janelas que só se tocam, ex. 08:00-12:00 e 12:00-13:00, não conflitam)."
- Rewrite `update`'s description analogously, keeping the no-op-on-all-null and reused-rules content.
- Rewrite `findByDiaSemana`'s description, e.g.: "Lista as janelas do dia informado (SEGUNDA-DOMINGO, case-insensitive). Dia inválido retorna 500, não 400 (sem tratamento dedicado ainda)."
- Review every other `@ApiResponse(description = ...)` on `create`, `update`, `findAll`, `findById`, `findByDiaSemana`, `deleteById`; tighten where possible.

### Scenario
- GIVEN the current `create` description (ordering rule + conflict rule + parenthetical example, one run-on sentence)
- WHEN simplified
- THEN it keeps the ordering rule, the conflict rule, and the touching-windows example, split into shorter sentences
- GIVEN the current `findByDiaSemana` description (day range + case-insensitivity + 500-not-400 caveat)
- WHEN simplified
- THEN the 500-vs-400 caveat is still explicit, not dropped or softened into "may fail"

### Acceptance
- MUST: `create`/`update` still state the ordering rule, the same-day conflict rule, and that touching windows don't conflict.
- MUST: `findByDiaSemana` still states, explicitly, that an invalid day returns 500 (not 400) and that it is current, undedicated behavior.
- MUST: character/word count of every touched description is <= its original.
- MUST NOT: imply `findByDiaSemana`'s behavior was fixed or changed.

### Tasks
- [ ] Simplify `@Operation(description)` on `create`.
- [ ] Simplify `@Operation(description)` on `update`.
- [ ] Simplify `@Operation(description)` on `findByDiaSemana`.
- [ ] Review/simplify `@ApiResponse(description)` on `create` (201/400/409/422/500).
- [ ] Review/simplify `@ApiResponse(description)` on `update` (200/400/404/409/422/500).
- [ ] Review/simplify `@ApiResponse(description)` on `findAll` (200/500).
- [ ] Review/simplify `@ApiResponse(description)` on `findById` (200/400/404/500).
- [ ] Review/simplify `@ApiResponse(description)` on `findByDiaSemana` (200/500).
- [ ] Review/simplify `@ApiResponse(description)` on `deleteById` (204/400/404/500).

### Constraints
- DO: preserve the documented 500-vs-400 caveat on `findByDiaSemana` verbatim in substance.
- DO NOT: touch `@Operation(summary = ...)` or any non-`description` attribute.

## SPEC-003 - ServicoController description simplification

### Goal
- Shorter `@Operation`/`@ApiResponse` descriptions on `ServicoController`, same business rules.

### Plan
- Rewrite `update`'s description, e.g.: "Atualiza os campos informados; nulos são ignorados, todos nulos é no-op. Novo nome deve ser único, novo status não pode repetir o atual, e não é possível desativar serviço com agendamentos futuros."
- Review `create`, `deleteById` descriptions (already short) and every `@ApiResponse(description = ...)` on `create`, `update`, `findAll`, `findById`, `findByStatus`, `deleteById`; tighten where possible, leave as-is where already minimal.

### Scenario
- GIVEN the current `update` description (no-op rule + unique-name rule + no-repeat-status rule + no-deactivation-with-future-bookings rule, one run-on sentence)
- WHEN simplified
- THEN all 4 rules remain, in shorter sentences

### Acceptance
- MUST: `update` still states the no-op-on-all-null rule, the unique-name rule, the no-repeat-status rule, and the no-deactivation-with-future-bookings rule.
- MUST: character/word count of every touched description is <= its original.
- MUST NOT: shorten `create`/`deleteById` descriptions into a form that drops the uniqueness rule or the linked-booking rule.

### Tasks
- [ ] Simplify `@Operation(description)` on `update`.
- [ ] Review `@Operation(description)` on `create`, `deleteById`, `findAll`, `findById`, `findByStatus` (simplify only if not already minimal).
- [ ] Review/simplify `@ApiResponse(description)` on `create` (201/400/409/500).
- [ ] Review/simplify `@ApiResponse(description)` on `update` (200/400/404/409/422/500).
- [ ] Review/simplify `@ApiResponse(description)` on `deleteById` (204/400/404/409/500).
- [ ] Review/simplify `@ApiResponse(description)` on `findAll`, `findById`, `findByStatus`.

### Constraints
- DO NOT: touch `@Operation(summary = ...)` or any non-`description` attribute.

## SPEC-004 - UsuarioController description simplification

### Goal
- Shorter `@Operation`/`@ApiResponse` descriptions on `UsuarioController`, same rules, `login`'s always-200/never-401 explanation kept intact.

### Problem
- SITUATION: `login`'s description explains a non-obvious contract (always 200, `sucesso` flag, no 401 ever) in a stacked sentence; `update`'s description lists 4 rules (unique e-mail, re-encrypted password, no-repeat-cargo, sole-DONO protection) in one run-on sentence.
- IMPACT: the always-200 contract is the most important fact on this endpoint; it must stay unambiguous even after tightening.

### Plan
- Rewrite `login`'s description, e.g.: "Confere e-mail e senha sem emitir token. Sempre 200: sucesso indica o resultado, usuario vem nulo se as credenciais não conferem (sem indicar qual campo falhou). Nunca 401."
- Rewrite `update`'s description, e.g.: "Atualiza os campos informados; nulos são ignorados, todos nulos é no-op. Novo e-mail deve ser único, nova senha (mín. 6 caracteres) é recriptografada, novo cargo não pode repetir o atual, e o único DONO não pode perder o cargo."
- Review `create`, `findAll`, `findById`, `deleteById` descriptions and every `@ApiResponse(description = ...)`; tighten where possible.

### Scenario
- GIVEN the current `login` description (always-200 contract + sucesso/usuario semantics + never-401, one run-on sentence)
- WHEN simplified
- THEN "sempre 200" and "nunca 401" remain explicit statements, not implied

### Acceptance
- MUST: `login`'s simplified description still states, explicitly, that the response is always 200, that `sucesso`/`usuario` carry the result, and that 401 never occurs.
- MUST: `update`'s simplified description still states all 4 rules (unique e-mail, re-encrypted password, no-repeat cargo, sole-DONO protection).
- MUST: character/word count of every touched description is <= its original.
- MUST NOT: weaken "nunca retorna 401" into something implicit or omit it.

### Tasks
- [ ] Simplify `@Operation(description)` on `login`.
- [ ] Simplify `@Operation(description)` on `update`.
- [ ] Review `@Operation(description)` on `create`, `deleteById`, `findAll`, `findById` (simplify only if not already minimal).
- [ ] Review/simplify `@ApiResponse(description)` on `create` (201/400/409/500).
- [ ] Review/simplify `@ApiResponse(description)` on `login` (200/400/500).
- [ ] Review/simplify `@ApiResponse(description)` on `update` (200/400/404/409/422/500).
- [ ] Review/simplify `@ApiResponse(description)` on `deleteById` (204/400/404/409/500).
- [ ] Review/simplify `@ApiResponse(description)` on `findAll`, `findById`.

### Constraints
- DO: keep `login`'s always-200/never-401 statement unambiguous.
- DO NOT: touch `@Operation(summary = ...)` or any non-`description` attribute.

## SPEC-005 - AuthenticationController description simplification

### Goal
- Shorter `@Operation`/`@ApiResponse` descriptions on `AuthenticationController`, same rules, the JWT-unused-by-frontend note kept intact.

### Plan
- Rewrite `register`'s description, e.g.: "Cria um usuário CLIENTE (mesmas regras de POST /api/usuarios) e devolve token JWT. Independente do login de /api/usuarios/login (não usado pelo frontend). Nenhuma rota exige token; por isso o Swagger UI não oferece autorização."
- Rewrite `login`'s description, e.g.: "Confere e-mail e senha e devolve token JWT. Credenciais inválidas retornam 401 com mensagem fixa (sem indicar o campo). Fluxo independente do login de /api/usuarios/login, não usado pelo frontend."
- Review every `@ApiResponse(description = ...)` on `register`, `login`; tighten where possible.

### Scenario
- GIVEN the current `register`/`login` descriptions (JWT purpose + independence from `/api/usuarios/login` + not-used-by-frontend note, stacked sentences)
- WHEN simplified
- THEN both facts (independent flow, unused by frontend) remain, in fewer words

### Acceptance
- MUST: `register`/`login` still state the flow is independent of `/api/usuarios/login` and unused by the frontend today.
- MUST: `login`'s 401 still states the fixed, field-agnostic message.
- MUST: character/word count of every touched description is <= its original.
- MUST NOT: drop the "não usado pelo frontend hoje" note.

### Tasks
- [ ] Simplify `@Operation(description)` on `register`.
- [ ] Simplify `@Operation(description)` on `login`.
- [ ] Review/simplify `@ApiResponse(description)` on `register` (201/400/409/500).
- [ ] Review/simplify `@ApiResponse(description)` on `login` (200/400/401/500).

### Constraints
- DO NOT: touch `@Operation(summary = ...)` or any non-`description` attribute.

## SPEC-006 - DTO @Schema description sweep

### Goal
- Every `@Schema(description = ...)` in `view/dto/**` is as short as it can be without losing meaning; most stay untouched.

### Problem
- SITUATION: `@Schema` descriptions across `agendamento/`, `disponibilidade/`, `servico/`, `usuario/`, `auth/`, `exception/ErrorResponseDTO` are already mostly terse (e.g. `"Identificador do agendamento"`, `"Status atual; AGENDADO é o único status não terminal"`, `"Senha em texto puro; é armazenada criptografada com BCrypt"`).
- IMPACT: little to no bloat expected; simplifying a field that is already minimal risks losing meaning for no size gain.

### Plan
- Read every record's `@Schema(description = ...)` in `agendamento/`, `disponibilidade/`, `servico/`, `usuario/`, `auth/`, `exception/ErrorResponseDTO`.
- Edit only descriptions that are actually bloated or overly technical (run-on, redundant clauses, jargon not needed to understand the field); leave the rest as-is.
- Do not touch `example`, `implementation`, or any other `@Schema` attribute.

### Acceptance
- MUST: any edited `@Schema(description = ...)` is shorter than its original and keeps the same meaning.
- MUST: fields left untouched stay byte-for-byte identical.
- MUST NOT: edit `example` values, add/remove/rename fields, or touch `jakarta.validation` annotations.
- MUST NOT: edit a field's `@Schema(description = ...)` if it is already minimal, purely to have "done something" to every file.

### Tasks
- [ ] Review `@Schema(description)` in `agendamento/*` (`AgendamentoCreateDTO`, `AgendamentoUpdateDTO`, `AgendamentoResponseDTO`, `AgendamentoFilter`).
- [ ] Review `@Schema(description)` in `disponibilidade/*` (`DisponibilidadeCreateDTO`, `DisponibilidadeUpdateDTO`, `DisponibilidadeResponseDTO`).
- [ ] Review `@Schema(description)` in `servico/*` (`ServicoCreateDTO`, `ServicoUpdateDTO`, `ServicoResponseDTO`).
- [ ] Review `@Schema(description)` in `usuario/*` (`UsuarioCreateDTO`, `UsuarioUpdateDTO`, `UsuarioResponseDTO`, `UsuarioLoginRequestDTO`, `UsuarioLoginResponseDTO`).
- [ ] Review `@Schema(description)` in `auth/*` (`AuthLoginRequestDTO`, `AuthResponseDTO`).
- [ ] Review `@Schema(description)` in `exception/ErrorResponseDTO`.

### Constraints
- DO: skip a field entirely if its current description is already minimal.
- DO NOT: touch `example`, `implementation`, field order, or any `jakarta.validation` annotation.

## SPEC-007 - Tag and API metadata description sweep

### Goal
- `@Tag(description = ...)` on the 5 controllers and `Info.description` in `OpenApiConfig` are as short as possible without losing meaning; likely no-ops.

### Plan
- Review the 5 `@Tag` descriptions ("Gestão de agendamentos da barbearia", "Gestão das janelas de atendimento da barbearia", "Gestão dos serviços oferecidos pela barbearia", "Cadastro e consulta de usuários", "Cadastro e login via JWT (não utilizado pelo frontend atualmente)") and `OpenApiConfig`'s `Info.description` ("API de agendamentos da barbearia Trimly: usuários, serviços, disponibilidades e agendamentos.").
- Edit only if a description is longer than needed for its one-line summary; leave already-minimal ones as-is.

### Acceptance
- MUST: any edited `@Tag`/`Info.description` keeps its current meaning (resource covered, JWT-unused-by-frontend note on `AuthenticationController`'s tag).
- MUST NOT: edit `@Tag(name = ...)` or any other `OpenApiConfig`/`Info` attribute (`title`, `version`).

### Tasks
- [ ] Review the 5 `@Tag(description)` values across `AgendamentoController`, `DisponibilidadeController`, `ServicoController`, `UsuarioController`, `AuthenticationController`.
- [ ] Review `Info.description` in `OpenApiConfig`.

### Constraints
- DO NOT: touch `@Tag(name = ...)`, `Info.title`, or `Info.version`.

## SPEC-008 - Verification and Definition of Done

### Goal
- Confirm the simplified descriptions compile, are formatted, and still cover every rule/status/field they covered before, with no behavior change.

### Plan
- Compile and format the touched files; diff every touched description against its pre-simplification text to confirm no rule/status/field explanation was dropped, and that it did not grow.
- Manually spot-check Swagger UI (`/swagger-ui.html`) on a sample of endpoints (at least `AgendamentoController.create`, `UsuarioController.login`, `DisponibilidadeController.findByDiaSemana`) to confirm the simplified descriptions still read clearly in PT-BR.

### Acceptance
- MUST: `./mvnw compile` with no errors.
- MUST: `./mvnw spotless:apply` has been run; every touched Java file stays formatted with palantir-java-format.
- MUST: for every touched description, new length <= original length and every original rule/status/field explanation is still present.
- MUST NOT: `./mvnw verify` fails due to formatting.
- MUST NOT: any `@Operation(summary = ...)`, annotation attribute other than `description`, Java logic, or dependency changed.

### Tasks
- [ ] Run `./mvnw compile`.
- [ ] Run `./mvnw spotless:apply` on the touched files.
- [ ] Diff each touched description against its original text; confirm no rule/status/field dropped and no growth in length.
- [ ] Run `./mvnw spring-boot:run` and spot-check Swagger UI on the sample endpoints above.

### Constraints
- DO NOT: introduce a new annotation, dependency, or `@RestControllerAdvice` as part of this task.

# CONSTRAINTS
- Scope: `@Operation(description = ...)`, `@ApiResponse(description = ...)` on the 5 controllers; `@Schema(description = ...)` on the DTOs; `@Tag(description = ...)` on the 5 controllers; `Info.description` in `OpenApiConfig`. Nothing else.
- `@Operation(summary = ...)` is out of scope; never edit it.
- Size-reduction only: no touched description may grow past its original length; no new description added where none exists today.
- Every business rule, status explanation, or field explanation currently present in a description must remain present after simplification - trim wording, not substance.
- All Swagger-facing text stays PT-BR, natural and professional (not casual/colloquial).
- No change to `summary`, `responseCode`, `content`, `examples`, `schema`/`implementation`, `required`, `@Tag(name = ...)`, `Info.title`, `Info.version`, or any other non-`description` attribute.
- No Java logic change, no new dependency, no behavior change on any endpoint.
- Formatting: `./mvnw spotless:apply` (palantir-java-format) on every touched file; `./mvnw verify` must not fail due to formatting.
