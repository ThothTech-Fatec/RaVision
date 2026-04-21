package br.com.ravision.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DatabaseConfig {

    @Bean
    public CommandLineRunner adjustDatabaseConstraints(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("Verificando constraints do banco de dados PostgreSQL...");
                jdbcTemplate.execute("ALTER TABLE tb_regra_negocio_dinamica DROP CONSTRAINT IF EXISTS tb_regra_negocio_dinamica_tipo_regra_check;");
                log.info("Constraint tb_regra_negocio_dinamica_tipo_regra_check removida com sucesso para permitir novos enums como BONUS_BASE.");
            } catch (Exception e) {
                log.warn("Nao foi possivel alterar a constraint do banco. O banco pode ja estar corrigido ou ser H2. Erro: {}", e.getMessage());
            }
        };
    }
}
