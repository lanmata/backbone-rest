#!/usr/bin/env sh
# postgres-mcp.sh — wrapper for the PostgreSQL MCP server.
# Reads all connection parameters from environment variables.
# Never hardcode host, port, credentials, or database name here.
#
# Required env-vars:
#   DB_HOST      — PostgreSQL host (e.g., aws-1-us-east-2.pooler.supabase.com)
#   DB_PORT      — PostgreSQL port (e.g., 6543 for Supabase pooler)
#   DB_NAME      — Database name (e.g., postgres)
#   DB_USER      — Database user
#   DB_PASSWORD  — Database password
#
# Optional env-vars (with defaults):
#   DB_SSLMODE   — SSL mode (default: require)

: "${DB_HOST:?DB_HOST is not set — set it to your PostgreSQL host}"
: "${DB_PORT:?DB_PORT is not set — set it to your PostgreSQL port (e.g. 6543)}"
: "${DB_NAME:?DB_NAME is not set — set it to your database name}"
: "${DB_USER:?DB_USER is not set — set it to your database username}"
: "${DB_PASSWORD:?DB_PASSWORD is not set — set it to your database password}"

DB_SSLMODE="${DB_SSLMODE:-require}"

CONNECTION_STRING="postgresql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=${DB_SSLMODE}"

exec npx -y @modelcontextprotocol/server-postgres "${CONNECTION_STRING}"
