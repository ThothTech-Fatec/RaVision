package br.com.ravision.backend.service;

import br.com.ravision.backend.config.JwtUtil;
import br.com.ravision.backend.domain.Role;
import br.com.ravision.backend.domain.Usuario;
import br.com.ravision.backend.dto.LoginRequest;
import br.com.ravision.backend.dto.LoginResponse;
import br.com.ravision.backend.dto.RegisterRequest;
import br.com.ravision.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HistoricoService historicoService;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRole().name());

        // Registrar ação de login no histórico
        historicoService.registrarAcao(
                usuario.getUsername(),
                "Login",
                "Login realizado com sucesso via JWT"
        );

        log.info("Login realizado com sucesso para usuário: {}", usuario.getUsername());

        return new LoginResponse(token, usuario.getUsername(), usuario.getRole().name());
    }

    public LoginResponse register(RegisterRequest request, String adminUsername) {
        // Verificar se o username já existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username já está em uso: " + request.getUsername());
        }

        // Validar role
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role inválida: " + request.getRole() +
                    ". Valores válidos: ADMINISTRADOR, ANALISTA, GESTOR_RH, AUDITOR");
        }

        // Criar novo usuário
        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(request.getUsername());
        novoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        novoUsuario.setRole(role);

        usuarioRepository.save(novoUsuario);

        // Registrar ação no histórico
        historicoService.registrarAcao(
                adminUsername,
                "Registro de Usuário",
                "Novo usuário registrado: " + novoUsuario.getUsername() + " com role " + novoUsuario.getRole()
        );

        // Gerar token para o novo usuário
        String token = jwtUtil.generateToken(novoUsuario.getUsername(), novoUsuario.getRole().name());

        log.info("Novo usuário registrado por {}: {} com role {}", adminUsername, novoUsuario.getUsername(), novoUsuario.getRole());

        return new LoginResponse(token, novoUsuario.getUsername(), novoUsuario.getRole().name());
    }
}
