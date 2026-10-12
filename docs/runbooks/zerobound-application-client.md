# ZeroBound — application, users and application client

> How ZeroBound is registered in backbone-rest so that **Zerobound Frontend** (Angular + BFF) can sign users in and
> **Zerobound REST** accepts only those sessions. The same steps apply to any other application.
> Model and rules: [`docs/api/session.md`](../api/session.md#-applications-users-and-application-clients).

| Piece | Value | Where it ends up |
|---|---|---|
| Application `zerobound` | its `id` (UUID) | BFF `BACKBONE_APPLICATION_ID` (`zerobound-rest/config/bff.env`) **and** zerobound-rest `ZEROBOUND_APPLICATION_ID` (must be the same id) |
| Roles of the application | e.g. `ZEROBOUND_ADMIN`, `ZEROBOUND_CUSTOMER` | assigned to each user in the application |
| Application users | administrators, customers, … | they sign in at Zerobound Frontend |
| Application client `zerobound-frontend` | `clientId` + `clientSecret` (shown once) | BFF `BACKBONE_CLIENT_ID` / `BACKBONE_CLIENT_SECRET` — **Vault / secret store, never a versioned file** |

All management calls need an administrator session of the backoffice (Stratum does every step from its UI:
*Applications → zerobound → Users / Managed clients*). With curl:

```bash
export HOST="https://<backbone-host>:8084"
export ADMIN_JWT="<session token of a backoffice administrator>"   # sent as Authorization: Bearer
```

## 1. Application

```bash
curl -k -s "$HOST/api/v1/applications" -H "Authorization: Bearer $ADMIN_JWT" | jq '.[] | select(.name=="zerobound")'
# missing? create it (roles and a first user can go in the same payload):
curl -k -s -X POST "$HOST/api/v1/applications" -H "Authorization: Bearer $ADMIN_JWT" -H "Content-Type: application/json" \
  -d '{"application":{"name":"zerobound","description":"ZeroBound","active":true}}'
export APP_ID="<id returned>"
```

## 2. Users of the application

Create each person as a user and link it to the application with a role (`docs/v1/03-user-management.md`,
`docs/v1/05-user-application-role.md`). A user that is not linked to `zerobound` gets `404` at login, whatever its password.

## 3. Application client of the front end

```bash
curl -k -s -X POST "$HOST/api/v1/managed-clients" -H "Authorization: Bearer $ADMIN_JWT" -H "Content-Type: application/json" \
  -d "{\"name\":\"zerobound-frontend\",\"description\":\"BFF of Zerobound Frontend\",\"applicationId\":\"$APP_ID\",\"scopes\":[\"session:manage\"],\"active\":true}"
# → { "clientId": "...", "clientSecret": "..." }   the secret is shown ONCE: store it in Vault now
```

The scope name is free-form; `session:manage` is the one the BFF asks for (`BACKBONE_CLIENT_SCOPES`).
Rotate with `POST /api/v1/managed-clients/{clientId}/rotate-secret` (the old secret keeps working `MCAM_ROTATION_GRACE_SECONDS`).

## 4. Make the client mandatory for ZeroBound

In backbone-rest's configuration (Vault / config server), add the application id:

```
SESSION_CLIENT_REQUIRED_APPLICATIONS=<APP_ID>[,<other application ids>]
```

Do this **after** the BFF has its credentials: from then on a login for `zerobound` without the client answers `401`.

## 5. Check

```bash
M2M=$(curl -k -s -X POST "$HOST/api/v1/managed-clients/token" -H "Content-Type: application/json" \
  -d "{\"clientId\":\"$CLIENT_ID\",\"clientSecret\":\"$CLIENT_SECRET\",\"scopes\":[\"session:manage\"]}" | jq -r .accessToken)

# with the client: 200 and tokens whose payload has "aid": "<APP_ID>"
curl -k -s -X POST "$HOST/api/v1/session/token" -H "Authorization: Bearer $M2M" -H "Content-Type: application/json" \
  -d "{\"email\":\"<user>\",\"password\":\"<password>\",\"applicationId\":\"$APP_ID\"}"

# without it (once step 4 is applied): 401          with the client of another application: 403
curl -k -s -o /dev/null -w '%{http_code}\n' -X POST "$HOST/api/v1/session/token" -H "Content-Type: application/json" \
  -d "{\"email\":\"<user>\",\"password\":\"<password>\",\"applicationId\":\"$APP_ID\"}"
```

## Order of deployment

1. backbone-rest with this version (tokens start carrying `aid`; nothing is mandatory yet).
2. Register the client (step 3) and give the BFF its credentials; deploy Zerobound Frontend + Zerobound REST together.
   Zerobound REST rejects sessions without `aid == ZEROBOUND_APPLICATION_ID`, so users sign in again once.
3. Step 4.
