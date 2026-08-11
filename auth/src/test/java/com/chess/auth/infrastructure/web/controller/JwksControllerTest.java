package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.infrastructure.security.RsaKeyPairProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JwksControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RsaKeyPairProvider keyPairProvider = new RsaKeyPairProvider();
        JwksController jwksController = new JwksController(keyPairProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(jwksController).build();
    }

    @Test
    @DisplayName("GET /.well-known/jwks.json - Should return 200 OK with RSA public key parameters")
    void getJwks_ShouldReturnJwksPayload() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].kid").value("auth-key-1"))
                .andExpect(jsonPath("$.keys[0].n").exists())
                .andExpect(jsonPath("$.keys[0].e").exists());
    }
}
