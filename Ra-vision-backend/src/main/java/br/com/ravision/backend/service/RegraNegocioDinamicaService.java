package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.RegraNegocioDinamica;
import br.com.ravision.backend.domain.StatusAprovacao;
import br.com.ravision.backend.dto.RegraNegocioRequest;
import br.com.ravision.backend.dto.RegraNegocioResponse;
import br.com.ravision.backend.repository.RegraNegocioDinamicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegraNegocioDinamicaService {

    private final RegraNegocioDinamicaRepository repository;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<RegraNegocioResponse> listarTodas() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegraNegocioResponse buscarPorId(Long id) {
        RegraNegocioDinamica regra = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));
        return toResponse(regra);
    }

    @Transactional
    public RegraNegocioResponse criarRegra(RegraNegocioRequest request) {
        String currentUser = getCurrentUser();
        
        RegraNegocioDinamica regra = RegraNegocioDinamica.builder()
                .descricaoRegra(request.getDescricaoRegra())
                .tipoRegra(request.getTipoRegra())
                .mesCompetencia(request.getMesCompetencia())
                .condicoesAplicacao(request.getCondicoesAplicacao())
                .valorModificador(request.getValorModificador())
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor(currentUser)
                .build();

        RegraNegocioDinamica saved = repository.save(regra);
        
        historicoService.registrarAcao(
                currentUser,
                "Regra Criada",
                "Nova regra (" + request.getTipoRegra() + ") criada com status PENDENTE"
        );
        
        return toResponse(saved);
    }

    @Transactional
    public RegraNegocioResponse editarRegra(Long id, RegraNegocioRequest request) {
        RegraNegocioDinamica regra = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        regra.setDescricaoRegra(request.getDescricaoRegra());
        regra.setTipoRegra(request.getTipoRegra());
        regra.setMesCompetencia(request.getMesCompetencia());
        regra.setCondicoesAplicacao(request.getCondicoesAplicacao());
        regra.setValorModificador(request.getValorModificador());
        regra.setStatusAprovacao(StatusAprovacao.PENDENTE); // Volta para pendente após edição

        RegraNegocioDinamica updated = repository.save(regra);
        
        historicoService.registrarAcao(
                getCurrentUser(),
                "Regra Editada",
                "Regra ID " + id + " editada e voltou para status PENDENTE"
        );
        
        return toResponse(updated);
    }

    @Transactional
    public void excluirRegra(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Regra não encontrada");
        }
        repository.deleteById(id);
        
        historicoService.registrarAcao(
                getCurrentUser(),
                "Regra Excluída",
                "Regra ID " + id + " excluída"
        );
    }

    @Transactional
    public RegraNegocioResponse aprovarRegra(Long id) {
        RegraNegocioDinamica regra = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        regra.setStatusAprovacao(StatusAprovacao.APROVADA);
        RegraNegocioDinamica updated = repository.save(regra);
        
        historicoService.registrarAcao(
                getCurrentUser(),
                "Regra Aprovada",
                "Regra ID " + id + " aprovada"
        );

        return toResponse(updated);
    }

    @Transactional
    public RegraNegocioResponse recusarRegra(Long id) {
        RegraNegocioDinamica regra = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        regra.setStatusAprovacao(StatusAprovacao.RECUSADA);
        RegraNegocioDinamica updated = repository.save(regra);
        
        historicoService.registrarAcao(
                getCurrentUser(),
                "Regra Recusada",
                "Regra ID " + id + " recusada"
        );

        return toResponse(updated);
    }

    private String getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }

    private RegraNegocioResponse toResponse(RegraNegocioDinamica regra) {
        return RegraNegocioResponse.builder()
                .id(regra.getId())
                .descricaoRegra(regra.getDescricaoRegra())
                .tipoRegra(regra.getTipoRegra())
                .mesCompetencia(regra.getMesCompetencia())
                .condicoesAplicacao(regra.getCondicoesAplicacao())
                .valorModificador(regra.getValorModificador())
                .statusAprovacao(regra.getStatusAprovacao())
                .criadoPor(regra.getCriadoPor())
                .dataCriacao(regra.getDataCriacao())
                .build();
    }
}
