# Roadmap de Desarrollo — backbone-rest
### Plan de acción para retomar y completar el desarrollo

---

| Campo | Valor |
|---|---|
| **Generado** | 2026-09-20 |
| **Cerrado** | 2026-09-21 |
| **Rama de referencia** | `bug/role-link-application` |
| **Basado en** | Auditoría de repo, `git log`, `mvn test`/`pmd:check`/JaCoCo, Dependabot API, inspección de los 18 módulos de dominio |
| **Estado** | ✅ **Completo** — Fases 0–4 ejecutadas y mergeadas; Fase 5 documentada (5.1/5.2), 5.3/5.4 quedan como decisión operativa del owner (ver sección 6) |

## Resumen de ejecución (PRs)

| Fase | PR | Estado |
|---|---|---|
| 0 — Cerrar lo en vuelo | #59 | Mergeado |
| 1 — Higiene de repo | #60, #61 | Mergeados |
| 2 — Deuda técnica (SEC/ARCH) | #62 | Mergeado |
| 4.1 — Refactor `report` | #63 | Mergeado |
| 4.2 — Completar `api.yaml` | #64 | Abierto, checks en verde |
| 3 — Cobertura `iam` | #65 | Abierto, checks en verde |
| 5 — Proceso de seguridad | (este commit, `CLAUDE.md`) | Documentado |

JaCoCo global pasó de **58% → 62%** a lo largo de este ciclo. Los 39 alertas de Dependabot abiertas al inicio quedaron en **0**.

---

## 0. Punto de partida (resumen, no repetir el detalle ya discutido)

- `develop` está sano: 616 tests, 0 violaciones PMD, JaCoCo 58% de cobertura global.
- La rama `bug/role-link-application` tiene el fix de Role↔Application↔Feature commiteado y pusheado; el `pom.xml` con la remediación de las 39 alertas de Dependabot está **listo pero sin commitear**.
- MCAM (M2M auth) está **completo** — CRUD, tokens, rotación, Redis, auditoría, tests, todo presente.
- El proyecto tiene **18 módulos de dominio** en `v1/`, no los 10 que documenta `CLAUDE.md` (`addresses`, `iam`, `identificationdocuments`, `managedclient`, `notices`, `noticetypes`, `rolefeatures`, `servicetype` faltan en el doc).
- El almacenamiento de imágenes de perfil **sí usa Cloudflare R2** vía el cliente externo `com.umdc.commons.services.cloudflare.r2.client.CloudflareR2StorageClient` (corrijo algo que dije mal antes: no es una feature fantasma, está integrada y testeada — solo que la clase vive en la librería `commons-services`, no en este repo).

---

## 1. Fase 0 — Cerrar lo que está en vuelo (esta semana) ✅

| # | Tarea | Detalle |
|---|---|---|
| 0.1 | Decidir y commitear la remediación de seguridad (`pom.xml`) | Ya validada (tests + PMD + `mvn package` en verde). Definir si va en `bug/role-link-application` o en una rama/PR `security/dependabot-remediation` separada — recomendado **separarla**, porque es un cambio de infraestructura sin relación con el fix de roles y así el reviewer no mezcla contextos. |
| 0.2 | Abrir PR de `bug/role-link-application` → `develop` | Ya tiene todo verde; no bloquear por lo de seguridad si se separa. |
| 0.3 | Abrir PR de la remediación de seguridad | Referenciar los GHSA IDs cerrados en la descripción para que Dependabot los reconcilie al mergear. |
| 0.4 | Confirmar la rama remota `dependabot/maven/maven-55f3996f77` | Puede solaparse con lo que ya arreglaste a mano — revisar y cerrar si queda redundante. |

