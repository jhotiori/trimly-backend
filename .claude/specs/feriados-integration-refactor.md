---
name: feriados-integration-refactor
author: jhotiori
date: 2026-09-17
---

# TASK
Refactor the Feriados (Brazilian public holidays) integration in the Trimly backend: relocate `FeriadosClient`/`FeriadoDTO` out of the retiring `model/entity/external/` package into a new `model/integration/feriados/` package, rename `FeriadoDTO` to `FeriadoResponseDTO`, replace the generic `AgendamentoConflitoException` thrown by `AgendamentoValidator.validateIsFeriado` with a dedicated `AgendamentoFeriadoException` (still mapped to HTTP 409), and bring every touched type's Javadoc up to `backend/CLAUDE.md`'s conventions.

# GOAL
`model/entity/external/` no longer exists; the feriados Feign client and its response DTO live under `model/integration/feriados/`, named `FeriadosClient`/`FeriadoResponseDTO`; a national-holiday conflict on `POST /api/agendamentos`/`PATCH /api/agendamentos/{id}` is reported through its own `AgendamentoFeriadoException` type while still returning `409` with the unchanged message; every touched type carries Javadoc per `backend/CLAUDE.md`.

# PLAN
1. Move and rename the Feign client and its DTO into `model/integration/feriados/`, updating every reference; delete the now-empty `model/entity/external/`.
2. Add `AgendamentoFeriadoException` (extends `AgendamentoException`) and make `validateIsFeriado` throw it instead of `AgendamentoConflitoException`.
3. Add `AgendamentoFeriadoException` to `DomainExceptionHandler`'s existing `409` group.
4. Add/upgrade Javadoc on `FeriadosClient`, `FeriadoResponseDTO`, `listarPorAno`, and the `feriadosClient` field in `AgendamentoValidator`.

# SPECS

## SPEC-001 - Mover e renomear a integração de feriados

### Goal
- `FeriadosClient` and `FeriadoResponseDTO` live in `org.trimly.backend.model.integration.feriados`; `model/entity/external/` no longer exists.

### Problem
- SITUATION: `FeriadosClient` (`@FeignClient`, `listarPorAno(int ano)`) and `FeriadoDTO` (`record FeriadoDTO(String date, String name, String type)`) are the only two members of `src/main/java/org/trimly/backend/model/entity/external/`; `AgendamentoValidator` imports both from there.
- IMPACT: `model/entity/` in this codebase holds only JPA `*Entity` classes (per `backend/CLAUDE.md`); `entity/external` doesn't fit that shape and is being retired in favor of an explicit `model/integration/feriados` package, a new top-level grouping under `model/` alongside `entity/`, `exception/`, `repository/`, `service/`.

### Plan
- Create `src/main/java/org/trimly/backend/model/integration/feriados/`.
- Move `FeriadosClient.java` there: package becomes `org.trimly.backend.model.integration.feriados`; `listarPorAno`'s return type becomes `List<FeriadoResponseDTO>`.
- Move `FeriadoDTO.java` there as `FeriadoResponseDTO.java`: package becomes `org.trimly.backend.model.integration.feriados`; record renamed `FeriadoDTO` -> `FeriadoResponseDTO`; fields unchanged (`String date, String name, String type`).
- In `AgendamentoValidator` (`src/main/java/org/trimly/backend/model/service/agendamento/AgendamentoValidator.java`), replace the imports of `org.trimly.backend.model.entity.external.FeriadosClient` / `FeriadoDTO` with `org.trimly.backend.model.integration.feriados.FeriadosClient` / `FeriadoResponseDTO`; update the `List<FeriadoDTO>` local variable in `validateIsFeriado` (line 192) to `List<FeriadoResponseDTO>`.
- Delete the now-empty `src/main/java/org/trimly/backend/model/entity/external/` directory.
- Grep the codebase for any remaining reference to `model.entity.external` or `FeriadoDTO` and update it.

