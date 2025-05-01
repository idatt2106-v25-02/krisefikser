package stud.ntnu.krisefikser.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.config.FrontendConfig;

@TestConfiguration
public class TestConfig {

    @Bean
    public AuthenticationManager authenticationManager() {
        return new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    authentication.getAuthorities()
                );
            }
        };
    }

    @Bean
    public TurnstileService turnstileService() {
        return new TurnstileService() {
            @Override
            public boolean verify(String token) {
                return true; // Always return true for testing
            }
        };
    }

    @Bean
    public FrontendConfig frontendConfig() {
        return new FrontendConfig() {
            @Override
            public String getUrl() {
                return "http://localhost:3000";
            }
        };
    }
} 