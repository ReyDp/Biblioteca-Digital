package com.ingsoft.prestamos.infrastructure.security;

import com.ingsoft.prestamos.infrastructure.adapter.output.http.BibliotecaDigitalHttpClient;
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

/** Token limitado a los permisos internos de Biblioteca Digital. */
public class BibliotecaDigitalServiceJwtTokenProvider implements BibliotecaDigitalHttpClient.TokenProvider {
    private final BibliotecaDigitalServiceJwtProperties properties;
    private final Clock clock;
    public BibliotecaDigitalServiceJwtTokenProvider(BibliotecaDigitalServiceJwtProperties properties, Clock clock) { this.properties = properties; this.clock = clock; }
    @Override public String token() {
        try {
            Instant now = Instant.now(clock);
            String header = encoded("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
            String claims = encoded("{\"iss\":\"" + properties.getIssuer() + "\",\"aud\":\"" + properties.getAudience() + "\",\"sub\":\"prestamos-service\",\"client_id\":\"prestamos-service\",\"token_type\":\"service\",\"scope\":\"biblioteca:clientes:leer biblioteca:reservas:gestionar\",\"iat\":" + now.getEpochSecond() + ",\"exp\":" + now.plus(properties.getLifetime()).getEpochSecond() + ",\"jti\":\"" + UUID.randomUUID() + "\"}");
            String signed = header + "." + claims;
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey()); signer.update(signed.getBytes(StandardCharsets.US_ASCII));
            return signed + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
        } catch (Exception exception) { throw new IllegalStateException("No se pudo firmar el JWT para Biblioteca Digital.", exception); }
    }
    private PrivateKey privateKey() throws Exception {
        String pem = Files.readString(Path.of(properties.getPrivateKeyLocation())).replaceAll("-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\\s", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }
    private String encoded(String value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
}
