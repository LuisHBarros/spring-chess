package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.infrastructure.security.RsaKeyPairProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "JWKS", description = "JSON Web Key Set endpoint for JWT signature verification")
public class JwksController {

    private final RsaKeyPairProvider keyPairProvider;

    public JwksController(RsaKeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
    }

    @Operation(
            summary = "Get JSON Web Key Set",
            description = "Returns the public RSA keys used to verify JWT signatures. "
                    + "Other services use this endpoint to validate access tokens issued by this auth service.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponse(responseCode = "200", description = "JWKS payload returned successfully")
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(keyPairProvider.getJwksPayload());
    }
}
