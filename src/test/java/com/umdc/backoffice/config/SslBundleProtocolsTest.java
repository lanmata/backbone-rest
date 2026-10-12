package com.umdc.backoffice.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

/** With a named SSL bundle Spring Boot ignores server.ssl.enabled-protocols: TLS 1.3 must live in the bundle's options. */
class SslBundleProtocolsTest {

    private static Properties applicationYml() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource(Path.of("src/main/resources/application.yml")));
        return yaml.getObject();
    }

    @Test
    void theBundleEnforcesTls13() {
        assertThat(applicationYml().getProperty("spring.ssl.bundle.jks.backbone-rest-security.options.enabled-protocols"))
                .isEqualTo("TLSv1.3");
    }

    @Test
    void theIgnoredServerSslPropertyIsNotUsedAnyMore() {
        Properties yml = applicationYml();

        assertThat(yml.getProperty("server.ssl.bundle")).isEqualTo("backbone-rest-security");
        assertThat(yml.getProperty("server.ssl.enabled-protocols")).isNull();
    }
}
