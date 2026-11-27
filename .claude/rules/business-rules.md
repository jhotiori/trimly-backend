---
paths:
  - "src/main/java/org/trimly/backend/model/service/**"
  - "src/main/java/org/trimly/backend/model/entity/**"
  - "src/main/java/org/trimly/backend/model/repository/**"
---

# Business rules

Preserve validation order and distinct exception types; callers depend on which one is thrown. Overlap checks
use half-open intervals (touching windows don't conflict).

## Update semantics

`*UpdateDTO` fields are nullable (PATCH). Services apply only non-null (strings: non-blank) fields. A request
with every field null is a no-op: entity returned unchanged, no write, no revalidation.

## Agendamento (`AgendamentoValidator`)

End = `data` + `Servico.duracao` min. `AgendamentoService.validateHorario` runs, for both `create` and `update`:
1. `validateIsFeriado` - national holiday (BrasilAPI, `FeriadosClient.listarPorAno`) -> `AgendamentoFeriadoException`
   (409); first, so a holiday wins over the 14-day check.
2. `validateIsRecesso` - start date inside a `Recesso` (`dataInicio` and `dataFim` both inclusive, via
   `RecessoRepository`) -> `AgendamentoRecessoException` (409); right after the holiday check.
3. `validateHorarioFuturo` - start not in the past (`AgendamentoNoPassadoException`), start/end on the same day
   (`AgendamentoEntreDiasException`).
4. `validateLimiteAntecedencia` - start at most `now + 14 days` (exactly 14 accepted) ->
   `AgendamentoAntecedenciaExcedidaException`.
5. `validateDisponibilidade` - the weekday has a `Disponibilidade` (`AgendamentoSemDisponibilidadeException`) and the
   booking fits fully in one window (`AgendamentoForaDaDisponibilidadeException`).
6. `validateConflitoDeHorario` - no overlap with other `AGENDADO` bookings that day ->
   `AgendamentoConflitoException`; `update` passes its own id (skipped), `create` passes `null`.

`update` takes `data`, `servicoId`, `status`. After the no-op check it first runs `validateStatusUpdate`: the booking
must be `AGENDADO` to be modified (`AgendamentoFinalizadoException`), and a given `status` must differ from the
current one (`AgendamentoStatusRepetidoException`). `AGENDADO` is the only non-terminal status; it may move to
`CANCELADO`, `CONCLUIDO` or `AUSENTE`.

## Disponibilidade (`DisponibilidadeValidator`)

`create`/`update` run: `validateHorarios` (`horaInicio` before `horaFim`, else
`DisponibilidadeHorarioInvalidoException`), then `validateConflitoDeHorario` (no overlap on the same `diaSemana`,
whole-day scope since a `Disponibilidade` has no owner, else `DisponibilidadeConflitoException`; application-level
check, no DB constraint).

## Recesso (`RecessoValidator`)

Closure period (`descricao`, `dataInicio`, `dataFim`, `LocalDate`, both ends inclusive; any length, no 14-day cap).
Existing `AGENDADO` bookings inside a new or edited `Recesso` are left untouched. `create` runs `validateNoPassado`
on `dataInicio` (today accepted, else `RecessoNoPassadoException`), `validatePeriodo` (`dataFim` not before
`dataInicio`, else `RecessoPeriodoInvalidoException`), then `validateConflito` (inclusive overlap with another
`Recesso` -> `RecessoConflitoException`, 409; `update` passes its own id, `create` passes `null`). `update` applies
the same order but `validateNoPassado` runs only on a `dataInicio`/`dataFim` present in the request, so an ongoing
`Recesso` can still change `descricao` or `dataFim`; `validatePeriodo` and `validateConflito` use the merged values.
