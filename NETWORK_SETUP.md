# PRX Docker Network Setup Guide

## Problem

The `backbone-rest` container and the `prx-qa-config-server` container run on the **same Docker host**.
By default each Compose project creates its own isolated bridge network, so containers in different
projects cannot reach each other by IP or hostname even on the same machine. This is why:

- Other devices **can** reach `https://prx-qa.config-server.tst` (they use the host external DNS/IP)
- The `backbone-rest` container **cannot** (trapped in its own isolated bridge; the hardcoded IP
  `172.29.0.2` in `extra_hosts` is unreachable because config-server is on a different Docker network)

Symptom in the logs:

```
Connect to https://prx-qa.config-server.tst:443 failed: Connect timed out
```

---

## Root Cause

The original `docker-compose.yml` created `prx-net` as a **new, isolated** bridge network:

```yaml
# BEFORE (broken)
networks:
  prx-net:
    driver: bridge   # creates a fresh isolated network -- config-server is NOT on it
```

The config-server container lives on a **different** Docker network (its own Compose project),
so `172.29.0.2` is never assigned to the config-server from backbone-rest's perspective.

---

## Solution: Shared External Docker Network

Both containers must join the **same** Docker bridge network (`prx-net`).
The network is declared `external: true` in each project's `docker-compose.yml` -- it is created
once on the host and both stacks join it.

```
Host
  Docker network: prx-net (172.29.0.0/16)
    config-server container  -> 172.29.0.2  (pinned via ipv4_address in config-server compose)
    backbone-rest container  -> 172.29.0.x  (dynamic, but on same subnet)
```

Docker DNS + the `extra_hosts` override both resolve `prx-qa.config-server.tst -> 172.29.0.2`
correctly from inside `backbone-rest`.

---

## Step-by-Step Setup

### 1. Create the shared network (once per host)

Run the idempotent helper script **before** starting any PRX container:

```bash
chmod +x ./scripts/bootstrap-network.sh
./scripts/bootstrap-network.sh
```

Or manually:

```bash
docker network create \
  --driver bridge \
  --subnet 172.29.0.0/16 \
  --gateway 172.29.0.1 \
  prx-net
```

Verify:

```bash
docker network inspect prx-net
# look for: "Subnet": "172.29.0.0/16"
```

### 2. backbone-rest (this repo) -- already configured

`docker-compose.yml` now declares:

```yaml
networks:
  prx-net:
    external: true
    name: prx-net
```

And `extra_hosts` pins:

```yaml
extra_hosts:
  - "prx-qa.config-server.tst:172.29.0.2"
```

No further changes needed in this repo.

### 3. config-server repo -- required change

In the config-server project's `docker-compose.yml`, add:

```yaml
services:
  prx-qa-config-server:           # use the actual service name
    networks:
      prx-net:
        ipv4_address: 172.29.0.2  # pin the IP that backbone-rest expects

networks:
  prx-net:
    external: true
    name: prx-net
```

`ipv4_address: 172.29.0.2` ensures config-server always gets the exact IP that backbone-rest resolves.

### 4. Startup order

```bash
# Step 1: Create shared network (idempotent -- safe to run multiple times)
./scripts/bootstrap-network.sh

# Step 2: Start config-server first (backbone-rest reads config from it at startup)
cd /path/to/config-server && docker compose up -d

# Step 3: Start backbone-rest
cd /path/to/backbone-rest && docker compose up -d
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `network prx-net declared as external, but could not be found` | Network not created | Run `./scripts/bootstrap-network.sh` |
| `Connect timed out` to config-server | config-server not on `prx-net` or IP mismatch | Confirm config-server uses `ipv4_address: 172.29.0.2` on `prx-net` |
| `Name or service not known` | `extra_hosts` missing or typo | Check `extra_hosts` in `docker-compose.yml` |
| SSL error after connection succeeds | CA cert not trusted | Verify `prx-local-ca.crt` imported into JVM cacerts (Dockerfile does this) |

---

## Related Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Declares `prx-net` as external; sets `extra_hosts` |
| `scripts/bootstrap-network.sh` | One-time host network creation -- idempotent |
| `src/main/resources/bootstrap.yml` | Reads `CNFS_URI` / `CNFS_PORT` env vars |
| `src/main/resources/prx-truststore.jks` | PRX Internal CA truststore for `*.tst` HTTPS |
