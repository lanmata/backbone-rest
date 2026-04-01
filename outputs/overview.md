# Project Overview — backbone-rest

> **Version:** 0.0.2  
> **Organisation:** PRX Dev Innova  
> **Author:** Luis Antonio Mata (luis.antonio.mata@gmail.com)  
> **License:** Proprietary — Luis Antonio Mata Mata. All rights reserved.

---

## 1. Executive Summary

**backbone-rest** is the central back-office REST service of the PRX platform. It provides a single, secured HTTP API that manages all system-level entities — users, roles, features, people, contacts, applications, sessions, documents, and profile images — on behalf of front-end products and other internal services.

The service acts as the authoritative source of record for identity and organisational structure across the PRX ecosystem. Every product that needs to know *who a user is*, *what roles they hold*, or *which application they belong to* queries backbone-rest.

---

## 2. Business Context

```
┌──────────────────────────────────────────────────────────┐
│                     PRX Ecosystem                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Web / SPA   │  │ Mobile Apps  │  │ Other µSvcs  │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         └─────────────────┼─────────────────┘            │
│                           │  REST / HTTPS                 │
│                           ▼                               │
│              ┌──────────────────────┐                     │
│              │    backbone-rest      │                     │
│              │   (this service)      │                     │
│              └──────────┬────────────┘                    │
│                         │                                  │
│           ┌─────────────┼─────────────┐                   │
│           ▼             ▼             ▼                    │
│       ┌────────┐  ┌──────────┐  ┌────────┐               │
│       │  PRX   │  │ Keycloak │  │ Vault  │               │
│       │  DB    │  │   IAM    │  │Secrets │               │
│       └────────┘  └──────────┘  └────────┘               │
└──────────────────────────────────────────────────────────┘
```

### Key Stakeholders

| Role | Interest |
|------|----------|
| **Product teams** | Consume user, role, and feature APIs to power their features |
| **Platform / DevOps** | Operate and deploy the service; manage secrets and certificates |
| **Security** | OAuth2 token validation, Keycloak integration, session management |
| **Data / BI** | Person and contact records that feed downstream reporting |

---

## 3. Business Capabilities

| Capability | Description |
|------------|-------------|
| **User Management** | Create, find, update, delete users; link/unlink roles; alias and email availability checks |
| **Session Management** | Authenticate by alias or email; issue, validate, and renew app-specific JWT session tokens |
| **Role Management** | Create and manage roles; assign to users per application |
| **Feature Management** | Create and manage application features; filter active/inactive |
| **Person Registry** | CRUD for personal data (name, birth date, gender) linked to users |
| **Contact Management** | Create/update/list contact records (phone, email, address) per person |
| **Contact Type Catalogue** | Manage the taxonomy of contact types |
| **Application Registry** | Register and manage client applications within the PRX platform |
| **Profile Image** | Upload, retrieve, and manage user profile photos |
| **Document / Report** | Process Word document templates with dynamic placeholder replacement |

---

## 4. API Surface (Summary)

All endpoints are under the base path `/api/v1/`.

| Domain | Base Path | Methods |
|--------|-----------|---------|
| Users | `/api/v1/users` | GET, POST, PUT, DELETE |
| Sessions | `/api/v1/sessions` | POST, GET |
| Roles | `/api/v1/roles` | GET, POST, PUT |
| Features | `/api/v1/features` | GET, POST, PUT |
| People | `/api/v1/people` | GET, POST, PUT |
| Contacts | `/api/v1/contacts` | GET, POST, PUT |
| Contact Types | `/api/v1/contact-types` | GET, POST, PUT |
| Applications | `/api/v1/applications` | POST |
| Profile Images | `/api/v1/profile-images` | GET, POST |
| Documents / Reports | `/api/v1/report` | GET |

See [api-reference.md](api-reference.md) for the full endpoint catalogue.

---

## 5. Non-Functional Characteristics

| Attribute | Value |
|-----------|-------|
| **Protocol** | HTTPS (TLSv1.3) |
| **Port** | 8082 (configurable via `APP_PORT`) |
| **Auth mechanism** | OAuth2 Bearer (Keycloak JWT) + optional `session-token` header |
| **Session type** | Stateless (no server-side HTTP sessions) |
| **Data format** | JSON (`application/json`) |
| **Deployment unit** | Docker container (`lamata/backbone-rest`) |
| **Runtime** | Java 21 on Amazon Corretto 21 Alpine |

---

## 6. Project Versioning

| Artefact | Version |
|----------|---------|
| Maven `pom.xml` | `0.0.2` |
| Docker label | `0.0.3` |
| Spring Boot | `3.4.1` |

Changes are tracked in [CHANGELOG](../CHANGELOG) at the repository root.

---

## 7. Repository Layout (top-level)

```
backbone-rest/
├── src/
│   ├── main/java/com/prx/backoffice/   <- Application source code
│   ├── main/resources/                  <- bootstrap.yml, keystore, OpenAPI spec
│   └── test/java/com/prx/backoffice/   <- Unit tests
├── Dockerfile                           <- Container build definition
├── pom.xml                              <- Maven build descriptor
├── ruleset.xml                          <- PMD code-quality ruleset
├── CHANGELOG                            <- Change history
├── .gitlab-ci.yml                       <- CI pipeline (SonarCloud + Qodana)
└── outputs/                             <- This documentation package
```

