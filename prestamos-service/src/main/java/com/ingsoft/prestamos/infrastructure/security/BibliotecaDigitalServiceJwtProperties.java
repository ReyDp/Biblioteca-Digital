package com.ingsoft.prestamos.infrastructure.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("biblioteca.digital.service-jwt")
public class BibliotecaDigitalServiceJwtProperties {
    private String privateKeyLocation;
    private String issuer = "prestamos-service";
    private String audience = "biblioteca-digital";
    private Duration lifetime = Duration.ofMinutes(5);
    public String getPrivateKeyLocation() { return privateKeyLocation; }
    public void setPrivateKeyLocation(String value) { this.privateKeyLocation = value; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String value) { this.issuer = value; }
    public String getAudience() { return audience; }
    public void setAudience(String value) { this.audience = value; }
    public Duration getLifetime() { return lifetime; }
    public void setLifetime(Duration value) { this.lifetime = value; }
}