**Resultado:** 0.3 no se separó como se recomendó (terminó bundleada en el mismo PR #59 que 0.2, ya que así llegó commiteada) — funcionalmente resuelto igual: 39→0 alertas Dependabot. El PR #58 (la rama de 0.4) se auto-cerró al mergear #59, quedó superado.

---

## 2. Fase 1 — Higiene de repo y documentación (1–2 días) ✅

| # | Tarea | Por qué |
|---|---|---|
| 1.1 | Actualizar la lista de "Domain Modules" en `CLAUDE.md` | Están documentados 10 de 18 módulos reales. Cualquier agente (`java-developer`, `api-designer`, etc.) que confíe en ese doc está trabajando con un mapa incompleto. |
| 1.2 | Decidir destino de `docs/plans/`, `docs/prompts/`, `docs/requirements/`, `docs/analysis_summary.md`, `docs/report-improve-action.md` | Son documentación real y útil (igual que este archivo) pero siguen sin trackear. Recomendado: trackearlos bajo `docs/` como memoria institucional del proyecto. |
| 1.3 | Resolver `.DS_Store` (7 ubicaciones), `.ai/`, `.codegraph/`, `.continue/config.json`, `prod-ca-2021.crt` | Quedaron pendientes de una sesión anterior. Mínimo: `.gitignore` para `.DS_Store` y `*.crt`; decidir si `.ai/`, `.codegraph/`, `.continue/` son tooling personal (ignorar) o config de equipo (trackear). |
| 1.4 | Revisar `memory/` dentro del repo | Es memoria de un agente viviendo en el árbol de trabajo — normalmente no debería vivir en un repo compartido. Moverla fuera o excluirla. |

**Resultado:** completo (PRs #60, #61). `feedback_response_format.md` (el único archivo realmente ajeno al repo) se migró a la memoria real del agente y se borró de aquí; `MEMORY.md`/`feedback_docs_conventions.md` ya estaban trackeados de antes y se dejaron intactos (fuera del alcance de lo pedido).

---

## 3. Fase 2 — Deuda técnica ya identificada, aún sin ejecutar ✅

Esto venía de `docs/report-improve-action.md` (jun/2024). Al ejecutar, 3 de los 5 hallazgos resultaron distintos a lo que decía el reporte:

| ID | Módulo | Acción original propuesta | Resultado real (PR #62, salvo PERF-01) |
|---|---|---|---|
| SEC-01 | `session` | Job `@Scheduled` para purgar JTI expirados | **Ya resuelto** — `JtiDenyListServiceImpl` usa Redis con TTL nativo; el job propuesto habría sido redundante. No se tocó código. |
| SEC-02 | `security` | Documentar/verificar rotación de `APP_TOKEN_SECRET` vía Vault | Verificado: sin fallback hardcodeado, viene de Vault. Documentado en `CLAUDE.md`. El runbook de rotación en sí queda fuera del código (ver 5.3/5.4 abajo). |
| PERF-01 | `users` | `@EntityGraph` en `UserRepository.loadUserAlias` | El N+1 real estaba en `UserRepository.findUserInfo` (no en un método `loadUserAlias` del repo — ese es privado en `SessionServiceImpl`). Corregido **en el repo hermano `persistence`** (`applicationRoleUser` es `@OneToMany(EAGER)` sin `JOIN FETCH`, más `role`/`application` `@ManyToOne(LAZY)`). Cambio uncommitted ahí, mezclado con WIP del owner — pendiente de que él lo commitee. |
| ARCH-01 | `session` | Descomponer `SessionServiceImpl` | Hecho — extraída `SessionTokenServiceImpl` (mecánica JWT pura). `mvn pmd:check` pasa **sin** el `@SuppressWarnings("PMD.GodClass")`. |
| ARCH-02 | general | `@LogDefault` uniforme en todos los `*ServiceImpl` | **La anotación estaba muerta** — 2021, cero adopción, ningún `@Aspect` la procesaba. Aplicarla habría sido puro ruido decorativo; se eliminó en vez de propagarla. |

---

## 4. Fase 3 — Cobertura de tests: los puntos ciegos reales ✅

JaCoCo global marcaba 58% al inicio; el promedio escondía módulos con cobertura casi nula.

| Módulo | Resultado |
|---|---|
| `report` | `DocumentServiceImplTest` + `DocumentControllerTest` nuevos (PR #63) |
| `rolefeatures` | `RoleFeatureLinkServiceTest` nuevo (rama `bug/role-link-application`, PR #59) |
| `iam` | `AuditController` 0%→71%, `AuditEventMapper` 0%→100%, `PermissionCheckController` 0%→56%, `TokenIntrospectController` 0%→56% (PR #65) |

JaCoCo global terminó en **62%**.

---

## 5. Fase 4 — Completar/normalizar módulos existentes ✅

| # | Módulo | Resultado |
|---|---|---|
| 4.1 | `report` | `DocumentApi.java` extraído (interface-first), endpoints migrados a `POST`. Además de la convención, el refactor destapó y corrigió **3 bugs reales**: ruta de escritura hardcodeada de Windows (el endpoint nunca pudo haber funcionado en el contenedor Linux), un `404` inalcanzable en `findPlaceholderValues` por un filtro faltante, y un **path traversal (CWE-22)** que CodeQL/SonarCloud marcaron al tocar esas líneas — neutralizado con saneamiento de nombre + verificación de contención. (PR #63) |
| 4.2 | `report`, `addresses`, `notices`, `noticetypes`, `identificationdocuments` | 12 paths + 8 schemas nuevos en `api.yaml`, transcritos directo de cada `*Api.java`/TO. (PR #64) |
| 4.3 | `managedclient` (MCAM) | Confirmado completo — no requirió trabajo adicional. |

---

## 6. Fase 5 — Seguridad continua (proceso, no solo el parche puntual)

| # | Tarea | Estado |
|---|---|---|
| 5.1 | Establecer cadencia de revisión de Dependabot | ✅ Documentado en `CLAUDE.md` § Dependency Security. |
| 5.2 | Documentar el patrón de overrides de versión | ✅ Documentado en `CLAUDE.md` § Dependency Security, con referencia a los 7 overrides ya aplicados en `pom.xml`. |
| 5.3 | Rotar el `VAULT_TOKEN` expuesto en `default.env` durante la sesión | ⬜ **Pendiente — acción del owner.** Requiere acceso a Vault que el agente no tiene. El archivo nunca llegó a git (está en `.gitignore`), pero como quedó en texto plano en este entorno, rotar por higiene sigue siendo recomendable. |
| 5.4 | Confirmar si hay push directo a `develop` sin PR | ⬜ **Pendiente — decisión del owner.** `develop` sí tiene protección de rama (confirmado: un intento de `git push` directo fue rechazado por GitHub durante este ciclo, "Changes must be made through a pull request"), así que el mecanismo ya existe. Lo que falta confirmar es una política explícita para cuando el equipo crezca (revisores obligatorios, checks requeridos más allá de CodeQL/SonarCloud). |

---

## 7. Qué queda abierto después de este roadmap

1. **Mergear PRs #64 y #65** (checks en verde, sin acción de código pendiente).
2. **PERF-01 en el repo `persistence`**: el fix de `UserRepository.findUserInfo` quedó aplicado pero sin commitear, mezclado con WIP propio del owner en la rama `ds-91-...`. Cuando esa rama se publique, hay que bumpear `com.umdc.persistence.version` en el `pom.xml` de `backbone-rest` para que el fix llegue aquí.
3. **5.3** — rotar el `VAULT_TOKEN` (acción operativa, requiere acceso a Vault).
4. **5.4** — decidir política de revisores/checks obligatorios en `develop` si el equipo crece (la protección de rama en sí ya existe).
5. Limpieza de repo que quedó deliberadamente sin decidir en Fase 1: `.DS_Store` (7 ubicaciones aún sin gitignore global si no se hizo), `.idea/`, `.opencode/`, `prod-ca-2021.crt` — ninguno bloquea nada, quedan a discreción del owner.

---

*Este documento se actualizó a mano tras cerrar el ciclo completo (Fases 0–5) el 2026-09-21. Si se retoma el desarrollo más adelante, tratar esta versión como archivo histórico y abrir un roadmap nuevo para el siguiente ciclo de trabajo.*
