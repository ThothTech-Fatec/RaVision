package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.RegraNegocioDinamica;
import br.com.ravision.backend.domain.StatusAprovacao;
import br.com.ravision.backend.dto.RegraNegocioRequest;
import br.com.ravision.backend.dto.RegraNegocioResponse;
import br.com.ravision.backend.repository.RegraNegocioDinamicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegraNegocioDinamicaService {

    private final RegraNegocioDinamicaRepository repository;
    private final HistoricoService historicoService;
    private final AplicadorRegrasSazonaisService aplicadorRegrasSazonaisService;

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

        // Capturar o mês ANTES de alterar, pois o mês pode mudar na edição
        String mesAntigo = regra.getMesCompetencia();

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
                "Regra ID " + id + " editada e voltou para status PENDENTE — recalculando comissões de " + mesAntigo
        );

        // Recalcular com base no mês original para reverter os efeitos que a regra tinha
        dispararRecalculo(mesAntigo);

        return toResponse(updated);
    }

    @Transactional
    public void excluirRegra(Long id) {
        RegraNegocioDinamica regra = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        String mesCompetencia = regra.getMesCompetencia();
        repository.deleteById(id);

        historicoService.registrarAcao(
                getCurrentUser(),
                "Regra Excluída",
                "Regra ID " + id + " excluída — recalculando comissões de " + mesCompetencia
        );

        dispararRecalculo(mesCompetencia);
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
                "Regra ID " + id + " aprovada — recalculando comissões de " + regra.getMesCompetencia()
        );

        dispararRecalculo(regra.getMesCompetencia());

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
                "Regra ID " + id + " recusada — recalculando comissões de " + regra.getMesCompetencia()
        );

        dispararRecalculo(regra.getMesCompetencia());

        return toResponse(updated);
    }

    /**
     * Dispara o recálculo das comissões finais para o mês de competência da regra.
     * O mesCompetencia está no formato "YYYY-MM" (ex: "2025-11").
     * Concatenamos "-01" apenas para formar uma LocalDate válida como chave técnica de dateRef
     * nos repositórios — o dia 01 não representa um registro real nas planilhas.
     * Se a folha ainda não foi processada para aquele mês, loga um aviso e segue sem erros.
     */
    private void dispararRecalculo(String mesCompetencia) {
        try {
            LocalDate dateRef = LocalDate.parse(mesCompetencia + "-01");
            log.info("Disparando recálculo de comissões sazonais para competência: {}", mesCompetencia);
            aplicadorRegrasSazonaisService.aplicarRegrasSazonais(dateRef);
            log.info("Recálculo de comissões concluído para competência: {}", mesCompetencia);
        } catch (IllegalArgumentException e) {
            log.warn("Recálculo ignorado para {}: folha ainda não processada ({})", mesCompetencia, e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao recalcular comissões para competência {}", mesCompetencia, e);
        }
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
