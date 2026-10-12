package com.umdc.backoffice.property;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** application.yml feeds these from SESSION_CLIENT_REQUIRED / SESSION_CLIENT_REQUIRED_APPLICATIONS (empty by default). */
class SessionClientPropertiesBindingTest {

    private static SessionClientProperties bind(Map<String, String> values) {
        return new Binder(new MapConfigurationPropertySource(values))
                .bind("umdc.security.session.client", SessionClientProperties.class).orElseGet(SessionClientProperties::new);
    }

    @Test
    void theDefaultsRequireNoClient() {
        SessionClientProperties properties = bind(Map.of("umdc.security.session.client.required", "false",
                "umdc.security.session.client.required-applications", ""));

        assertFalse(properties.isRequired());
        assertTrue(properties.getRequiredApplications().isEmpty());
        assertFalse(properties.isRequiredFor(UUID.randomUUID()));
        assertFalse(properties.isRequiredFor(null));
    }

    @Test
    void aCommaSeparatedListNamesTheApplicationsThatNeedAClient() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        SessionClientProperties properties = bind(Map.of(
                "umdc.security.session.client.required-applications", first + ", " + second));

        assertEquals(2, properties.getRequiredApplications().size());
        assertTrue(properties.isRequiredFor(first));
        assertTrue(properties.isRequiredFor(second));
        assertFalse(properties.isRequiredFor(UUID.randomUUID()));
        assertFalse(properties.isRequiredFor(null));
    }

    @Test
    void requiredAppliesToEveryApplication() {
        SessionClientProperties properties = bind(Map.of("umdc.security.session.client.required", "true"));

        assertTrue(properties.isRequiredFor(UUID.randomUUID()));
        assertTrue(properties.isRequiredFor(null));
    }
}
