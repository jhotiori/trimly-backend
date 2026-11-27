---
paths:
  - "src/main/java/org/trimly/backend/infra/security/**"
  - "src/main/java/org/trimly/backend/model/service/auth/**"
  - "src/main/java/org/trimly/backend/controller/**"
  - "src/main/java/org/trimly/backend/infra/openapi/**"
---

# Security

Stateless JWT, hand-rolled (no oauth2-resource-server). HMAC256 kept on purpose: one service signs and verifies, so
asymmetric keys would add key management for nothing. Passwords use BCrypt.

`infra/security/` splits by concern: `authentication/` (`JwtConfig`, `JwtProperties`, `SecurityFilter`,
`PasswordConfig`), `authorization/` (`AccessPolicy`), and at the root the beans both share (`SecurityConfig`,
`CorsConfig`, `SecurityErrorHandler`).

## Authentication

- `JwtConfig`: builds the `Algorithm` and `JWTVerifier` beans once. `JwtProperties` (`trimly.security.jwt.issuer|expiry`)
  binds the rest; the secret is read with `@Value` because the `@ConfigurationProperties` binder would accept an
  unresolved `${JWT_SECRET}` literal and start anyway.
- `TokenService` (`model/service/auth`): subject = e-mail, `cargo` claim, `iat`, issuer `JWT_ISSUER` (default
  `trimly-auth-api`), expiry `JWT_EXPIRY` (default `28800`, 8 hours).
- `AuthenticationService`: `register` -> `UsuarioService.create` (always `CLIENTE`, a `cargo` in the body is ignored)
  then token; `login` -> `UsuarioService.findByCredenciais` or `AuthCredenciaisInvalidasException`. Both return
  `AuthResponseDTO` (`token` + `UsuarioResponseDTO`). `/api/auth/*` is the only sign-in and sign-up path. No
  `AuthenticationManager` or `UserDetailsService` bean.
- `SecurityFilter` (plain class built by the private `SecurityConfig.securityFilter()`, not a bean, so the container
  doesn't register it outside the chain): reads the `Bearer` token and populates `SecurityContext`. An invalid,
  expired or orphaned token clears the context and continues anonymous, so a protected route then answers 401.
  `UsuarioEntity implements UserDetails`; authority = `ROLE_` + `cargo`.

## Chain

`SecurityConfig` (`@EnableMethodSecurity`): CORS (`CORS_ORIGIN`), CSRF off, `STATELESS`. Public: `/api/auth/**`,
OpenAPI/Swagger, `/error`, and `/h2-console/**` only when the profile enables it. Everything else authenticated.
`SecurityErrorHandler` writes the chain's 401/403 as `ErrorResponseDTO` (filters run before the dispatcher).
`OpenApiConfig` adds `bearerAuth` plus 401 to every non-auth operation, and 403 where `@PreAuthorize` is present.

## Authorization

Rules sit on controller methods: `@PreAuthorize("hasAnyRole('DONO', 'ADMIN')")` for staff-only and
`@PreAuthorize("@accessPolicy.x(...)")` for ownership. `AccessPolicy` never throws: staff short-circuit (a missing id
still yields 404), a `CLIENTE` gets `false` (403) for unknown ids. `DONO` and `ADMIN` are always equal.

| Endpoint | `CLIENTE` | `DONO` / `ADMIN` |
|---|---|---|
| `GET` servicos, disponibilidades, recessos, feriados | yes | yes |
| `POST`/`PATCH`/`DELETE` servicos, disponibilidades, recessos | no | yes |
| `POST agendamentos` | own `usuarioId` only | any |
| `GET agendamentos` | own only (filter forced by `AccessPolicy.restrictFilter`) | all |
| `GET agendamentos/{id}` | own | any |
| `PATCH agendamentos/{id}` | own; `status` null or `CANCELADO` | any |
| `DELETE agendamentos/{id}` | no | yes |
| `GET usuarios` | no | yes |
| `GET usuarios/{id}` | self | any |
| `PATCH usuarios/{id}` | self, no `cargo` | any, `cargo` allowed |
| `DELETE usuarios/{id}` | no | yes |

`UsuarioCargo`: `CLIENTE`, `ADMIN` (developers), `DONO` (owner). `ADMIN` and `DONO` come only from seeds/DB or a staff
`cargo` patch.
