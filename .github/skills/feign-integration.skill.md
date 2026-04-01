---
name: Feign Integration
description: Skill for OpenFeign client patterns for inter-service communication
applies-to:
  - Developer
---

# Feign Integration Skill

## Scope

This skill covers OpenFeign client patterns for inter-service HTTP communication
in the **backbone-rest** project. `spring-cloud-starter-openfeign` is present as a
dependency and `@FeignClient` is declared on the main application class, but no
concrete Feign clients are currently implemented in this repo.

## Infrastructure Available

- `@FeignClient` scanning enabled via `PrxBackofficeRestApplication`
- `spring-cloud-starter-openfeign` (version 4.2.0) in `pom.xml`
- `spring-cloud-starter-config` and Vault for config/secret injection

## Implementing a New Feign Client

Follow this pattern when adding a client to call another PRX service:

```java
// 1. Create interface in com.prx.backoffice.v1.<domain>.client
@FeignClient(name = "target-service", url = "${services.target.url}")
public interface TargetServiceClient {
    @GetMapping("/api/v1/resource/{id}")
    ResponseEntity<ResourceTO> findById(@PathVariable UUID id);
}
```

```java
// 2. Configure request interceptor for auth header injection if needed
@Component
public class TargetServiceFeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // inject token from SecurityContextHolder or config
    }
}
```

```yaml
# 3. Add URL to bootstrap.yml
services:
  target:
    url: ${TARGET_SERVICE_URL}
```

## Testing Feign Clients

Mock the Feign interface in unit tests — do not call real services:
```java
@Mock TargetServiceClient targetServiceClient;
when(targetServiceClient.findById(any())).thenReturn(ResponseEntity.ok(mockTO));
```

## Notes

- Never hardcode service URLs — use `${ENV_VAR}` references in `bootstrap.yml`.
- Feign clients should be in their own package under the consuming domain module.
- Use `@CircuitBreaker` if resilience is needed (Spring Cloud Circuit Breaker is available via Spring Cloud dependencies).
