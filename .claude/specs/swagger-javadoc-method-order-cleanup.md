---
name: swagger-javadoc-method-order-cleanup
author: jhotiori
date: 2026-09-17
---

# TASK
Three non-functional cleanup passes over the Trimly backend, no behavior change: (1) simplify the Swagger/OpenAPI annotation content already present on the 6 controllers (`AgendamentoController`, `DisponibilidadeController`, `ServicoController`, `UsuarioController`, `AuthenticationController`, plus `OpenApiConfig`) - brief descriptions, text blocks instead of `+ ""` string concatenation for multi-line text, drop examples only when they add no value; (2) apply/tighten `backend/CLAUDE.md`'s Javadoc convention project-wide and fill the gaps found, chiefly the field-level Javadoc missing on every JPA entity; (3) extend `backend/CLAUDE.md`'s method-ordering convention with create/update/delete/find granularity and a mapper `toX`/`toXList` pairing rule, then audit and reorder (never rewrite) any method found out of order.

# GOAL
Every Swagger `@Operation`/`@ApiResponse` description in the 6 controllers is a brief, direct summary written as a text block when it spans multiple lines; every class, field and method in scope for `backend/CLAUDE.md`'s Javadoc rules carries complete, concise, PT-BR Javadoc with no gaps (verified module by module, entities first); `backend/CLAUDE.md`'s method-ordering bullet documents the create/update/delete/find granularity and the mapper `toX`/`toXList` rule, and every controller/service/repository/mapper is confirmed compliant or reordered to comply, with zero change to any method's body, signature, visibility or annotations.

# PLAN
1. Simplify Swagger annotation content in the 6 controllers and `OpenApiConfig`: convert every multi-line `+ ""`-concatenated `description` to a text block, trim filler wording to a brief summary, drop an example only when it demonstrably adds nothing beyond the schema.
2. Add the missing field-level Javadoc to all 4 JPA entities and to `EndpointConfig`'s constants; do a project-wide tightening pass on existing Javadoc per module (`controller/`, `view/dto/`, `view/mapper/`, `model/service/<domain>`, `model/repository/`, `config/`).
3. Extend `backend/CLAUDE.md`'s method-ordering bullet with the create/update/delete/find granularity and the mapper `toX`/`toXList` rule; audit every controller, service, repository and mapper against it, reordering (cut/paste only) any method found out of place.

# SPECS

## SPEC-001 - Simplificação do Swagger: AgendamentoController e DisponibilidadeController

### Goal
- `AgendamentoController` and `DisponibilidadeController`'s `@Operation`/`@ApiResponse` descriptions are brief text blocks, with no `+ ""` string concatenation.

### Problem
- SITUATION: both controllers build every multi-line description with `+ ""` string-literal concatenation, e.g. `AgendamentoController.create`:
```java
@Operation(
        summary = "Cria um agendamento",
        description = "Cria um agendamento em AGENDADO para o usuário e o serviço informados, com fim calculado pela"
                + " duração do serviço. Após localizar usuário e serviço, as regras são aplicadas nesta ordem:"
                + " início no futuro, início e fim no mesmo dia, início em no máximo 14 dias a partir de agora,"
                + " uma janela de disponibilidade que comporte todo o horário e nenhuma sobreposição com outro"
                + " agendamento em AGENDADO. Uma data anterior a hoje é barrada pela validação do corpo (400);"
                + " hoje com horário já passado retorna 422."
)
```
  `DisponibilidadeController.findByDiaSemana` has the same pattern.
- IMPACT: the concatenation style contradicts the project's own text-block convention (already used in `OpenApiExamples.java` and in `UsuarioController.login`'s `@ExampleObject`), and the wording is longer than needed to convey the rule.

### Plan
- Convert every concatenated `description` in both controllers to a text block; trim wording to keep only the rule, dropping narrative filler ("Após localizar usuário e serviço, as regras são aplicadas nesta ordem:" -> just list the rules).
- Do not touch `@ApiResponse` response codes, `@Content`/`@Schema`/`@ExampleObject` references, or any Java logic.
- No example in either controller was found redundant enough to drop outright (each `@ExampleObject` maps to a distinct exception/message via `OpenApiExamples`); keep all examples as-is unless a clearer duplication turns up during implementation.

### Scenario
- BEFORE (`AgendamentoController.update`, lines 170-177):
```java
description = "Atualiza data, status e/ou serviço. Campos nulos são ignorados; com todos nulos, devolve o"
        + " agendamento sem alterações e sem revalidar. Só agendamentos em AGENDADO podem ser alterados, e"
        + " o novo status não pode repetir o atual. Valem as mesmas regras de horário, antecedência,"
        + " disponibilidade e conflito da criação, ignorando o próprio agendamento na checagem de"
        + " conflito."
```
  AFTER:
