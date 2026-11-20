/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.security;

import com.ingsoft.prestamos.infrastructure.adapter.output.http.BibliotecaDigitalHttpClient;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxPublisherProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class ServiceJwtTokenProvider
implements BibliotecaDigitalHttpClient.TokenProvider {
    private final OutboxPublisherProperties properties;
    private final Clock clock;

    public ServiceJwtTokenProvider(OutboxPublisherProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String token() {
        try {
            Instant now = Instant.now(this.clock);
            String header = this.base64("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
            String claims = this.base64("{\"iss\":\"" + this.properties.getJwtIssuer() + "\",\"aud\":\"" + this.properties.getJwtAudience() + "\",\"sub\":\"prestamos-service\",\"client_id\":\"prestamos-service\",\"token_type\":\"service\",\"scope\":\"biblioteca:clientes:leer biblioteca:reservas:gestionar\",\"iat\":" + now.getEpochSecond() + ",\"exp\":" + now.plus(this.properties.getJwtLifetime()).getEpochSecond() + ",\"jti\":\"" + String.valueOf(UUID.randomUUID()) + "\"}");
            String signed = header + "." + claims;
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(this.privateKey());
            signer.update(signed.getBytes(StandardCharsets.US_ASCII));
            return signed + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
        }
        catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar el JWT de servicio.", exception);
        }
    }

    private PrivateKey privateKey() throws Exception {
        String pem = Files.readString(Path.of(this.properties.getJwtPrivateKeyLocation(), new String[0])).replaceAll("-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\\s", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    private String base64(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}

