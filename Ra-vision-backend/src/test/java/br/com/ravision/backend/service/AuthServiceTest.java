package br.com.ravision.backend.service;

import br.com.ravision.backend.config.JwtUtil;
import br.com.ravision.backend.domain.Role;
import br.com.ravision.backend.domain.Usuario;
import br.com.ravision.backend.dto.LoginRequest;
import br.com.ravision.backend.dto.LoginResponse;
import br.com.ravision.backend.dto.RegisterRequest;
import br.com.ravision.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HistoricoService historicoService;

    @InjectMocks
    private AuthService authService;

    private Usuario adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("$2a$10$encodedPassword");
        adminUser.setRole(Role.ADMINISTRADOR);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Login com credenciais válidas deve retornar token JWT")
    void loginComCredenciaisValidasDeveRetornarToken() {
        LoginRequest request = new LoginRequest("admin", "admin123");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin123", adminUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMINISTRADOR")).thenReturn("jwt.token.mock");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.mock", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ADMINISTRADOR", response.getRole());
    }

    @Test
    @DisplayName("Login deve registrar ação no histórico de processamento")
    void loginDeveRegistrarAcaoNoHistorico() {
        LoginRequest request = new LoginRequest("admin", "admin123");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin123", adminUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMINISTRADOR")).thenReturn("jwt.token.mock");

        authService.login(request);

        verify(historicoService, times(1)).registrarAcao(
                eq("admin"),
                eq("Login"),
                anyString()
        );
    }

    @Test
    @DisplayName("Login com username inexistente deve lançar exceção")
    void loginComUsernameInexistenteDeveLancarExcecao() {
        LoginRequest request = new LoginRequest("usuario_inexistente", "senha");

        when(usuarioRepository.findByUsername("usuario_inexistente")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    @Test
    @DisplayName("Login com senha incorreta deve lançar exceção")
    void loginComSenhaIncorretaDeveLancarExcecao() {
        LoginRequest request = new LoginRequest("admin", "senhaErrada");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("senhaErrada", adminUser.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    @Test
    @DisplayName("Login com senha errada não deve registrar no histórico")
    void loginComSenhaErradaNaoDeveRegistrarNoHistorico() {
        LoginRequest request = new LoginRequest("admin", "senhaErrada");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("senhaErrada", adminUser.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));

        verify(historicoService, never()).registrarAcao(anyString(), anyString(), anyString());
    }

    // ─── REGISTRO ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Registro de novo usuário deve criar e retornar token")
    void registroDeNovoUsuarioDeveCriarERetornarToken() {
        RegisterRequest request = new RegisterRequest("analista1", "senha123", "ANALISTA");

        when(usuarioRepository.existsByUsername("analista1")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedSenha123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtUtil.generateToken("analista1", "ANALISTA")).thenReturn("jwt.token.analista");

        LoginResponse response = authService.register(request, "admin");

        assertNotNull(response);
        assertEquals("jwt.token.analista", response.getToken());
        assertEquals("analista1", response.getUsername());
        assertEquals("ANALISTA", response.getRole());
    }

    @Test
    @DisplayName("Registro deve registrar ação no histórico com info do admin que criou")
    void registroDeveRegistrarAcaoNoHistorico() {
        RegisterRequest request = new RegisterRequest("gestor1", "senha123", "GESTOR_RH");

        when(usuarioRepository.existsByUsername("gestor1")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("gestor1", "GESTOR_RH")).thenReturn("jwt.token.gestor");

        authService.register(request, "admin");

        verify(historicoService, times(1)).registrarAcao(
                eq("admin"),
                eq("Registro de Usuário"),
                anyString()
        );
    }

    @Test
    @DisplayName("Registro com username já existente deve lançar exceção")
    void registroComUsernameExistenteDeveLancarExcecao() {
        RegisterRequest request = new RegisterRequest("admin", "senha123", "ANALISTA");

        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request, "admin"));

        assertTrue(exception.getMessage().contains("já está em uso"));
    }

    @Test
    @DisplayName("Registro com role inválida deve lançar exceção")
    void registroComRoleInvalidaDeveLancarExcecao() {
        RegisterRequest request = new RegisterRequest("user1", "senha123", "ROLE_INVALIDA");

        when(usuarioRepository.existsByUsername("user1")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request, "admin"));

        assertTrue(exception.getMessage().contains("Role inválida"));
    }

    @Test
    @DisplayName("Registro deve encodar senha com BCrypt antes de salvar")
    void registroDeveEncodarSenhaComBcrypt() {
        RegisterRequest request = new RegisterRequest("auditor1", "senha123", "AUDITOR");

        when(usuarioRepository.existsByUsername("auditor1")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$bcryptHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("auditor1", "AUDITOR")).thenReturn("jwt.token");

        authService.register(request, "admin");

        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(argThat(user ->
                user.getPassword().equals("$2a$10$bcryptHash")
        ));
    }
}
