#!/usr/bin/env bash
# bootstrap-network.sh — Create (or repair) the shared PRX Docker network
# Usage: ./scripts/bootstrap-network.sh
# Must be executed BEFORE starting any PRX containers (backbone-rest, config-server, etc.)
# Safe to re-run: idempotent. If prx-net exists with the wrong subnet it will be recreated.
set -euo pipefail

NETWORK_NAME="prx-net"
SUBNET="172.29.0.0/16"
GATEWAY="172.29.0.1"

needs_create=false
needs_recreate=false

if docker network inspect "${NETWORK_NAME}" >/dev/null 2>&1; then
  # Network exists — verify it has the expected subnet
  existing_subnet=$(docker network inspect "${NETWORK_NAME}" \
    --format '{{range .IPAM.Config}}{{.Subnet}}{{end}}' 2>/dev/null || true)

  if [ "${existing_subnet}" = "${SUBNET}" ]; then
    echo "[INFO] Network '${NETWORK_NAME}' already exists with correct subnet (${SUBNET}) — nothing to do."
    exit 0
  else
    echo "[WARN] Network '${NETWORK_NAME}' exists but has wrong subnet: '${existing_subnet}' (expected ${SUBNET})."
    needs_recreate=true
  fi
else
  needs_create=true
fi

if [ "${needs_recreate}" = "true" ]; then
  echo "[INFO] Disconnecting all containers from '${NETWORK_NAME}' ..."
  connected=$(docker network inspect "${NETWORK_NAME}" \
    --format '{{range $k,$v := .Containers}}{{$k}} {{end}}' 2>/dev/null || true)
  for container_id in ${connected}; do
    echo "  [INFO] Disconnecting container ${container_id} ..."
    docker network disconnect --force "${NETWORK_NAME}" "${container_id}" || true
  done

  echo "[INFO] Removing network '${NETWORK_NAME}' ..."
  docker network rm "${NETWORK_NAME}"
  needs_create=true
fi

if [ "${needs_create}" = "true" ]; then
  echo "[INFO] Creating Docker network '${NETWORK_NAME}' (${SUBNET}, gw ${GATEWAY}) ..."
  docker network create \
    --driver bridge \
    --subnet "${SUBNET}" \
    --gateway "${GATEWAY}" \
    "${NETWORK_NAME}"
  echo "[OK] Network '${NETWORK_NAME}' created successfully."
fi

