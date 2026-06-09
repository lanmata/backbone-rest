package com.umdc.backoffice.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bean de configuración para cargar propiedades secretas y credenciales desde umdc.cloud.vault.
 * Las propiedades deben estar configuradas con el prefijo "umdc.cloud.vault".
 */
@Component
@ConfigurationProperties(prefix = "umdc.cloud.vault")
public class VaultProperties {

    // Variables de instancia para mapear las propiedades del entorno y application.yml
    private String url;
    private String clientId;
    private String clientSecret;
    private String projectId;
    private String environment;
    private String secretPath;
    private Boolean enabled; // Usar Boolean si se espera true/false

    public VaultProperties() {
        // Default constructor
    }

    // --- Getters (Spring Boot provee setters, pero los getters son necesarios para acceder a la información) ---

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public void setSecretPath(String secretPath) {
        this.secretPath = secretPath;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
