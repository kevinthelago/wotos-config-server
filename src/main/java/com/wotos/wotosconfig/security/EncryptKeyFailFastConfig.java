package com.wotos.wotosconfig.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Refuses to start outside the dev/local profile when ENCRYPT_KEY is absent.
 *
 * <p>Without an encrypt key, {@code {cipher}} values in the backing config repo cannot
 * be decrypted before being served to clients, and the {@code /encrypt} endpoint is
 * unavailable — leaving every consuming service unable to bootstrap its secrets.
 *
 * <p>The check is bypassed when the {@code dev} or {@code local} Spring profile is
 * active so that local developer setups do not require a key.
 */
@Configuration
public class EncryptKeyFailFastConfig {

    private final Environment environment;

    public EncryptKeyFailFastConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void requireEncryptKeyOutsideDev() {
        boolean isDevOrLocal = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equals("dev") || p.equals("local"));
        String key = environment.getProperty("encrypt.key", "");
        if (!isDevOrLocal && key.isEmpty()) {
            throw new IllegalStateException(
                "ENCRYPT_KEY environment variable must be set in non-dev environments. " +
                "Provide a strong symmetric key via the environment variable, " +
                "or activate the 'dev' profile for local development."
            );
        }
    }
}
