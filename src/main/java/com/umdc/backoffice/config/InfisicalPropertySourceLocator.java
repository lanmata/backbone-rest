package com.umdc.backoffice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umdc.backoffice.property.VaultProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class InfisicalPropertySourceLocator implements PropertySourceLocator {

    private static final Logger logger = LoggerFactory.getLogger(InfisicalPropertySourceLocator.class);
    private static final String PROPERTY_SOURCE_NAME = "infisical";
    private static final String PREFIX = "umdc.cloud.vault";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PropertySource<?> locate(Environment environment) {
        logger.info("[Infisical] locate() invoked — binding '{}' from environment", PREFIX);

        VaultProperties props = Binder.get((ConfigurableEnvironment) environment)
            .bind(PREFIX, VaultProperties.class)
            .orElseGet(VaultProperties::new);

        Boolean enabled = props.getEnabled();
        logger.info("[Infisical] enabled={} | url={} | projectId={} | environment={} | secretPath={}",
            enabled, props.getUrl(), props.getProjectId(), props.getEnvironment(), props.getSecretPath());

        if (!Boolean.TRUE.equals(enabled)) {
            logger.info("[Infisical] Skipping — umdc.cloud.vault.enabled is not 'true'");
            return null;
        }

        if (props.getClientId() == null || props.getClientSecret() == null || props.getProjectId() == null) {
            logger.error("[Infisical] Missing required credentials — clientId={} clientSecret={} projectId={}",
                props.getClientId() != null ? "SET" : "MISSING",
                props.getClientSecret() != null ? "SET" : "MISSING",
                props.getProjectId() != null ? "SET" : "MISSING");
            throw new IllegalStateException(
                "Infisical is enabled but umdc.cloud.vault.client-id, umdc.cloud.vault.client-secret, "
                    + "and umdc.cloud.vault.project-id must be set");
        }

        String siteUrl = props.getUrl() != null ? props.getUrl() : "https://app.infisical.com";
        String env = props.getEnvironment() != null ? props.getEnvironment() : "dev";
        String secretPath = props.getSecretPath() != null ? props.getSecretPath() : "/";

        try {
            HttpClient client = HttpClient.newHttpClient();
            logger.info("[Infisical] Authenticating — siteUrl={} clientId={}", siteUrl, props.getClientId());
            String accessToken = authenticate(client, siteUrl, props.getClientId(), props.getClientSecret());
            logger.info("[Infisical] Authentication successful — fetching secrets projectId={} env={} path={}",
                props.getProjectId(), env, secretPath);
            Map<String, Object> secrets = fetchSecrets(client, siteUrl, accessToken, props.getProjectId(), env, secretPath);
            logger.info("[Infisical] Loaded {} secrets: {}", secrets.size(), secrets.keySet());
            return new MapPropertySource(PROPERTY_SOURCE_NAME, secrets);
        } catch (Exception e) {
            logger.error("[Infisical] Failed to load secrets — {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new IllegalStateException("Failed to load secrets from Infisical", e);
        }
    }

    private String authenticate(HttpClient client, String siteUrl, String clientId, String clientSecret) throws Exception {
        String authUrl = siteUrl + "/api/v1/auth/universal-auth/login";
        logger.debug("[Infisical] POST {}", authUrl);
        String body = "{\"clientId\":\"" + clientId + "\",\"clientSecret\":\"" + clientSecret + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(authUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("[Infisical] Authentication response status={}", response.statusCode());
        if (response.statusCode() != 200) {
            logger.error("[Infisical] Authentication failed — status={} body={}", response.statusCode(), response.body());
            throw new IllegalStateException("Infisical authentication failed, status: " + response.statusCode()
                + " body: " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("accessToken").asText();
    }

    private Map<String, Object> fetchSecrets(HttpClient client, String siteUrl, String accessToken,
                                              String projectId, String env, String secretPath) throws Exception {
        String encodedPath = URLEncoder.encode(secretPath, StandardCharsets.UTF_8);
        String url = siteUrl + "/api/v3/secrets/raw"
            + "?workspaceId=" + projectId
            + "&environment=" + env
            + "&secretPath=" + encodedPath
            + "&include_imports=true";

        logger.debug("[Infisical] GET {}", url);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("[Infisical] Fetch secrets response status={}", response.statusCode());
        if (response.statusCode() != 200) {
            logger.error("[Infisical] Failed to fetch secrets — status={} body={}", response.statusCode(), response.body());
            throw new IllegalStateException("Failed to fetch secrets from Infisical, status: " + response.statusCode());
        }

        Map<String, Object> secrets = new HashMap<>();
        JsonNode json = objectMapper.readTree(response.body());
        JsonNode secretsNode = json.get("secrets");
        if (secretsNode != null && secretsNode.isArray()) {
            for (JsonNode secret : secretsNode) {
                secrets.put(secret.get("secretKey").asText(), secret.get("secretValue").asText());
            }
        }
        logger.debug("[Infisical] Parsed {} secrets from response", secrets.size());
        return secrets;
    }
}
