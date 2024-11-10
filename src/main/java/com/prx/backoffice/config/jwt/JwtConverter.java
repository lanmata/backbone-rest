package com.prx.backoffice.config.jwt;

import com.prx.commons.exception.JwtConverterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converter class for converting a {@link Jwt} to an {@link AbstractAuthenticationToken}.
 * This class extracts roles and authorities from the JWT and creates an authentication token.
 */
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger LOGGER = LogManager.getLogger(JwtConverter.class);

    private static final String RESOURCE_ACCESS_STRING = "resource_access";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLE_KEY = "roles";

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private final JwtConverterProperties jwtAuthConverterProperties;

    /**
     * Constructor for JwtConverter.
     *
     * @param jwtConverterProperties the properties for JWT conversion
     */
    public JwtConverter(JwtConverterProperties jwtConverterProperties) {
        this.jwtAuthConverterProperties = jwtConverterProperties;
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    /**
     * Converts the given {@link Jwt} to an {@link AbstractAuthenticationToken}.
     *
     * @param jwt the JWT to convert
     * @return the authentication token
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorityCollection = null;
        try {
            authorityCollection = Stream.concat(
                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                    extractResourceRoles(jwt).stream()).collect(Collectors.toSet());
        } catch (JwtConverterException e) {
            LOGGER.error(" Error occurred during load the JWT granted authorization properties to instance the converter object", e);
        }
        return new JwtAuthenticationToken(jwt, authorityCollection, getPrincipalClaimName(jwt));
    }

    /**
     * Retrieves the principal claim name from the JWT.
     *
     * @param jwt the JWT
     * @return the principal claim name
     */
    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (Objects.nonNull(jwtAuthConverterProperties.getPrincipalClaimName())) {
            claimName = jwtAuthConverterProperties.getPrincipalClaimName();
        }
        return jwt.getClaim(claimName);
    }

    /**
     * Extracts resource roles from the JWT.
     *
     * @param jwt the JWT
     * @return a collection of granted authorities
     * @throws JwtConverterException if there is an error while loading the user roles and resources id
     */
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) throws JwtConverterException {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_STRING);
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (Objects.nonNull(jwtAuthConverterProperties.getResourceId())) {
            resource = (Map<String, Object>) resourceAccess.get(jwtAuthConverterProperties.getResourceId());
            resourceRoles = (Collection<String>) resource.get(ROLE_KEY);
            return resourceRoles.stream().map(role -> new SimpleGrantedAuthority(ROLE_PREFIX.concat(role))).collect(Collectors.toSet());
        }
        throw new JwtConverterException("Error while load the user roles and resources id.");
    }
}
