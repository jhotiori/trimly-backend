---
paths:
  - "src/main/java/org/trimly/backend/model/exception/**"
  - "src/main/java/org/trimly/backend/model/service/**"
  - "src/main/java/org/trimly/backend/infra/openapi/**"
---

# Errors and logging

All leaf domain exceptions extend `TrimlyException` directly (keeps inheritance depth at 5, the Sonar limit;
no per-module base classes). Handlers under `model/exception/handlers/` return `ErrorResponseDTO.of(status,
message)` and log the full exception.

- `AuthenticationExceptionHandler` (`HIGHEST_PRECEDENCE`): `AuthException` family (extends `RuntimeException`,
  not `TrimlyException`) -> 401 with a fixed message that doesn't reveal which credential failed;
  `AccessDeniedException` (a `@PreAuthorize` denial inside the controller layer) -> 403, without which the catch-all
  would answer 500.
- `DomainExceptionHandler`, by class: `*NaoEncontrado(a)Exception` (one per resource) -> 404;
  `AgendamentoConflitoException`, `AgendamentoFeriadoException`, `DisponibilidadeConflitoException`,
  `ServicoNomeDuplicadoException`, `UsuarioEmailExistenteException`, `ServicoComAgendamentoPendenteException`,
  `UsuarioComAgendamentoPendenteException` -> 409; every other `TrimlyException` -> 422.
- `GlobalExceptionHandler`: validation/unreadable body/bad parameter -> 400; unknown route -> 404; unsupported
  method -> 405; any other `Exception` -> 500 with a generic message.
- One exception class per error. Each leaf has a no-arg constructor holding its fixed default message and a
  `(String message)` constructor for a custom one; throw sites use the no-arg form (`orElseThrow(XException::new)`).
- New domain exception: extend `TrimlyException` (defaults to 422); add to a group only for 404/409. New auth
  exception: extend `AuthException` (401), no wiring.
- Messages: PT-BR, one short direct sentence, fixed (no interpolated values), living in the exception class; logs
  keep the detail. Not-found uses `<Recurso> não encontrado(a)` for all four resources. Swagger error examples in
  `infra/openapi/OpenApiExamples` quote them verbatim.
- Log levels: client errors (domain, validation, auth, denied) `WARN`; unexpected `ERROR` with stack trace; request and
  operation tracing `DEBUG`; security and lifecycle events (login, register, user removal) `INFO`. Never log
  passwords, tokens or the secret; identify users by e-mail or id.
