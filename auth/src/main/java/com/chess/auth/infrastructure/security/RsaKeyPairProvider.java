package com.chess.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RsaKeyPairProvider {

    private static final String KEY_ID = "auth-key-1";
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public RsaKeyPairProvider() {
        this(null, null);
    }

    public RsaKeyPairProvider(
            @Value("${app.jwt.private-key:}") String configuredPrivateKey,
            @Value("${app.jwt.public-key:}") String configuredPublicKey
    ) {
        if (configuredPrivateKey != null && !configuredPrivateKey.isBlank() 
                && configuredPublicKey != null && !configuredPublicKey.isBlank()) {
            this.privateKey = loadPrivateKeyFromPem(configuredPrivateKey);
            this.publicKey = loadPublicKeyFromPem(configuredPublicKey);
        } else {
            KeyPair keyPair = loadOrGenerateLocalKeyPair();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
        }
    }

    private KeyPair loadOrGenerateLocalKeyPair() {
        try {
            Path keyDir = Path.of(".keys");
            Path privPath = keyDir.resolve("rsa_private.key");
            Path pubPath = keyDir.resolve("rsa_public.key");

            if (Files.exists(privPath) && Files.exists(pubPath)) {
                byte[] privBytes = Files.readAllBytes(privPath);
                byte[] pubBytes = Files.readAllBytes(pubPath);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
                RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(pubBytes));
                return new KeyPair(pubKey, privKey);
            }

            Files.createDirectories(keyDir);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            Files.write(privPath, keyPair.getPrivate().getEncoded());
            Files.write(pubPath, keyPair.getPublic().getEncoded());
            return keyPair;
        } catch (Exception e) {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                return keyPairGenerator.generateKeyPair();
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Failed to generate RSA key pair", ex);
            }
        }
    }

    private RSAPrivateKey loadPrivateKeyFromPem(String pem) {
        try {
            String clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] bytes = Base64.getDecoder().decode(clean);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configured RSA private key", e);
        }
    }

    private RSAPublicKey loadPublicKeyFromPem(String pem) {
        try {
            String clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] bytes = Base64.getDecoder().decode(clean);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configured RSA public key", e);
        }
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String getKeyId() {
        return KEY_ID;
    }

    public Map<String, Object> getJwksPayload() {
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", KEY_ID);
        jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray()));

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of(jwk));
        return jwks;
    }
}
