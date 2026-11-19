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
- Comments: none for commented-out code; line comments only for non-obvious intent.
- Fail first: guards (no-op check, validation, not-found) before the logic. Extract a repeated block into a private
  method only when that removes real repetition.

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
