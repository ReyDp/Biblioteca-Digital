package com.ingsoft.biblioteca.config;

import com.ingsoft.biblioteca.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService userDetailsService
    ) throws Exception {

        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/catalogo",
                                "/libros/**",
                                "/css/**",
                                "/images/**",
                                "/login",
                                "/registro",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/**")
                        .denyAll()
                        .requestMatchers("/multas/**")
                        .hasRole("CLIENTE")
                        .requestMatchers("/perfil/**", "/prestamos/**")
                        .hasRole("CLIENTE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/catalogo", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/catalogo")
                        .permitAll()
                );

        return http.build();
    }

    @Configuration
    @ConditionalOnExpression("'${biblioteca.service-jwt.public-key-location:}'.trim().length() > 0")
    static class InternalApiSecurityConfig {
        @Value("${biblioteca.service-jwt.public-key-location}")
        private String publicKeyLocation;
        @Value("${biblioteca.service-jwt.issuer}")
        private String issuer;
        @Value("${biblioteca.service-jwt.audience}")
        private String audience;

        @Bean
        @Order(1)
        SecurityFilterChain internalApiSecurityFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/api/**")
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/api/clientes/*/estado-prestamo")
                            .hasAuthority("SCOPE_biblioteca:clientes:leer")
                            .requestMatchers("/api/libros/*/reservas-prestamo", "/api/libros/reservas-prestamo/**")
                            .hasAuthority("SCOPE_biblioteca:reservas:gestionar")
                            .anyRequest().denyAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
            return http.build();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(readPublicKey())
                    .signatureAlgorithm(SignatureAlgorithm.RS256).build();
            OAuth2TokenValidator<Jwt> standard = JwtValidators.createDefaultWithIssuer(issuer);
            decoder.setJwtValidator(token -> {
                OAuth2TokenValidatorResult base = standard.validate(token);
                if (base.hasErrors()) return base;
                if (!token.getAudience().contains(audience)) {
                    return OAuth2TokenValidatorResult.failure(new OAuth2Error("audience_invalid", "Audiencia JWT inválida.", null));
                }
                if (!"service".equals(token.getClaimAsString("token_type"))) {
                    return OAuth2TokenValidatorResult.failure(new OAuth2Error("token_type_invalid", "Solo se aceptan tokens de servicio.", null));
                }
                return OAuth2TokenValidatorResult.success();
            });
            return decoder;
        }

        private RSAPublicKey readPublicKey() {
            try {
                String pem = Files.readString(Path.of(publicKeyLocation))
                        .replaceAll("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\\s", "");
                return (RSAPublicKey) KeyFactory.getInstance("RSA")
                        .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
            } catch (Exception exception) {
                throw new IllegalStateException("No se pudo leer la clave pública JWT de servicios.", exception);
            }
        }
    }
}
