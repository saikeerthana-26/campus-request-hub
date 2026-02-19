package edu.isu.crh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Frontend origin
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // Allow common methods including preflight
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow headers your frontend sends
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        // If you use cookies/sessions, set true. For JWT in header, false is fine.
        config.setAllowCredentials(false);

        // Optional: expose auth header if needed
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
