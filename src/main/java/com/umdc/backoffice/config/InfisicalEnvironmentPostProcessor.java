package com.umdc.backoffice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
//import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class InfisicalEnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "infisical";
    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String enabled = environment.getProperty("INFISICAL_ENABLED", "false");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }

        String siteUrl = environment.getProperty("INFISICAL_SITE_URL", "https://app.infisical.com");
        String clientId = environment.getProperty("INFISICAL_CLIENT_ID");
        String clientSecret = environment.getProperty("INFISICAL_CLIENT_SECRET");
        String projectId = environment.getProperty("INFISICAL_PROJECT_ID");
        String env = environment.getProperty("INFISICAL_ENVIRONMENT", "Development");
        String secretPath = environment.getProperty("INFISICAL_SECRET_PATH", "/");

        if (clientId == null || clientSecret == null || projectId == null) {
            throw new IllegalStateException(
                "Infisical is enabled but INFISICAL_CLIENT_ID, INFISICAL_CLIENT_SECRET, and INFISICAL_PROJECT_ID must be set");
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String accessToken = authenticate(client, siteUrl, clientId, clientSecret);
            Map<String, Object> secrets = fetchSecrets(client, siteUrl, accessToken, projectId, env, secretPath);
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, secrets));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load secrets from Infisical", e);
        }
    }

    private String authenticate(HttpClient client, String siteUrl, String clientId, String clientSecret) throws Exception {
        String body = "{\"clientId\":\"" + clientId + "\",\"clientSecret\":\"" + clientSecret + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(siteUrl + "/api/v1/auth/universal-auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Infisical authentication failed, status: " + response.statusCode());
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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
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
        return secrets;
    }
}