```java
description = """
        Atualiza data, status e/ou serviço; campos nulos são ignorados e todos nulos é um no-op. Só \
        agendamentos em AGENDADO podem ser alterados, e o novo status não pode repetir o atual. Vale a \
        mesma regra de horário, antecedência, disponibilidade e conflito da criação, ignorando o próprio \
        agendamento no conflito."""
```
- BEFORE (`DisponibilidadeController.create`, lines 64-69):
```java
description = "Cria uma janela de atendimento semanal. A hora de início deve ser anterior à de fim, e a"
        + " janela não pode se sobrepor a outra do mesmo dia da semana; janelas que apenas se tocam, como"
        + " 08:00-12:00 e 12:00-13:00, não conflitam."
```
  AFTER:
```java
description = """
        Cria uma janela de atendimento semanal. Início deve ser anterior ao fim, e a janela não pode se \
        sobrepor a outra do mesmo dia; janelas que só se tocam (08:00-12:00 e 12:00-13:00) não conflitam."""
```
- BEFORE (`DisponibilidadeController.findByDiaSemana`, lines 280-283):
```java
description = "Lista as janelas de atendimento do dia informado, de SEGUNDA a DOMINGO, sem diferenciar"
        + " maiúsculas de minúsculas. Um valor que não corresponde a nenhum dia retorna 500, e não 400:"
        + " a conversão do dia ainda não tem tratamento dedicado e cai no erro genérico."
```
  AFTER:
```java
description = """
        Lista as janelas do dia informado, de SEGUNDA a DOMINGO, sem diferenciar maiúsculas de minúsculas. \
        Um dia inválido retorna 500, não 400: a conversão ainda não tem tratamento dedicado."""
```

### Acceptance
- MUST: every `description` in `AgendamentoController` and `DisponibilidadeController` that spans more than one line uses a Java text block, no `+` concatenation left.
- MUST: `@Operation`, `@ApiResponse`, `@Schema`, `@ExampleObject` annotation types are all kept; only their text content is shortened.
- MUST NOT: change any response code, `@Content`/`@Schema`/`implementation`, `OpenApiExamples` reference, method signature, or endpoint behavior.
- MUST NOT: drop an `@ExampleObject` unless it is a literal duplicate of another example already shown on the same `@ApiResponse`.

### Tasks
- [ ] Convert `AgendamentoController.create`'s description to a trimmed text block.
- [ ] Convert `AgendamentoController.update`'s description to a trimmed text block.
- [ ] Convert `DisponibilidadeController.create`'s description to a trimmed text block.
- [ ] Convert `DisponibilidadeController.update`'s description to a trimmed text block.
- [ ] Convert `DisponibilidadeController.findByDiaSemana`'s description to a trimmed text block.

