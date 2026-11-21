/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.http.HttpMethod
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.config.http.SessionCreationPolicy
 *  org.springframework.security.core.GrantedAuthority
 *  org.springframework.security.core.authority.SimpleGrantedAuthority
 *  org.springframework.security.oauth2.core.OAuth2Error
 *  org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
 *  org.springframework.security.oauth2.jwt.Jwt
 *  org.springframework.security.oauth2.jwt.JwtDecoder
 *  org.springframework.security.oauth2.jwt.NimbusJwtDecoder
 *  org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
 *  org.springframework.security.web.SecurityFilterChain
 */
package com.ingsoft.prestamos.infrastructure.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnExpression(value="'${jwt.public-key-location:}'.trim().length() > 0 and '${jwt.issuer:}'.trim().length() > 0")
public class SecurityConfiguration {
    @Value(value="${jwt.public-key-location:}")
    private String publicKeyLocation;
    @Value(value="${jwt.issuer:}")
    private String issuer;
    @Value(value="${jwt.audience:prestamos-service}")
    private String audience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/actuator/health"})).permitAll().requestMatchers(new String[]{"/actuator/info"})).permitAll().requestMatchers(HttpMethod.POST, new String[]{"/api/prestamos"})).hasAuthority("SCOPE_prestamos:crear").requestMatchers(HttpMethod.POST, new String[]{"/api/prestamos/*/devolucion"})).hasAuthority("SCOPE_prestamos:devolver").anyRequest()).authenticated()).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(this.jwtDecoder()).jwtAuthenticationConverter((Converter)this.jwtAuthenticationConverter())));
        return (SecurityFilterChain)http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (this.publicKeyLocation == null || this.publicKeyLocation.isBlank()) {
            throw new IllegalStateException("jwt.public-key-location debe estar configurado para validar tokens JWT.");
        }
        if (this.issuer == null || this.issuer.isBlank()) {
            throw new IllegalStateException("jwt.issuer debe estar configurado para validar tokens JWT.");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey)((RSAPublicKey)this.readPublicKey())).build();
        var standardValidator = JwtValidators.createDefaultWithIssuer(this.issuer);
        decoder.setJwtValidator(token -> {
            OAuth2TokenValidatorResult standardResult = standardValidator.validate(token);
            if (standardResult.hasErrors()) {
                return standardResult;
            }
            if (!this.issuer.equals(token.getIssuer())) {
                return OAuth2TokenValidatorResult.failure((OAuth2Error[])new OAuth2Error[]{new OAuth2Error("issuer_invalid", "Issuer inv\u00e1lido: " + String.valueOf(token.getIssuer()), null)});
            }
            if (!token.getAudience().contains(this.audience)) {
                return OAuth2TokenValidatorResult.failure((OAuth2Error[])new OAuth2Error[]{new OAuth2Error("audience_invalid", "Audience inv\u00e1lido: " + String.valueOf(token.getAudience()), null)});
            }
            if (!"user".equals(token.getClaim("token_type"))) {
                return OAuth2TokenValidatorResult.failure((OAuth2Error[])new OAuth2Error[]{new OAuth2Error("token_type_invalid", "token_type debe ser 'user', no se aceptan tokens de servicio.", null)});
            }
            return OAuth2TokenValidatorResult.success();
        });
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter =
                jwt -> this.extractAuthorities(jwt);

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object scopeObj = jwt.getClaim("scope");
        if (scopeObj instanceof String) {
            String scope = (String)scopeObj;
            return List.of(scope.split(" ")).stream().map(s -> new SimpleGrantedAuthority("SCOPE_" + s)).collect(Collectors.toList());
        }
        if (scopeObj instanceof List<?> scopes) {
            return scopes.stream()
                    .map(Object::toString)
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private PublicKey readPublicKey() {
        try {
            String pem = Files.readString(Path.of(this.publicKeyLocation, new String[0])).replaceAll("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\\s", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(new X509EncodedKeySpec(decoded));
        }
        catch (Exception ex) {
            throw new IllegalStateException("No se pudo leer la clave p\u00fablica JWT.", ex);
        }
    }
}
