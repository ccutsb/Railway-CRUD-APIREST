# ADR-001 - Estrategia de autenticacion JWT + Refresh Token

- Estado: Aprobado
- Fecha: 2026-03-04

## Contexto

El API requiere autenticacion escalable sin sesiones server-side, con posibilidad de invalidar sesiones y soportar roles.

## Decision

Usar:

- Access token JWT de corta vida (15 min por defecto).
- Refresh token persistido en base de datos con rotacion en cada refresh.
- Invalidacion en logout marcando token revocado.
- RBAC con roles `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER`.

## Consecuencias

Positivas:

- Escalable horizontalmente (stateless para access token).
- Mejor seguridad con expiracion corta y rotacion de refresh.
- Logout efectivo y trazabilidad de sesiones.

Trade-offs:

- Complejidad mayor que session-cookie clasica.
- Necesidad de proteger `JWT_SECRET` y endurecer politicas de contrasena.
