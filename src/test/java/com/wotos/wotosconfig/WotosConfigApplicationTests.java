package com.wotos.wotosconfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.security.user.name=tester",
    "spring.security.user.password=secret",
    // Point at a placeholder repo and disable the git-backed health indicator so
    // the context loads and the security tests run without any network access.
    "spring.cloud.config.server.git.uri=https://example.com/placeholder-config.git",
    "spring.cloud.config.server.health.enabled=false"
})
public class WotosConfigApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
    }

    @Test
    public void healthEndpointIsPublic() {
        ResponseEntity<String> response =
            restTemplate.getForEntity(url("/actuator/health"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void protectedEndpointRejectsAnonymousRequests() {
        ResponseEntity<String> response =
            restTemplate.getForEntity(url("/actuator/info"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void protectedEndpointAcceptsValidCredentials() {
        ResponseEntity<String> response = restTemplate
            .withBasicAuth("tester", "secret")
            .getForEntity(url("/actuator/info"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void protectedEndpointRejectsBadCredentials() {
        ResponseEntity<String> response = restTemplate
            .withBasicAuth("tester", "wrong-password")
            .getForEntity(url("/actuator/info"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
