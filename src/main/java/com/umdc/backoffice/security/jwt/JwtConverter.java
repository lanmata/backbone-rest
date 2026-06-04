//package com.umdc.backoffice.security.jwt;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtClaimNames;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * Converter class for converting a {@link Jwt} to an {@link AbstractAuthenticationToken}.
// * This class extracts roles and authorities from the JWT and creates an authentication token.
// */
//public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
//
//    private static final Logger LOGGER = LogManager.getLogger(JwtConverter.class);
//
//    private static final String ROLE_PREFIX = "ROLE_";
//    private static final String SUPABASE_ROLE_CLAIM = "role";
//    private static final String APP_METADATA_CLAIM = "app_metadata";
//    private static final String ROLES_KEY = "roles";
//
//    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
//
//    private final JwtConverterProperties jwtAuthConverterProperties;
//
//    /**
//     * Constructor for JwtConverter.
//     *
//     * @param jwtConverterProperties the properties for JWT conversion
//     */
//    public JwtConverter(JwtConverterProperties jwtConverterProperties) {
//        this.jwtAuthConverterProperties = jwtConverterProperties;
//        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        LOGGER.info("JwtConverter initialised — principalClaimName: {}",
//                jwtConverterProperties.getPrincipalClaimName());
//    }
//
//    /**
//     * Converts the given {@link Jwt} to an {@link AbstractAuthenticationToken}.
//     *
//     * @param jwt the JWT to convert
//     * @return the authentication token
//     */
//    @Override
//    public AbstractAuthenticationToken convert(Jwt jwt) {
//        LOGGER.debug("Converting JWT — subject: {}, issuer: {}, issued-at: {}, expires-at: {}",
//                jwt.getSubject(), jwt.getIssuer(), jwt.getIssuedAt(), jwt.getExpiresAt());
//
//        Collection<GrantedAuthority> standardAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt);
//        LOGGER.debug("Standard JWT authorities extracted: {}", standardAuthorities);
//
//        Collection<? extends GrantedAuthority> resourceRoles = extractResourceRoles(jwt);
//        LOGGER.debug("Resource roles extracted: {}", resourceRoles);
//
//        Collection<GrantedAuthority> authorityCollection = Stream.concat(
//                standardAuthorities.stream(),
//                resourceRoles.stream())
//            .collect(Collectors.toSet());
//
//        String principalName = getPrincipalClaimName(jwt);
//        LOGGER.debug("Final authority collection for principal '{}': {}", principalName, authorityCollection);
//
//        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorityCollection, principalName);
//        LOGGER.info("JWT authentication token created — principal: {}, authorities: {}",
//                principalName, authorityCollection);
//        return token;
//    }
//
//    /**
//     * Retrieves the principal claim name from the JWT.
//     *
//     * @param jwt the JWT
//     * @return the principal claim name
//     */
//    private String getPrincipalClaimName(Jwt jwt) {
//        String claimName = JwtClaimNames.SUB;
//        if (Objects.nonNull(jwtAuthConverterProperties.getPrincipalClaimName())) {
//            claimName = jwtAuthConverterProperties.getPrincipalClaimName();
//            LOGGER.debug("Using configured principalClaimName: {}", claimName);
//        } else {
//            LOGGER.debug("No principalClaimName configured, using default: {}", claimName);
//        }
//        String value = jwt.getClaim(claimName);
//        LOGGER.debug("Principal claim '{}' resolved to: {}", claimName, value);
//        return value;
//    }
//
//    /**
//     * Extracts roles from a Supabase JWT.
//     * Maps the {@code role} claim (e.g. {@code authenticated}, {@code service_role}) and any
//     * custom roles found under {@code app_metadata.roles} to Spring Security granted authorities.
//     *
//     * @param jwt the JWT
//     * @return a collection of granted authorities
//     */
//    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
//        Set<GrantedAuthority> authorities = new HashSet<>();
//
//        String role = jwt.getClaimAsString(SUPABASE_ROLE_CLAIM);
//        LOGGER.debug("Supabase '{}' claim value: {}", SUPABASE_ROLE_CLAIM, role);
//        if (Objects.nonNull(role) && !role.isBlank()) {
//            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase(Locale.getDefault()));
//            authorities.add(authority);
//            LOGGER.debug("Added authority from Supabase role claim: {}", authority);
//        } else {
//            LOGGER.debug("No Supabase role claim present or blank — skipping");
//        }
//
//        Map<String, Object> appMetadata = jwt.getClaim(APP_METADATA_CLAIM);
//        LOGGER.debug("'{}' claim present: {}", APP_METADATA_CLAIM, Objects.nonNull(appMetadata));
//        if (Objects.nonNull(appMetadata)) {
//            Object customRoles = appMetadata.get(ROLES_KEY);
//            LOGGER.debug("Custom roles under '{}.{}': {}", APP_METADATA_CLAIM, ROLES_KEY, customRoles);
//            if (customRoles instanceof Collection<?> roleList) {
//                roleList.stream()
//                    .filter(r -> r instanceof String)
//                    .map(r -> new SimpleGrantedAuthority(ROLE_PREFIX + ((String) r).toUpperCase(Locale.getDefault())))
//                    .peek(a -> LOGGER.debug("Added authority from app_metadata.roles: {}", a))
//                    .forEach(authorities::add);
//            } else {
//                LOGGER.debug("'{}.{}' is absent or not a Collection — skipping", APP_METADATA_CLAIM, ROLES_KEY);
//            }
//        }
//
//        LOGGER.debug("Total resource roles extracted: {}", authorities);
//        return authorities;
//    }
//}
