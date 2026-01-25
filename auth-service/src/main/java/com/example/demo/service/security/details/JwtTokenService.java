package com.example.demo.service.security.details;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    private static final String TOKEN_ISSUER = "http://localhost:8080";
    private static final MacAlgorithm SIGNING_ALGORITHM = MacAlgorithm.HS256;
    
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.expiration.time.hours}")
    private Integer tokenExpirationHours;

    public String generateToken(Authentication authenticatedUser) {
        log.debug("Generating JWT token for user: {}", authenticatedUser.getName());
        
        Instant currentTime = Instant.now();
        Instant expirationTime = calculateExpirationTime(currentTime);
        
        // Build claims set with user information
        JwtClaimsSet claimsSet = buildClaimsSet(
            authenticatedUser.getName(),
            extractUserRoles(authenticatedUser),
            currentTime,
            expirationTime
        );
        
        // Encode JWT with claims
        JwtEncoderParameters parameters = createEncoderParameters(claimsSet);
        String token = jwtEncoder.encode(parameters).getTokenValue();
        
        log.info("JWT token generated successfully for user: {}", authenticatedUser.getName());
        return token;
    }
    
    private Instant calculateExpirationTime(Instant issuedAt) {
        return issuedAt.plus(tokenExpirationHours, ChronoUnit.HOURS);
    }
    
    private String extractUserRoles(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }
    
    private JwtClaimsSet buildClaimsSet(String username, String roles, Instant issuedAt, Instant expiresAt) {
        return JwtClaimsSet.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(username)
                .claim("scope", roles)
                .build();
    }
    
    private JwtEncoderParameters createEncoderParameters(JwtClaimsSet claims) {
        return JwtEncoderParameters.from(
            JwsHeader.with(SIGNING_ALGORITHM).build(), 
            claims
        );
    }

    public Long extractExpirationTime(String tokenValue) {
        Jwt decodedToken = jwtDecoder.decode(tokenValue);
        Instant expiration = (Instant) decodedToken.getClaim("exp");
        return expiration.toEpochMilli();
    }
    public Jwt decodeToken(String token) {
        log.info("Decoding JWT token...");
        log.debug("Token to decode: {}", token);
        
        try {
            Jwt jwt = jwtDecoder.decode(token);
            log.info("JWT token decoded successfully");
            log.info("Token subject: {}", jwt.getSubject());
            log.info("Token issuer: {}", jwt.getIssuer());
            log.info("Token expiration: {}", jwt.getExpiresAt());
            log.info("Token issued at: {}", jwt.getIssuedAt());
            log.debug("All token claims: {}", jwt.getClaims());
            
            return jwt;
        } catch (Exception e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            log.error("Decode exception details: ", e);
            throw e;
        }
    }

}