### Scenario
- GIVEN `AgendamentoValidator.validateIsFeriado` calls `feriadosClient.listarPorAno(ano)`
- WHEN the codebase compiles after the move
- THEN the only import path for the client and DTO is `org.trimly.backend.model.integration.feriados.*`
- AND `model/entity/external/` no longer exists on disk
- AND no file references `org.trimly.backend.model.entity.external.FeriadosClient`, `.FeriadoDTO`, or the bare name `FeriadoDTO`

### Expected
- EXPECTED: `./mvnw compile` succeeds with zero references to the old package or the old record name.
- NOT EXPECTED: any change to `FeriadosClient`'s `@FeignClient(name = "brasilapi-feriados", url = "https://brasilapi.com.br/api/feriados/v1")`, to `listarPorAno`'s `@GetMapping("/{ano}")`/`@PathVariable("ano")`, or to `FeriadoResponseDTO`'s field list/order.

### Acceptance
- MUST: `FeriadosClient.java` exists at `src/main/java/org/trimly/backend/model/integration/feriados/FeriadosClient.java`, package `org.trimly.backend.model.integration.feriados`.
- MUST: `FeriadoResponseDTO.java` exists at `src/main/java/org/trimly/backend/model/integration/feriados/FeriadoResponseDTO.java`, package `org.trimly.backend.model.integration.feriados`, `public record FeriadoResponseDTO(String date, String name, String type)`.
- MUST: `FeriadosClient.listarPorAno` returns `List<FeriadoResponseDTO>`.
- MUST: `AgendamentoValidator` imports and uses `org.trimly.backend.model.integration.feriados.FeriadosClient`/`FeriadoResponseDTO` exclusively.
- MUST NOT: leave `src/main/java/org/trimly/backend/model/entity/external/` on disk.
- MUST NOT: change `FeriadosClient`'s `@FeignClient` attributes or `listarPorAno`'s HTTP mapping annotations.

### Tasks
- [ ] Move `FeriadosClient.java` to `model/integration/feriados/`, updating its package declaration and `listarPorAno`'s return type.
- [ ] Move `FeriadoDTO.java` to `model/integration/feriados/FeriadoResponseDTO.java`, updating its package declaration and record name.
- [ ] Update `AgendamentoValidator`'s imports and the `List<FeriadoDTO>` local variable in `validateIsFeriado`.
- [ ] Delete the empty `model/entity/external/` directory.
- [ ] Grep for any other remaining reference to `model.entity.external` or `FeriadoDTO`.

### Constraints
- DO: keep `FeriadosClient` and `FeriadoResponseDTO` as the only two members of `model/integration/feriados`, mirroring the old `entity/external` layout.
- DO NOT: touch `model/entity/` (JPA entities) or add unrelated files to `model/integration/feriados`.

## SPEC-002 - Exceção dedicada para feriado nacional

### Goal
- `AgendamentoValidator.validateIsFeriado` throws a dedicated `AgendamentoFeriadoException` instead of the generic `AgendamentoConflitoException`.

### Problem
- SITUATION: `validateIsFeriado(LocalDate data)` (`AgendamentoValidator`, lines 191-200) throws `AgendamentoConflitoException("O dia selecionado é feriado nacional")` when `data` matches a BrasilAPI holiday. `AgendamentoConflitoException`'s own Javadoc scopes it to a schedule overlap ("o horário escolhido se sobrepõe a outro agendamento em `AGENDADO` no mesmo dia"), a different failure than "date is a national holiday". `validateIsFeriado`'s current Javadoc has no `@throws` tag:
```java
/**
 * Verifica se está tentando agendar para um feriado.
 *
 * @param data - data de um agendamento
 */
```
- IMPACT: reusing `AgendamentoConflitoException` for two semantically different failures makes the exception's meaning ambiguous and its Javadoc inaccurate at this call site.