### Constraints
- DO: match the text-block style already used in `OpenApiExamples.java` (indented content, `\` line-continuation to avoid a literal newline mid-sentence).
- DO NOT: reword `@ApiResponse` `description`s that are already a single short line (e.g. "Agendamento não encontrado") - only multi-line/concatenated ones are in scope.

## SPEC-002 - Simplificação do Swagger: ServicoController e UsuarioController

### Goal
- `ServicoController` and `UsuarioController`'s multi-line `@Operation` descriptions become brief text blocks.

### Problem
- SITUATION: `ServicoController.update` and `UsuarioController.create`/`update`/`login` concatenate descriptions with `+ ""`, e.g. `UsuarioController.update` (lines 176-180):
```java
description = "Atualiza os campos informados. Campos nulos ou em branco são ignorados; com todos nulos,"
        + " devolve o usuário sem alterações. O novo e-mail deve ser único, a nova senha deve ter no mínimo"
        + " 6 caracteres e é recriptografada,"
        + " o novo cargo não pode repetir o atual e o único DONO cadastrado não pode perder o cargo."
```
  note the orphan `+ " ..."` line break after "recriptografada,%s" - a leftover of a prior edit, not just verbose wording.
- IMPACT: same text-block gap as SPEC-001, plus this particular block shows the concatenation style is also error-prone (awkward line break mid-sentence).

### Plan
- Convert `ServicoController.update`, `UsuarioController.create`, `UsuarioController.update`, `UsuarioController.login` to text blocks; trim wording.
- `UsuarioController.login`'s `@ApiResponse(responseCode = "200")` already uses text blocks for its two `@ExampleObject`s (`"""{"sucesso": true, ...}"""`) - keep that part unchanged, it is the target style, not something to simplify further.

### Scenario
- BEFORE (`ServicoController.update`, lines 116-118):
```java
description = "Atualiza os campos informados. Campos nulos ou em branco são ignorados; com todos nulos,"
        + " devolve o serviço sem alterações. O novo nome deve ser único, o novo status não pode repetir o"
        + " atual, e o serviço não pode ser desativado enquanto tiver agendamentos futuros."
```
  AFTER:
```java
description = """
        Atualiza os campos informados; nulos são ignorados e todos nulos é um no-op. Novo nome deve ser \
        único, novo status não pode repetir o atual, e o serviço não pode ser desativado com agendamentos \
        futuros."""
```
- BEFORE (`UsuarioController.create`, lines 68-70):
```java
description = "Cadastra um usuário sempre com cargo CLIENTE. O e-mail deve ser único e a senha, com no"
        + " mínimo 6 caracteres, é armazenada criptografada; a resposta nunca expõe a senha."
```
  AFTER:
```java
description = """
        Cadastra um usuário com cargo CLIENTE. E-mail deve ser único; senha (mínimo 6 caracteres) é \
        armazenada criptografada e nunca aparece na resposta."""
```
- BEFORE (`UsuarioController.update`, lines 176-179, with the orphan line break noted above):
```java
description = "Atualiza os campos informados. Campos nulos ou em branco são ignorados; com todos nulos,"
        + " devolve o usuário sem alterações. O novo e-mail deve ser único, a nova senha deve ter no mínimo"
        + " 6 caracteres e é recriptografada,"
        + " o novo cargo não pode repetir o atual e o único DONO cadastrado não pode perder o cargo."
```
  AFTER:
```java
description = """
        Atualiza os campos informados; nulos são ignorados e todos nulos é um no-op. Novo e-mail deve ser \
        único, nova senha (mínimo 6 caracteres) é recriptografada, novo cargo não pode repetir o atual e o \
        único DONO cadastrado não pode perder o cargo."""
```
- BEFORE (`UsuarioController.login`, lines 120-122):
```java
description = "Confere e-mail e senha sem emitir token. A resposta é sempre 200, inclusive quando as"
        + " credenciais não conferem: nesse caso sucesso vem false e usuario vem nulo, sem indicar se o"
        + " e-mail ou a senha falhou. Este login nunca retorna 401."
```
  AFTER:
```java
description = """
        Confere e-mail e senha sem emitir token. Resposta é sempre 200: sucesso indica o resultado e \
        usuario vem nulo quando as credenciais não conferem, sem indicar qual campo falhou. Nunca retorna \
        401."""
```

### Acceptance
- MUST: the four descriptions above become text blocks with no `+` concatenation, including the mid-sentence orphan break in `UsuarioController.update`.
- MUST: `UsuarioController.login`'s existing `@ExampleObject` text blocks (200 examples) are left untouched.
- MUST NOT: change any `@ApiResponse` response code, `OpenApiExamples` reference, or endpoint behavior.

### Tasks
- [ ] Convert `ServicoController.update`'s description to a trimmed text block.
- [ ] Convert `UsuarioController.create`'s description to a trimmed text block.
- [ ] Convert `UsuarioController.update`'s description to a trimmed text block, fixing the orphan line break.
- [ ] Convert `UsuarioController.login`'s description to a trimmed text block.

### Constraints
- DO: keep `UsuarioController.login`'s two JSON `@ExampleObject`s exactly as they are.
- DO NOT: touch `ServicoController.create`, `findAll`, `findById`, `findByStatus`, `deleteById`, or `UsuarioController.findAll`/`findById` - their descriptions are already single-line.

## SPEC-003 - Simplificação do Swagger: AuthenticationController e OpenApiConfig

### Goal
- `AuthenticationController.register`/`login` and `OpenApiConfig.openApi`'s descriptions become brief text blocks.

### Problem
- SITUATION: `AuthenticationController.register` (lines 51-53) and `.login` (lines 102-104), plus `OpenApiConfig.openApi`'s `Info.description` (lines 26-28), all concatenate with `+ ""`:
```java
description = "Cria um usuário com cargo CLIENTE, com as mesmas regras de POST /api/usuarios, e devolve um"
        + " token JWT. Fluxo independente do login de /api/usuarios/login e não utilizado pelo frontend"
        + " atualmente. Nenhuma rota exige o token hoje, por isso o Swagger UI não oferece autorização."
```
- IMPACT: same gap as the other controllers; `OpenApiConfig` is the one non-controller file in Swagger's scope with the same style issue.

### Plan
- Convert `AuthenticationController.register`, `.login`, and `OpenApiConfig.openApi`'s description to text blocks; trim wording, keep the "not used by frontend today" and "no Authorize button" facts since they are load-bearing (documented deliberately in `swagger-openapi-integration.md`).

### Scenario
- BEFORE (`AuthenticationController.register`):
```java
description = "Cria um usuário com cargo CLIENTE, com as mesmas regras de POST /api/usuarios, e devolve um"
        + " token JWT. Fluxo independente do login de /api/usuarios/login e não utilizado pelo frontend"
        + " atualmente. Nenhuma rota exige o token hoje, por isso o Swagger UI não oferece autorização."
```
  AFTER:
```java
description = """
        Cria um usuário CLIENTE (mesmas regras de POST /api/usuarios) e devolve um token JWT. Fluxo \
        independente do login de /api/usuarios/login, não usado pelo frontend hoje; nenhuma rota exige \
        token, por isso o Swagger UI não oferece autorização."""
```
- BEFORE (`AuthenticationController.login`):
```java
description = "Confere e-mail e senha e devolve um token JWT. Credenciais inválidas retornam 401 com"
        + " mensagem fixa, sem indicar se o e-mail ou a senha falhou. Fluxo independente do login de"
        + " /api/usuarios/login e não utilizado pelo frontend atualmente."
```
  AFTER:
```java
description = """
        Confere e-mail e senha e devolve um token JWT. Credenciais inválidas retornam 401 com mensagem \
        fixa, sem indicar qual campo falhou. Fluxo independente do login de /api/usuarios/login, não \
        usado pelo frontend hoje."""
```
- BEFORE (`OpenApiConfig.openApi`):
```java
.description(
        "API de agendamentos da barbearia Trimly: usuários, serviços, disponibilidades e"
                + " agendamentos."
)
```
  AFTER:
```java
.description("""
        API de agendamentos da barbearia Trimly: usuários, serviços, disponibilidades e agendamentos.""")
```

### Acceptance
- MUST: all three descriptions above become text blocks with no `+` concatenation.
- MUST: the facts "not used by frontend today" and "no Authorize button/token enforcement" stay present somewhere in `AuthenticationController`'s descriptions or `@Tag` (already on the class `@Tag`, do not duplicate unnecessarily).
- MUST NOT: change any response code, DTO reference, or endpoint behavior.

### Tasks
- [ ] Convert `AuthenticationController.register`'s description to a trimmed text block.
- [ ] Convert `AuthenticationController.login`'s description to a trimmed text block.
- [ ] Convert `OpenApiConfig.openApi`'s `Info.description` to a text block.

### Constraints
- DO NOT: touch `AuthenticationController`'s `@Tag` (already a single line: `"Cadastro e login via JWT (não utilizado pelo frontend atualmente)"`).

## SPEC-004 - Javadoc de campo nas entidades JPA

### Goal
- Every persisted field of `AgendamentoEntity`, `UsuarioEntity`, `ServicoEntity`, `DisponibilidadeEntity` carries a one-line PT-BR Javadoc description, per `backend/CLAUDE.md`'s "self-explanatory fields get Javadoc, but no `@see`" rule.

### Problem
- SITUATION: none of the 4 entities has any field-level Javadoc today. Example, `AgendamentoEntity` (`model/entity/agendamento/AgendamentoEntity.java`, lines 34-54):
```java
public class AgendamentoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "duracao", nullable = false)
    private Integer duracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgendamentoStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoEntity servico;
}
```
  The same gap exists in `UsuarioEntity` (`id`, `nome`, `email`, `senha`, `cargo`), `ServicoEntity` (`id`, `nome`, `valor`, `duracao`, `status`), and `DisponibilidadeEntity` (`id`, `diaSemana`, `horaInicio`, `horaFim`). By contrast, every DTO record in `view/dto/` already documents its components via `@param` on the record's own Javadoc (e.g. `AgendamentoResponseDTO`), and every service/validator/mapper already documents its injected-collaborator fields with the two-line `@see` shape - entities are the one gap in the codebase.
- IMPACT: a reader of any entity has no field-level documentation at all, the one place in the codebase where the documented field-Javadoc convention is not applied.

### Plan
- Add a one-line Javadoc comment above every field in the 4 entities, no `@see` (these are column/association fields, not injected collaborators - `@see` stays scoped to DI collaborators as already practiced in services/validators/mappers/controllers).
- Class-level Javadoc on all 4 entities is already present and stays unchanged; only field Javadoc is added.

### Scenario
- BEFORE (`AgendamentoEntity`, field block, see Problem).
- AFTER:
```java
public class AgendamentoEntity {
    /**
     * Identificador do agendamento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data e hora de início do atendimento.
     */
    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    /**
     * Duração do atendimento em minutos, herdada do serviço no momento da criação.
     */
    @Column(name = "duracao", nullable = false)
    private Integer duracao;

