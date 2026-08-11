package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.infrastructure.security.RsaKeyPairProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {

    private final RsaKeyPairProvider keyPairProvider;

    public JwksController(RsaKeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(keyPairProvider.getJwksPayload());
    }
}