### Plan
- Add `src/main/java/org/trimly/backend/model/exception/agendamento/AgendamentoFeriadoException.java`, matching `AgendamentoConflitoException`'s exact shape:
```java
package org.trimly.backend.model.exception.agendamento;

/**
 * Sinaliza que a data selecionada coincide com um feriado nacional.
 */
public class AgendamentoFeriadoException extends AgendamentoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoFeriadoException(String message) {
        super(message);
    }
}
```
- In `AgendamentoValidator`, add the import `org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException` (the existing `AgendamentoConflitoException` import stays: `validateConflitoDeHorario` still throws it).
- Change `validateIsFeriado`'s throw from `AgendamentoConflitoException` to `AgendamentoFeriadoException`, message text unchanged:
```java
if (isFeriado) {
    throw new AgendamentoFeriadoException("O dia selecionado é feriado nacional");
}
```
- Update `validateIsFeriado`'s Javadoc to add a `@throws` tag:
```java
/**
 * Verifica se está tentando agendar para um feriado.
 *
 * @param data - data de um agendamento
 * @throws AgendamentoFeriadoException - quando a data coincide com um feriado nacional
 */
```

### Scenario
- GIVEN `validateIsFeriado(data)` where `data` matches a BrasilAPI holiday for that year
- WHEN the method runs
- THEN it throws `org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException` with message `"O dia selecionado é feriado nacional"`
- AND `validateConflitoDeHorario`'s own overlap check still throws `AgendamentoConflitoException`, unchanged

### Expected
- EXPECTED: `validateIsFeriado`'s only behavior change is the concrete exception class; the message text, the "date matches a holiday" logic, and both `AgendamentoService` call sites (create and update flows) are untouched.
- NOT EXPECTED: any change to `AgendamentoConflitoException` itself, to `validateConflitoDeHorario`, or to `AgendamentoService`'s two calls to `validateIsFeriado`.

### Acceptance
- MUST: `AgendamentoFeriadoException` extends `AgendamentoException`, has a single-arg constructor forwarding `message` to `super`, one-line class Javadoc, in `model/exception/agendamento/`.
- MUST: `validateIsFeriado` throws `new AgendamentoFeriadoException("O dia selecionado é feriado nacional")` verbatim.
- MUST: `validateIsFeriado`'s Javadoc gains `@throws AgendamentoFeriadoException - quando a data coincide com um feriado nacional`.
- MUST NOT: modify `AgendamentoConflitoException` or its use in `validateConflitoDeHorario`.
- MUST NOT: change `AgendamentoService`'s two call sites to `validateIsFeriado`.

### Tasks
- [ ] Add `AgendamentoFeriadoException.java` under `model/exception/agendamento/`.
- [ ] Add the `AgendamentoFeriadoException` import to `AgendamentoValidator`.
- [ ] Change `validateIsFeriado`'s throw statement to `AgendamentoFeriadoException`.
- [ ] Add the `@throws` tag to `validateIsFeriado`'s Javadoc.

### Constraints
- DO: match `AgendamentoConflitoException`/`AgendamentoException`'s exact shape (single message-forwarding constructor, one-line class Javadoc, no extra members).
- DO NOT: add a status field or any HTTP-related member to `AgendamentoFeriadoException`; status mapping lives only in `DomainExceptionHandler`.

## SPEC-003 - Mapeamento HTTP da nova exceção

### Goal
- `AgendamentoFeriadoException` resolves to HTTP `409`, matching today's behavior.

### Problem
- SITUATION: `DomainExceptionHandler` (`src/main/java/org/trimly/backend/model/exception/handlers/DomainExceptionHandler.java`, lines 48-58) groups `AgendamentoConflitoException`, `DisponibilidadeConflitoException`, `ServicoNomeDuplicadoException`, `UsuarioEmailExistenteException`, `ServicoComAgendamentoPendenteException`, `UsuarioComAgendamentoPendenteException` under one `@ExceptionHandler({...})` -> `409`; any `TrimlyException` not grouped falls through to the generic `422` handler (lines 60+). After SPEC-002, `validateIsFeriado` throws `AgendamentoFeriadoException` instead of `AgendamentoConflitoException`.
- IMPACT: without adding `AgendamentoFeriadoException` to this group, it would silently fall to `422`, changing the caller-visible status for a national-holiday conflict from today's `409` - ruled out by the confirmed decision to keep this case at `409`.