    /**
     * Status atual do agendamento no ciclo de vida.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgendamentoStatus status;

    /**
     * Usuário dono do agendamento.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    /**
     * Serviço agendado.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoEntity servico;
}
```
- `UsuarioEntity` fields: `id` "Identificador do usuário.", `nome` "Nome completo do usuário.", `email` "E-mail de acesso, único entre os usuários.", `senha` "Senha criptografada com BCrypt.", `cargo` "Cargo do usuário, que define seu papel no sistema."
- `ServicoEntity` fields: `id` "Identificador do serviço.", `nome` "Nome do serviço, único no catálogo.", `valor` "Valor cobrado pelo serviço.", `duracao` "Duração do atendimento em minutos.", `status` "Status do serviço no catálogo (ativo ou inativo)."
- `DisponibilidadeEntity` fields: `id` "Identificador da disponibilidade.", `diaSemana` "Dia da semana da janela de atendimento.", `horaInicio` "Hora de início da janela de atendimento.", `horaFim` "Hora de fim da janela de atendimento."

### Acceptance
- MUST: every field listed above in all 4 entities carries a one-line Javadoc comment placed directly above its JPA annotations.
- MUST NOT: add `@see` to any of these fields (they are not injected collaborators).
- MUST NOT: change any `@Column`/`@Id`/`@Enumerated`/`@ManyToOne`/`@JoinColumn` annotation, field type, or class-level Javadoc.

### Tasks
- [ ] Add field Javadoc to `AgendamentoEntity` (`id`, `data`, `duracao`, `status`, `usuario`, `servico`).
- [ ] Add field Javadoc to `UsuarioEntity` (`id`, `nome`, `email`, `senha`, `cargo`).
- [ ] Add field Javadoc to `ServicoEntity` (`id`, `nome`, `valor`, `duracao`, `status`).
- [ ] Add field Javadoc to `DisponibilidadeEntity` (`id`, `diaSemana`, `horaInicio`, `horaFim`).

### Constraints
- DO: keep every description to one line, PT-BR, no HTML, no filler.
- DO NOT: touch `UsuarioEntity`'s existing method Javadoc (`getAuthorities`, `getPassword`, `getUsername`, `isAccountNonExpired`, etc.) - already compliant, out of scope.

## SPEC-005 - Javadoc de campo em config/

### Goal
- `EndpointConfig`'s constants carry field Javadoc; the rest of `config/` (`config/security/*`, `config/openapi/*`) is confirmed already compliant.

### Problem
- SITUATION: `EndpointConfig` (`config/EndpointConfig.java`) has class Javadoc but no field Javadoc on any of its 6 constants:
```java
public final class EndpointConfig {
    public static final String AGENDAMENTOS_ENDPOINT = "/api/agendamentos";
    public static final String SERVICOS_ENDPOINT = "/api/servicos";
    public static final String USUARIOS_ENDPOINT = "/api/usuarios";
    public static final String DISPONIBILIDADES_ENDPOINT = "/api/disponibilidades";
    public static final String AUTHENTICATION_ENDPOINT = "/api/auth";
    public static final String FRONTEND_ENDPOINT = "http://localhost:4200";

    private EndpointConfig() {}
}
```
  `SecurityConfig`, `CorsConfig`, `PasswordConfig`, `SecurityFilter`, `OpenApiConfig` were all verified during this survey and already carry complete class and field Javadoc (e.g. `SecurityConfig.tokenService`/`.usuarioService` use the two-line `@see` shape correctly) - no change needed there.
- IMPACT: `EndpointConfig` is the only config class in the codebase with undocumented fields.

### Plan
- Add a one-line Javadoc comment above each of the 6 constants, no `@see` (`String` constants are self-explanatory fields).

### Scenario
- AFTER:
```java
public final class EndpointConfig {
    /**
     * Caminho base do controller de agendamentos.
     */
    public static final String AGENDAMENTOS_ENDPOINT = "/api/agendamentos";

