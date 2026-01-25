package com.example.demo.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@Slf4j
public class JwtConfig {

    @Value("${jwt.key}")
    private String jwtKey;

    @Bean
    public JwtEncoder jwtEncoder() {
        log.info("Configuring JWT Encoder with key length: {}", jwtKey.length());
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey.getBytes()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        log.info("Configuring JWT Decoder with key length: {}", jwtKey.length());
        byte[] bytes = jwtKey.getBytes();
        SecretKeySpec originalKey = new SecretKeySpec(bytes, 0, bytes.length, "HmacSHA256");
        log.info("SecretKeySpec created with algorithm: HmacSHA256");
        
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(originalKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        
        log.info("JWT Decoder configured successfully with HS256 algorithm");
        return decoder;
    }
}