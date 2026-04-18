package br.com.ravision.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Base64-encoded 256-bit key for testing
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "RaVisionTestSecretKey2026ProjectDomRock00".getBytes()
    );
    private static final long TEST_EXPIRATION = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido")
    void deveGerarTokenValido() {
        String token = jwtUtil.generateToken("admin", "ADMINISTRADOR");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3, "Token JWT deve ter 3 partes separadas por ponto");
    }

    @Test
    @DisplayName("Deve extrair username do token corretamente")
    void deveExtrairUsernameDoToken() {
        String token = jwtUtil.generateToken("admin", "ADMINISTRADOR");

        String username = jwtUtil.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    @DisplayName("Deve extrair role do token corretamente")
    void deveExtrairRoleDoToken() {
        String token = jwtUtil.generateToken("gestor", "GESTOR_RH");

        String role = jwtUtil.extractRole(token);

        assertEquals("GESTOR_RH", role);
    }

    @Test
    @DisplayName("Token válido deve retornar true na validação")
    void tokenValidoDeveRetornarTrue() {
        String token = jwtUtil.generateToken("analista", "ANALISTA");

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    @DisplayName("Token inválido deve retornar false na validação")
    void tokenInvalidoDeveRetornarFalse() {
        assertFalse(jwtUtil.isTokenValid("token.invalido.aqui"));
    }

    @Test
    @DisplayName("Token nulo deve retornar false na validação")
    void tokenNuloDeveRetornarFalse() {
        assertFalse(jwtUtil.isTokenValid(null));
    }

    @Test
    @DisplayName("Token expirado deve retornar false na validação")
    void tokenExpiradoDeveRetornarFalse() {
        // Criar JwtUtil com expiração de 0ms (token já nasce expirado)
        JwtUtil expiredJwtUtil = new JwtUtil(TEST_SECRET, 0L);
        String token = expiredJwtUtil.generateToken("admin", "ADMINISTRADOR");

        // Pequeno delay para garantir que o token expirou
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertFalse(expiredJwtUtil.isTokenValid(token));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
    void deveGerarTokensDiferentes() {
        String token1 = jwtUtil.generateToken("admin", "ADMINISTRADOR");
        String token2 = jwtUtil.generateToken("analista", "ANALISTA");

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve extrair username correto para cada role")
    void deveExtrairUsernameCorretoParaCadaRole() {
        String[] roles = {"ADMINISTRADOR", "ANALISTA", "GESTOR_RH", "AUDITOR"};

        for (String role : roles) {
            String username = "user_" + role.toLowerCase();
            String token = jwtUtil.generateToken(username, role);

            assertEquals(username, jwtUtil.extractUsername(token));
            assertEquals(role, jwtUtil.extractRole(token));
        }
    }

    @Test
    @DisplayName("Token adulterado deve ser inválido")
    void tokenAdulteradoDeveSerInvalido() {
        String token = jwtUtil.generateToken("admin", "ADMINISTRADOR");

        // Adulterar a payload do token
        String[] parts = token.split("\\.");
        parts[1] = parts[1] + "ADULTERADO";
        String tampered = String.join(".", parts);

        assertFalse(jwtUtil.isTokenValid(tampered));
    }

    @Test
    @DisplayName("Deve extrair dados de token com role AUDITOR")
    void deveExtrairDadosTokenAuditor() {
        String token = jwtUtil.generateToken("auditor_user", "AUDITOR");

        assertEquals("auditor_user", jwtUtil.extractUsername(token));
        assertEquals("AUDITOR", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.isTokenValid(token));
    }
}
