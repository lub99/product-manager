package com.example.products.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec keySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(keySpec)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        OctetSequenceKey key = new OctetSequenceKey.Builder(
                secretKey.getBytes(StandardCharsets.UTF_8))
            .keyUse(KeyUse.SIGNATURE)
            .keyID("1")
            .algorithm(JWSAlgorithm.HS256)
            .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }
}
