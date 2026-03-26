package com.example.sdvnews.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ProtectedHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.AlgorithmParameters;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final Map<String, PublicKey> publicKeys;

    public JwtUtil(@Value("${supabase.jwks-url}") String jwksUrl) {
        this.publicKeys = fetchJwks(jwksUrl);
        log.info("Loaded {} key(s) from JWKS endpoint: {}", publicKeys.size(), jwksUrl);
    }

    private Map<String, PublicKey> fetchJwks(String jwksUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = new ObjectMapper().readTree(response.body());
            Map<String, PublicKey> keys = new HashMap<>();
            for (JsonNode keyNode : root.get("keys")) {
                String kty = keyNode.path("kty").asText();
                String kid = keyNode.path("kid").asText();
                if ("EC".equals(kty)) {
                    PublicKey key = buildEcPublicKey(
                            keyNode.path("crv").asText(),
                            keyNode.path("x").asText(),
                            keyNode.path("y").asText()
                    );
                    keys.put(kid, key);
                }
            }
            return keys;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch JWKS from " + jwksUrl, e);
        }
    }

    private PublicKey buildEcPublicKey(String crv, String x, String y) throws Exception {
        String stdName = "P-256".equals(crv) ? "secp256r1"
                : "P-384".equals(crv) ? "secp384r1"
                : "secp521r1";

        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] xBytes = decoder.decode(x);
        byte[] yBytes = decoder.decode(y);
        ECPoint point = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));

        AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
        params.init(new ECGenParameterSpec(stdName));
        ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

        return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(point, ecSpec));
    }

    public Optional<Claims> validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .keyLocator(header -> header instanceof ProtectedHeader ph
                            ? publicKeys.get(ph.getKeyId()) : null)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }
}
