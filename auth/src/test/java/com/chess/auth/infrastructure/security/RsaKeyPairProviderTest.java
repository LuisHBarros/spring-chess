package com.chess.auth.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RsaKeyPairProviderTest {

    @Test
    @DisplayName("Should initialize with default local key pair when configured keys are empty")
    void shouldInitializeWithDefaultKeyPair() {
        RsaKeyPairProvider provider = new RsaKeyPairProvider();

        assertNotNull(provider.getPrivateKey());
        assertNotNull(provider.getPublicKey());
        assertEquals("auth-key-1", provider.getKeyId());

        Map<String, Object> jwks = provider.getJwksPayload();
        assertNotNull(jwks);
        assertTrue(jwks.containsKey("keys"));
    }

    @Test
    @DisplayName("Should load RSA key pair from valid PEM strings")
    void shouldLoadKeyPairFromPemStrings() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        String privPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        String pubPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        RsaKeyPairProvider provider = new RsaKeyPairProvider(privPem, pubPem);

        assertNotNull(provider.getPrivateKey());
        assertNotNull(provider.getPublicKey());
        assertEquals(keyPair.getPublic().getAlgorithm(), provider.getPublicKey().getAlgorithm());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when invalid PEM private key is provided")
    void shouldThrowOnInvalidPrivateKeyPem() {
        String invalidPem = "-----BEGIN PRIVATE KEY-----\ninvalid_base64_content\n-----END PRIVATE KEY-----";
        String dummyPubPem = "-----BEGIN PUBLIC KEY-----\nAAA=\n-----END PUBLIC KEY-----";

        assertThrows(IllegalArgumentException.class, () -> new RsaKeyPairProvider(invalidPem, dummyPubPem));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when invalid PEM public key is provided")
    void shouldThrowOnInvalidPublicKeyPem() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        String validPrivPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        String invalidPubPem = "-----BEGIN PUBLIC KEY-----\ninvalid_base64_content\n-----END PUBLIC KEY-----";

        assertThrows(IllegalArgumentException.class, () -> new RsaKeyPairProvider(validPrivPem, invalidPubPem));
    }
}