    /**
     * Caminho base do controller de serviços.
     */
    public static final String SERVICOS_ENDPOINT = "/api/servicos";

    /**
     * Caminho base do controller de usuários.
     */
    public static final String USUARIOS_ENDPOINT = "/api/usuarios";

    /**
     * Caminho base do controller de disponibilidades.
     */
    public static final String DISPONIBILIDADES_ENDPOINT = "/api/disponibilidades";

    /**
     * Caminho base do controller de autenticação.
     */
    public static final String AUTHENTICATION_ENDPOINT = "/api/auth";

    /**
     * Origem do frontend, liberada em CorsConfig.
     */
    public static final String FRONTEND_ENDPOINT = "http://localhost:4200";

    private EndpointConfig() {}
}
```

### Acceptance
- MUST: all 6 constants in `EndpointConfig` carry a one-line Javadoc comment.
- MUST NOT: add Javadoc to `EndpointConfig`'s private constructor (constructors carrying no logic and no parameters do not need it, consistent with every other `private *Config() {}`/`private *Examples() {}` in the codebase, none of which document their private constructor).
- MUST NOT: change `SecurityConfig`, `CorsConfig`, `PasswordConfig`, `SecurityFilter`, or `OpenApiConfig` - already verified compliant.

### Tasks
- [ ] Add field Javadoc to all 6 constants in `EndpointConfig`.

### Constraints
- DO NOT: add `@see` to any `EndpointConfig` constant.

## SPEC-006 - Revisão de concisão do Javadoc já existente

### Goal
- Existing Javadoc across `controller/`, `view/dto/`, `view/mapper/`, `model/service/<domain>`, `model/repository/` stays PT-BR, concise and gap-free; this spec records the module-by-module audit and its (mostly clean) findings, and tightens the few over-explained spots found.

### Problem
- SITUATION: a full read of all 6 controllers, all 4 domain services plus `auth`'s `AuthenticationService`/`TokenService`, all 4 validators, all 4 repositories, `AgendamentoSpecification`, all 4 mappers, and a sample of DTOs/entities shows the project's Javadoc convention (block tags limited to `@param`/`@return`/`@throws`, field Javadoc with `@see` only for injected collaborators, PT-BR, no HTML) is already applied correctly almost everywhere; the field-level gap is isolated to `model/entity/` and `EndpointConfig` (SPEC-004, SPEC-005). The one recurring, minor over-explanation pattern is in a few method Javadocs that restate what the method signature already makes obvious, e.g. `AgendamentoService.calculateFimAgendamento`:
```java
/**
 * Calcula o horário de fim de um agendamento.
 *
 * @param inicioAgendamento - data e hora de início do agendamento
 * @param duracao - duração do serviço em minutos
 * @return LocalDateTime - data e hora de fim calculada
 */
