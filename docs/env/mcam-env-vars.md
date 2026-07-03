# MCAM Environment Variables

> All MCAM-specific environment variables for the Management Client Authentication Manager.

| Variable | Required | Default | Description |
|---|---|---|---|
| `MCAM_KEY_ALIAS` | **Yes** | — | Key alias in the PKCS12 keystore used to sign and verify M2M JWTs |
| `MCAM_KEYSTORE_LOCATION` | **Yes** | — | Path to PKCS12 keystore (`classpath:` or `file:` prefix) |
| `MCAM_KEYSTORE_PASSWORD` | **Yes** | — | Keystore password — inject from secrets manager, never hard-code |
| `MCAM_TOKEN_TTL_SECONDS` | No | `3600` | M2M access token TTL in seconds (max recommended: 86400) |
| `MCAM_ROTATION_GRACE_SECONDS` | No | `300` | Seconds the old secret remains valid after rotation (grace period) |
| `MCAM_RATE_LIMIT_RPM` | No | `60` | Maximum token issuance requests per minute per `clientId` |
| `MCAM_MAINTENANCE_INTERVAL_MS` | No | `600000` | Interval (ms) for the scheduled `prevSecretHash` cleanup task |

## Notes

- `MCAM_KEYSTORE_PASSWORD` must be injected at runtime (e.g., Vault, Kubernetes Secret, or AWS SSM).  
  **Never commit its value to version control.**
- The keystore referenced by `MCAM_KEYSTORE_LOCATION` must contain an RSA key pair for RS256 signing.
- Reduce `MCAM_TOKEN_TTL_SECONDS` for high-security environments; increase `MCAM_ROTATION_GRACE_SECONDS`  
  if your deployment pipeline requires more than 5 minutes to distribute the new secret.
