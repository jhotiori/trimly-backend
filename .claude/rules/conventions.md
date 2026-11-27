---
paths:
  - "src/**/*.java"
  - ".config/eclipse-formatter.prefs"
---

# Conventions

- Casing: `camelCase` (methods, vars, properties); `PascalCase` (classes, objects).
- Naming: English verb (`find`/`get`/`delete`) + PT-BR spec: `findByNome`, `getByEmail`. Vars: shortest
  descriptive name (`nome`, `usuarioId`, `isAtivo`). Domain types carry the domain prefix (`DisponibilidadeDiaSemana`).
- Method order: by action then specificity - `create` > `update` > `delete` > `findAll` > `findById` >
  `findByX`; public before private. `*Mapper`: each direction paired, single conversion before list
  (`toEntity`, `toEntityList`, then `toResponse`, `toResponseList`). `*Validator` exempt: methods follow the
  validation order in [business-rules](business-rules.md).
- Generics: parametrize a collection with a wildcard only for non-final types (`List<? extends XEntity>`);
  `final` types (records/DTOs) use the exact type (`List<XCreateDTO>`).
- Utility classes: `final` + private constructor.
- Single-line strings use plain literals; text blocks only for multi-line content or `\` continuation.
- Argument and null handling: `Objects.requireNonNull`/`requireNonNullElse` over manual `if ... throw new
  IllegalArgumentException` or `x != null ? x : y`; `StringUtils.hasText` over `s != null && !s.isBlank()`;
  `"...".formatted(...)` over `String.format`.

## Code rules

Apply to new and touched code; do not refactor untouched code for them.

- Guard clauses: guards (no-op check, validation, not-found) come first and `return`/`throw`; the happy path stays
  unindented. No `else` after `return`/`throw`; no more than two nesting levels.
- Immutability: records for DTOs and value types; `final` on fields and locals; return `List.of(...)` or
  `Stream.toList()`, never a list the caller can mutate. JPA entities stay mutable (JPA requires it).
- No null: a lookup that may find nothing returns `Optional<T>`; a collection is empty, never `null`. `Optional` is a
  return type only, never a field or parameter. Exempt: nullable `*UpdateDTO` fields (PATCH, see
  [business-rules](business-rules.md)) and JPA columns.
- Focused methods: one job at one level of abstraction. If the name needs "and", or a block needs a comment to be
  understood, extract a private method. Service orchestrates, validator validates, mapper maps, controller delegates.
- No magic values: a literal that carries meaning becomes a `private static final` constant
  (`LIMITE_ANTECEDENCIA_DIAS`), an enum, or a `trimly.*` property; base paths live in `EndpointConfig`. `0`, `1` and
  `""` with an obvious meaning stay inline.
- Fail fast at boundaries: validate at the edge (Bean Validation on DTOs, `@Min`/`@Max` on request parameters, domain
  validators in the service) before any database or external call. Invalid input never travels deeper.
- Exceptions: catch the narrowest type (`FeignException`, `DataIntegrityViolationException`); never `catch
  (Exception)` or `Throwable` outside the global handler; never an empty `catch`. A wrapping exception receives the
  cause, and the failure is logged once (at the catch or in the handler, not both).
- Comments: inline comments only for complex or ambiguous logic, and they say why (constraint, trade-off, external
  quirk), never what; if a comment restates the code, delete it or rename instead. No commented-out code. Every
  comment, inline or Javadoc, uses plain, clear, concise, direct wording. Javadoc follows the section below.
- Logging: SLF4J with parameters (`log.warn("... {}", id)`), never string concatenation, `System.out` or
  `printStackTrace`.

## Formatting

Spotless + Eclipse JDT (4.40), settings in `.config/eclipse-formatter.prefs`. 4-space indent, 8-space continuation,
120 cols; wrapped lists go one element per line, closing `)` on its own line; long parameter and record component
annotations go one per line. Javadoc is not reformatted: wrap it by hand. Run `./mvnw spotless:apply` before
committing; `./mvnw verify` fails on unformatted code. After editing the prefs, delete `target/spotless-index` or
Spotless skips unchanged files.

## Javadoc

- PT-BR, natural, concise. No HTML tags (paragraphs separated by a blank ` *` line), no em-dashes, no filler.
- Inline code uses backticks (`` `AGENDADO` ``), never `{@code}`; `{@link}` is fine. Method/type block tags:
  `@param`, `@throws`, `@return` only, in that order, in the form `@param name - desc`.
- Class Javadoc: sits before the annotations; one brief sentence (max 200 characters); a second paragraph only for
  a non-obvious constraint.
- Fields: Javadoc on every field. Class-typed fields (injected collaborators, mocks, entities, enums) get a 1-line
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
