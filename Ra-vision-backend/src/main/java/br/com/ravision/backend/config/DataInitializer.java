package br.com.ravision.backend.config;

import br.com.ravision.backend.domain.Role;
import br.com.ravision.backend.domain.Usuario;
import br.com.ravision.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMINISTRADOR);
            usuarioRepository.save(admin);
            log.info("Usuário administrador criado com sucesso: admin / admin123");
        } else {
            log.info("Usuário administrador já existe. Seed ignorado.");
        }
    }
}
