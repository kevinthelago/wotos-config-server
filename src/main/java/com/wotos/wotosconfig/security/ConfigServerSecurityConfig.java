package com.wotos.wotosconfig.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Locks the config server's HTTP surface behind HTTP Basic authentication.
 *
 * <p>The config server serves every microservice's configuration — including any
 * decrypted {@code {cipher}} secrets — so its endpoints must not be world-readable.
 * All requests require authentication against the {@code spring.security.user.*}
 * credentials except the liveness probe, which is left open so orchestrators can
 * health-check the service without credentials.
 *
 * <p>CSRF protection is disabled because clients are non-browser services that
 * authenticate per-request with HTTP Basic; CSRF tokens would otherwise block their
 * POSTs to {@code /encrypt} and {@code /decrypt}.
 *
 * <p>NOTE: {@link WebSecurityConfigurerAdapter} is the idiomatic mechanism on this
 * Spring Security 5.1 (Spring Boot 2.1.x) baseline. The Phase 1 upgrade to Spring
 * Security 6 will require migrating this to a {@code SecurityFilterChain} bean.
 */
@Configuration
public class ConfigServerSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