private LocalDateTime calculateFimAgendamento(LocalDateTime inicioAgendamento, Integer duracao) {
```
  this one is already at the right level of brevity and is not a violation; it is kept here only as the passing example of the standard every other method Javadoc in scope should match.
- IMPACT: no material gap found beyond SPEC-004/SPEC-005; this spec exists to make the audit explicit per module (as requested) and to catch any wording drift during implementation, not to mandate a rewrite of already-compliant text.

### Plan
- Per module, confirm (or fix on the spot if a reviewer finds a stray HTML tag, extra block tag, or over-long description while touching a file for SPEC-001 to SPEC-005):
  - `controller/`: all 6 controllers - class and field Javadoc present and concise (verified: `AgendamentoController`, `DisponibilidadeController`, `ServicoController`, `UsuarioController`, `AuthenticationController`).
  - `view/dto/<module>/`: all records document components via `@param` on the record Javadoc, matching the DTO's `@Schema` descriptions without duplicating them (verified: `AgendamentoResponseDTO`, `AgendamentoFilter`, `ErrorResponseDTO`, `UsuarioCreateDTO`, `UsuarioUpdateDTO`).
  - `view/mapper/`: all 4 mappers - method Javadoc present and concise on every `toEntity`/`toEntityList`/`toResponse`/`toResponseList`.
  - `model/service/<domain>` (`servico`, `usuario`, `agendamento`, `disponibilidade`, `auth`): all services and validators - class Javadoc states the domain rule order where relevant (`AgendamentoValidator`, `DisponibilidadeValidator`), field Javadoc uses the two-line `@see` shape for every injected collaborator, method Javadoc lists every `@throws` actually thrown.
  - `model/repository/`: all 4 repositories plus `AgendamentoSpecification` - method Javadoc present on every derived query method.
- No rewrite is prescribed by this spec beyond what SPEC-001 to SPEC-005 already touch; this spec's task list is the audit checklist itself.

### Scenario
- [x] `controller/` - 6/6 controllers audited, class/field Javadoc compliant.
- [x] `view/dto/` - sampled records (`agendamento`, `usuario`, `exception`) compliant; convention (```@param``` per component, `@Schema` for Swagger, no duplication) confirmed consistent.
- [x] `view/mapper/` - 4/4 mappers audited, method Javadoc compliant.
- [x] `model/service/agendamento`, `disponibilidade`, `servico`, `usuario`, `auth` - all services/validators audited, field/method Javadoc compliant.
- [x] `model/repository/` - 4 repositories + `AgendamentoSpecification` audited, method Javadoc compliant.
- [ ] `model/entity/` - gap found and fixed in SPEC-004.
- [ ] `config/` - gap found and fixed in SPEC-005 (`EndpointConfig`); rest of `config/` confirmed compliant.

### Expected
- EXPECTED: after SPEC-004 and SPEC-005 land, every module listed above has zero Javadoc gaps against `backend/CLAUDE.md`'s convention.
- NOT EXPECTED: a blanket rewrite of already-concise Javadoc; only SPEC-004/SPEC-005's additions and any stray issue caught incidentally while editing a file for SPEC-001 to SPEC-003.

### Acceptance
- MUST: this module-by-module audit is re-run after SPEC-004/SPEC-005 are applied, confirming zero remaining gaps.
- MUST NOT: introduce `@author`, `@since`, `@version`, or any HTML tag anywhere while touching Javadoc under this spec.

### Tasks
- [ ] Re-run the module checklist above after SPEC-004 and SPEC-005 are applied and confirm all boxes compliant.
- [ ] Fix any stray HTML tag, extra block tag, or clearly over-long description noticed while editing files for SPEC-001 to SPEC-005 (none identified as of this audit).

### Constraints
- DO NOT: rewrite Javadoc text that already meets the convention just for stylistic preference.
- DO NOT: touch `model/exception/` classes' Javadoc as part of this spec (out of scope; not surveyed here).

## SPEC-007 - Convenção e auditoria de ordenação de métodos

### Goal
- `backend/CLAUDE.md`'s method-ordering bullet documents the create/update/delete/find granularity and the mapper `toX`/`toXList` pairing rule; every controller, service, repository and mapper is confirmed compliant or reordered (cut/paste only) to comply.

### Problem
- SITUATION: `backend/CLAUDE.md`'s current bullet only says "by action then specificity (`create > update > findAll > findById > findByX > deleteById`); public methods first, private last" - it has no explicit rule for `delete`'s position relative to the `find*` group, and no rule at all for mapper `toX`/`toXList` pairing. A full read of all 6 controllers, all 4 domain services (plus `AuthenticationService`/`TokenService`), all 4 repositories (plus `AgendamentoSpecification`), and all 4 mappers found every one of them already ordered per the tightened rule requested (create, update, findAll, findById, findByX/other finders, deleteById, private helpers last; mapper `toEntity`[, `toEntityList`] before `toResponse`[, `toResponseList`]) - e.g. `AgendamentoService`: `create`, `update`, `findAll()`, `findAll(filtro)`, `findById`, `findByStatus`, `findByStatusAndPeriodo`, `deleteById`, `calculateFimAgendamento` (private, last); `DisponibilidadeMapper`: `toEntity`, `toEntityList`, `toResponse`, `toResponseList`.
- IMPACT: without codifying the granular rule in `backend/CLAUDE.md`, the convention that the current code already follows is only implicit (inferable from example, not stated), so a future addition could drift out of order with no written rule to catch it in review.

### Plan
- Update `backend/CLAUDE.md`'s "Method ordering" bullet to state explicitly: `create` > `update` > `delete` > `findAll` > `findById` > `findByX` (multi-result finders); public before private; and, for `*Mapper` classes, `toX` (single conversion) before `toXList` (list conversion) per direction, each direction's pair (`toEntity`/`toEntityList`, `toResponse`/`toResponseList`) kept together.
- Audit every file in scope against the extended rule (see Scenario checklist); reorder only the methods found out of place, moving them verbatim (no change to body, signature, visibility, or annotations).
- `AgendamentoValidator`, `DisponibilidadeValidator`, `ServicoValidator`, `UsuarioValidator` are excluded: their methods are already ordered by validation-execution sequence (a domain rule documented in each class's own Javadoc and in `backend/CLAUDE.md`'s "Agendamento business rules"/"Disponibilidade business rules" sections), not by create/update/find semantics.

### Scenario
- [x] `AgendamentoController`: `create`, `update`, `findAll`, `findById`, `deleteById` - compliant.
- [x] `DisponibilidadeController`: `create`, `update`, `findAll`, `findById`, `findByDiaSemana`, `deleteById` - compliant (single-lookup `findById` precedes the multi-result `findByDiaSemana`).
- [x] `ServicoController`: `create`, `update`, `findAll`, `findById`, `findByStatus`, `deleteById` - compliant.
- [x] `UsuarioController`: `create`, `login`, `update`, `findAll`, `findById`, `deleteById` - compliant (`login` is neither find nor delete; its place right after `create` is not a violation of this rule).
- [x] `AuthenticationController`: `register`, `login` - compliant.
- [x] `AgendamentoService`: `create`, `update`, `findAll()`, `findAll(filtro)`, `findById`, `findByStatus`, `findByStatusAndPeriodo`, `deleteById`, `calculateFimAgendamento` (private) - compliant.
- [x] `DisponibilidadeService`: `create`, `update`, `findAll`, `findById`, `findByDiaSemana`, `deleteById` - compliant.
- [x] `ServicoService`: `create`, `update`, `findAll`, `findById`, `findByNome`, `findByStatus`, `deleteById` - compliant.
- [x] `UsuarioService`: `create`, `update`, `findAll`, `findById`, `findByNome`, `findByEmail`, `findByCredenciais`, `deleteById` - compliant.
- [x] `AuthenticationService`: `register`, `login` - compliant.
- [x] `TokenService`: `generateToken`, `validateToken`, `generateExpirationTime` (private) - compliant.
- [x] `AgendamentoRepository`, `DisponibilidadeRepository`, `ServicoRepository`, `UsuarioRepository`, `AgendamentoSpecification`: no `create`/`update`/`delete` overrides (inherited from `JpaRepository`); custom `findByX`/`existsByX` methods already grouped sensibly - compliant.
- [x] `AgendamentoMapper`: `toEntity`, `toResponse`, `toResponseList` - compliant (no `toEntityList` exists, nothing to pair).
- [x] `DisponibilidadeMapper`, `ServicoMapper`, `UsuarioMapper`: `toEntity`, `toEntityList`, `toResponse`, `toResponseList` - compliant.

### Expected
- EXPECTED: `backend/CLAUDE.md` states the granular rule explicitly; the checklist above is the audit record showing the codebase needs no reordering today.
- NOT EXPECTED: any change to a method's body, signature, visibility, or annotations anywhere in this spec; reordering a method that is not actually in violation "to tidy it further".

### Acceptance
- MUST: `backend/CLAUDE.md`'s "Method ordering" bullet documents `create` > `update` > `delete` > `findAll` > `findById` > `findByX`, and the mapper `toX`-before-`toXList`-per-direction rule.
- MUST: every file in the Scenario checklist is re-verified against the extended rule at implementation time; any method found out of order is moved (cut/paste only) to comply.
- MUST NOT: change any method's logic, body, signature, visibility, or annotations while reordering.
- MUST NOT: reorder `AgendamentoValidator`, `DisponibilidadeValidator`, `ServicoValidator`, or `UsuarioValidator` under this rule.

### Tasks
- [ ] Update `backend/CLAUDE.md`'s "Method ordering" bullet with the granular create/update/delete/find rule and the mapper `toX`/`toXList` rule.
- [ ] Re-verify the 6 controllers against the extended rule; reorder any violation found.
- [ ] Re-verify the domain services, `AuthenticationService`, and `TokenService` against the extended rule; reorder any violation found.
- [ ] Re-verify the 4 repositories and `AgendamentoSpecification` against the extended rule; reorder any violation found.
- [ ] Re-verify the 4 mappers' `toX`/`toXList` pairing; reorder any violation found.
- [ ] Run `./mvnw spotless:apply` on any file touched by a reorder.

### Constraints
- DO: treat this strictly as a cut/paste reordering task.
- DO NOT: change any method's logic, body, signature, visibility, or annotations while reordering.
- DO NOT: apply this rule to `model/service/<domain>/*Validator` classes; their order is governed by validation-execution sequence instead.

# CONSTRAINTS
- No behavior change anywhere in this task: no HTTP route, status code, response body, business rule, or validation order changes as a side effect of any of the three cleanups.
- Swagger simplification keeps every existing annotation type (`@ApiResponses`, `@ApiResponse`, `@Operation`, `@Schema`, `@ExampleObject`) - only the text content and, where multi-line, the string-building style (text block instead of `+` concatenation) change.
- Javadoc stays PT-BR, natural, concise, direct; no HTML tags; no em-dashes or filler; type/method Javadoc limited to `@param`/`@return`/`@throws`; field Javadoc uses the two-line description + `@see {@link Type}` shape only for injected collaborators, and a plain one-line description (no `@see`) for self-explanatory fields and entity columns/associations.
- Method reordering is cut/paste only: no method's body, signature, visibility, or annotations may change as a result of SPEC-007.
- Run `./mvnw spotless:apply` (or `-DspotlessFiles=<regex>` scoped to touched files) before considering any of these specs done; `./mvnw compile` and `./mvnw spotless:check` must both pass.
- This is a specification-only deliverable: no file under `src/` is touched while producing this document.