### Plan
- Add `AgendamentoFeriadoException` to the existing `@ExceptionHandler({...})` array (lines 48-55), alongside the six exceptions already listed.
- Add `import org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException;` to `DomainExceptionHandler`, sorted with the existing imports.
- No new `@ExceptionHandler` method, no new handler class, no change to the group's `handleConflito` body or `HttpStatus.CONFLICT` return.

### Scenario
- GIVEN a `POST /api/agendamentos` or `PATCH /api/agendamentos/{id}` request whose date lands on a national holiday
- WHEN `AgendamentoValidator.validateIsFeriado` throws `AgendamentoFeriadoException`
- THEN `DomainExceptionHandler.handleConflito` catches it and returns `ErrorResponseDTO{status: 409, error: "Conflict", message: "O dia selecionado é feriado nacional"}`

### Expected
- EXPECTED: the response status/body for a feriado conflict is identical, before and after this refactor, to what `AgendamentoConflitoException` produced today.
- NOT EXPECTED: a second `@ExceptionHandler` method, a new handler class, or any change to the `404`/`422`/`500` mapping of other exceptions.

### Acceptance
- MUST: `AgendamentoFeriadoException.class` is listed inside the same `@ExceptionHandler({...})` array as the other six `409` exceptions.
- MUST: `DomainExceptionHandler` imports `org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException`.
- MUST: a feriado-conflict request still returns HTTP `409` with the unchanged message.
- MUST NOT: add `AgendamentoFeriadoException` to any other group, or leave it unmapped (which would default it to `422`).

### Tasks
- [ ] Add `AgendamentoFeriadoException.class` to `DomainExceptionHandler`'s `409` `@ExceptionHandler` group.
- [ ] Add the corresponding import.

### Constraints
- DO: keep the single grouped `@ExceptionHandler` method (`handleConflito`); do not split it into two methods.
- DO NOT: alter the status/message logic for any of the six other exceptions already in that group.

## SPEC-004 - Documentação Javadoc da integração

### Goal
- `FeriadosClient`, `FeriadoResponseDTO`, `listarPorAno`, and `AgendamentoValidator.feriadosClient` carry Javadoc per `backend/CLAUDE.md` (PT-BR, no HTML, type/method Javadoc limited to `@param`/`@return`/`@throws`, field Javadoc = one-line description + `@see {@link Type}` on the next line, no blank line between).

### Problem
- SITUATION: `FeriadosClient` and `FeriadoDTO`/`FeriadoResponseDTO` have no type-level Javadoc today; `listarPorAno` has no method Javadoc. `AgendamentoValidator.feriadosClient`'s field Javadoc (line 54) is a single, malformed line: `/** Comunicação com a API externa @see {@link FeriadosClient} */` - it doesn't explain what the collaborator does and doesn't follow the two-line field-Javadoc shape (`backend/CLAUDE.md`'s field-Javadoc example: one-line description, then `@see` on its own line, no blank line between).
- IMPACT: a reader of these types gets no explanation of the BrasilAPI feriados integration or of what `date`/`name`/`type` represent, and the malformed field Javadoc doesn't match this repository's own documented convention.

