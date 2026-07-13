/*
 *  @(#)RedisConfig.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.config;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.net.URI;
import java.time.Duration;
/**
 * Source-aware Lettuce Redis connection configuration.
 *
 * <p>Resolves the Redis endpoint from either a single {@code spring.data.redis.url}
 * property (source = {@code "url"}) or discrete {@code spring.data.redis.*} properties
 * (source = {@code "properties"}) and builds the Lettuce connection factory accordingly.</p>
 *
 * <p>When the source is {@code "url"}, Lettuce's own {@link RedisURI} parser is used
 * so that Redis-specific URL semantics (TLS via {@code rediss://}, ACL credentials,
 * percent-encoded passwords, database index) are handled natively.
 * Supported URL schemes:</p>
 * <ul>
 *   <li>{@code redis://[user:password@]host[:port][/database]} — plain RESP</li>
 *   <li>{@code rediss://[user:password@]host[:port][/database]} — TLS</li>
 * </ul>
 *
 * <p>When the source is {@code "properties"}, the discrete
 * {@code spring.data.redis.host}, {@code .port}, {@code .username},
 * {@code .password}, {@code .database} and {@code .ssl.enabled} values are used
 * directly, so local development without a URL still works.</p>
 *
 * <p>The resolved endpoint is logged once at startup to make misconfiguration
 * immediately visible.</p>
 */
@Configuration
public class RedisConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);
    private static final int MAX_REDIS_DATABASE = 15;
    private static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofSeconds(5);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private final DataRedisProperties properties;
    public RedisConfig(DataRedisProperties properties) {
        this.properties = properties;
    }
    /**
     * Builds the {@link LettuceConnectionFactory} using the resolved connection source.
     *
     * When {@code source = "url"} the factory is constructed from Lettuce's own
     * {@link RedisURI}, preserving all URL-encoded credentials and TLS flags.
     * When {@code source = "properties"} the factory is constructed from discrete
     * {@link RedisStandaloneConfiguration} fields.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisEndpoint endpoint = resolveEndpoint();
        Duration timeout = properties.getTimeout() != null ?  Duration.ofSeconds(50): DEFAULT_COMMAND_TIMEOUT;
        LOGGER.info("Configuring Redis connection: host={}, port={}, database={}, ssl={}, username={}, source={}",
            endpoint.host, endpoint.port, endpoint.database, endpoint.sslEnabled,
            hasText(endpoint.username) ? endpoint.username : "(none)", endpoint.source);
        final RedisStandaloneConfiguration server;
        final LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder;
        if ("url".equals(endpoint.source)) {
            // Connect by source URL — delegate parsing to Lettuce's own RedisURI so that
            // Redis-specific semantics (percent-encoded passwords, rediss://, ACL user) are
            // handled correctly without manual java.net.URI decomposition.
            RedisURI redisURI = RedisURI.create(properties.getUrl().trim());
            server = new RedisStandaloneConfiguration(redisURI.getHost(), redisURI.getPort());
            server.setDatabase(redisURI.getDatabase());
            String urlUsername = extractUsernameFromUrl(properties.getUrl());
            if (hasText(urlUsername)) {
                server.setUsername(urlUsername);
            }
            String urlPassword = extractPasswordFromUrl(properties.getUrl());
            if (hasText(urlPassword)) {
                server.setPassword(RedisPassword.of(urlPassword));
            }
            clientBuilder = LettuceClientConfiguration.builder().commandTimeout(timeout);
            if (redisURI.isSsl()) {
                clientBuilder.useSsl();
            }
        } else {
            // Connect from discrete spring.data.redis.* properties
            server = new RedisStandaloneConfiguration(endpoint.host, endpoint.port);
            server.setDatabase(endpoint.database);
            if (hasText(endpoint.username)) {
                server.setUsername(endpoint.username);
            }
            if (hasText(endpoint.password)) {
                server.setPassword(RedisPassword.of(endpoint.password));
            }
            ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(io.lettuce.core.SocketOptions.builder()
                        .connectTimeout(timeout).build())
                .build();
            clientBuilder = LettuceClientConfiguration.builder()
                    .clientOptions(clientOptions)
                    .commandTimeout(timeout);
            if (endpoint.sslEnabled) {
                clientBuilder.useSsl();
            }
        }
        var lettuceConnectionFactory = new LettuceConnectionFactory(server, clientBuilder.build());
        lettuceConnectionFactory.setShareNativeConnection(false);
        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }
    private RedisEndpoint resolveEndpoint() {
        String url = properties.getUrl();
        if (hasText(url)) {
            return fromUrl(url);
        }
        return fromDiscreteProperties();
    }
    /**
     * Parses the Redis URL using Lettuce's {@link RedisURI} to produce a
     * loggable {@link RedisEndpoint} snapshot.
     */
    private RedisEndpoint fromUrl(String rawUrl) {
        RedisURI redisURI;
        try {
            redisURI = RedisURI.create(rawUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid spring.data.redis.url: " + rawUrl, ex);
        }
        String host = hasText(redisURI.getHost()) ? redisURI.getHost() : DEFAULT_HOST;
        int port = redisURI.getPort() > 0 ? redisURI.getPort() : DEFAULT_PORT;
        String username = extractUsernameFromUrl(rawUrl);
        String password = extractPasswordFromUrl(rawUrl);
        int database = clampDatabase(redisURI.getDatabase());
        boolean sslEnabled = redisURI.isSsl();
        return new RedisEndpoint(host, port, username, password, database, sslEnabled, "url");
    }
    private RedisEndpoint fromDiscreteProperties() {
        String host = hasText(properties.getHost()) ? properties.getHost() : DEFAULT_HOST;
        int port = properties.getPort() > 0 ? properties.getPort() : DEFAULT_PORT;
        int database = clampDatabase(properties.getDatabase());
        boolean sslEnabled = properties.getSsl().isEnabled();
        return new RedisEndpoint(host, port, properties.getUsername(), properties.getPassword(),
            database, sslEnabled, "properties");
    }
    private static String extractUsernameFromUrl(String rawUrl) {
        try {
            String userInfo = new URI(rawUrl.trim()).getUserInfo();
            if (userInfo != null) {
                return userInfo.split(":", 2)[0];
            }
        } catch (Exception ignored) {
            // RedisURI.create() has already validated the URL; silent fallback is safe
        }
        return null;
    }
    private static String extractPasswordFromUrl(String rawUrl) {
        try {
            String userInfo = new URI(rawUrl.trim()).getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                return userInfo.split(":", 2)[1];
            }
        } catch (Exception ignored) {
            // RedisURI.create() has already validated the URL; silent fallback is safe
        }
        return null;
    }
    private static int clampDatabase(int database) {
        if (database < 0 || database > MAX_REDIS_DATABASE) {
            LOGGER.warn("Configured Redis database index {} is out of range [0..{}]; defaulting to 0",
                database, MAX_REDIS_DATABASE);
            return 0;
        }
        return database;
    }
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
    private record RedisEndpoint(String host, int port, String username, String password,
                                 int database, boolean sslEnabled, String source) {
    }
}
