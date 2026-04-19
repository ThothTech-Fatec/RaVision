package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.HistoricoProcessamento;
import br.com.ravision.backend.domain.Usuario;
import br.com.ravision.backend.dto.HistoricoResponse;
import br.com.ravision.backend.repository.HistoricoProcessamentoRepository;
import br.com.ravision.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoProcessamentoRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;

    public void registrarAcao(String username, String acao, String detalhes) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElse(null);

        if (usuario == null) {
            log.warn("Tentativa de registrar ação para usuário inexistente: {}", username);
            return;
        }

        HistoricoProcessamento historico = new HistoricoProcessamento();
        historico.setUsuario(usuario);
        historico.setAcaoRealizada(acao);
        historico.setDataHora(LocalDateTime.now());
        historico.setDetalhesAcao(detalhes);

        historicoRepository.save(historico);
        log.info("Ação registrada: [{}] {} - {}", username, acao, detalhes);
    }

    public List<HistoricoResponse> listarTodos() {
        return historicoRepository.findAllByOrderByDataHoraDesc()
                .stream()
                .map(h -> new HistoricoResponse(
                        h.getId(),
                        h.getUsuario().getUsername(),
                        h.getAcaoRealizada(),
                        h.getDataHora(),
                        h.getDetalhesAcao()
                ))
                .collect(Collectors.toList());
    }
}