### Plan
- `FeriadosClient` gains type Javadoc and `listarPorAno` gains method Javadoc:
```java
/**
 * Cliente Feign para a API pública de feriados nacionais da BrasilAPI.
 */
@FeignClient(name = "brasilapi-feriados", url = "https://brasilapi.com.br/api/feriados/v1")
public interface FeriadosClient {

    /**
     * Lista os feriados nacionais de um ano.
     *
     * @param ano - ano de referência
     * @return lista de feriados nacionais do ano informado
     */
    @GetMapping("/{ano}")
    List<FeriadoResponseDTO> listarPorAno(@PathVariable("ano") int ano);
}
```
- `FeriadoResponseDTO` gains type Javadoc describing the record and its components:
```java
/**
 * Feriado nacional retornado pela BrasilAPI.
 *
 * @param date - data do feriado, no formato {@code yyyy-MM-dd}
 * @param name - nome do feriado
 * @param type - tipo do feriado (ex.: {@code national})
 */
public record FeriadoResponseDTO(String date, String name, String type) {}
```
- `AgendamentoValidator.feriadosClient`'s field Javadoc is rewritten to the two-line shape:
```java
/**
 * Cliente da API de feriados nacionais (BrasilAPI), usado para bloquear agendamentos em datas de feriado.
 * @see {@link FeriadosClient}
 */
private final FeriadosClient feriadosClient;
```

### Scenario
- GIVEN a reader opens `FeriadosClient`, `FeriadoResponseDTO`, or `AgendamentoValidator`
- WHEN they read the type/method/field Javadoc
- THEN they learn, in PT-BR, that the client integrates with BrasilAPI's feriados nacionais endpoint, what `listarPorAno` returns, what each `FeriadoResponseDTO` field represents, and why `AgendamentoValidator` depends on `FeriadosClient`

### Expected
- EXPECTED: every block above matches `backend/CLAUDE.md`'s Javadoc rules exactly (no HTML tags, only `@param`/`@return`/`@throws` on type/method Javadoc, `@see` only in field Javadoc with no blank line before it).
- NOT EXPECTED: any `@author`/`@since`/`@version` tag, any HTML tag, or a blank Javadoc line between the field's description and its `@see`.

### Acceptance
- MUST: `FeriadosClient` and `FeriadoResponseDTO` each carry the type Javadoc shown in Plan, verbatim.
- MUST: `listarPorAno` carries the method Javadoc shown in Plan, verbatim (`@param ano`, `@return`).
- MUST: `AgendamentoValidator.feriadosClient`'s field Javadoc is replaced with the two-line block shown in Plan, verbatim.
- MUST NOT: introduce `@author`, `@since`, `@version`, or any HTML tag in any of these blocks.

### Tasks
- [ ] Add type Javadoc to `FeriadosClient` and method Javadoc to `listarPorAno`.
- [ ] Add type Javadoc to `FeriadoResponseDTO`.
- [ ] Replace `AgendamentoValidator.feriadosClient`'s field Javadoc with the corrected two-line block.

### Constraints
- DO: keep all Javadoc text in PT-BR, natural and concise, matching the vocabulary already used in sibling field/type Javadoc in this codebase.
- DO NOT: add Javadoc block tags beyond `@param`/`@return`/`@throws` (type/method) or `@see` (field only).

# CONSTRAINTS
- No behavior change beyond the exception type thrown by `validateIsFeriado` and its resulting HTTP status stays `409`: BrasilAPI call, holiday-matching logic, message text, and both `AgendamentoService` call sites are unchanged.
- `model/entity/external/` must not exist after this refactor; `model/integration/feriados/` holds only `FeriadosClient` and `FeriadoResponseDTO`.
- All Javadoc, exception messages, and identifiers stay PT-BR/domain-consistent with the rest of the codebase; no HTML tags; type/method Javadoc limited to `@param`/`@return`/`@throws`; field Javadoc for injected collaborators uses the one-line-description + `@see {@link Type}` shape with no blank line between.
- Run `./mvnw spotless:apply` (or `-DspotlessFiles=<regex>` scoped to the touched files) before considering this refactor done; `./mvnw compile` must succeed with no reference to the old package or `FeriadoDTO` remaining anywhere in the codebase.
