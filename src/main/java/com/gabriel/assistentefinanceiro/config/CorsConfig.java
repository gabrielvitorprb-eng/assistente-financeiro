package com.gabriel.assistentefinanceiro.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    private static final String LOCAL_FRONTEND_URL = "http://localhost:5173";
    private static final String VERCEL_FRONTEND_URL = "https://assistente-financeiro-azure.vercel.app";

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    @Bean
    CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(originsPermitidas());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }

    private List<String> originsPermitidas() {
        return List.of(LOCAL_FRONTEND_URL, VERCEL_FRONTEND_URL, normalizarOrigem(frontendUrl)).stream()
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();
    }

    private String normalizarOrigem(String origem) {
        if (origem == null) {
            return "";
        }
        String origemNormalizada = origem.trim();
        while (origemNormalizada.endsWith("/")) {
            origemNormalizada = origemNormalizada.substring(0, origemNormalizada.length() - 1);
        }
        return origemNormalizada;
    }
}
