package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.MonitoramentoIA;
import br.com.ravision.backend.dto.EstatisticaDiariaDTO;
import br.com.ravision.backend.dto.MonitoramentoAgregadoDTO;
import br.com.ravision.backend.dto.MonitoramentoIADTO;
import br.com.ravision.backend.repository.MonitoramentoIARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoramentoIAService {

    private final MonitoramentoIARepository repository;

    @Async
    public void salvarMetrica(MonitoramentoIADTO dto) {
        log.info("Salvando nova métrica de IA de forma assíncrona. Usuário: {}", dto.getUsuarioLogado());
        try {
            MonitoramentoIA entidade = new MonitoramentoIA();
            entidade.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
            entidade.setPerguntaUsuario(dto.getPerguntaUsuario());
            entidade.setRespostaIA(dto.getRespostaIA());
            entidade.setTempoRespostaMs(dto.getTempoRespostaMs());
            entidade.setFerramentaUtilizada(dto.getFerramentaUtilizada());
            entidade.setSucessoFerramenta(dto.getSucessoFerramenta() != null ? dto.getSucessoFerramenta() : true);
            entidade.setUsuarioLogado(dto.getUsuarioLogado() != null ? dto.getUsuarioLogado() : "SISTEMA");

            repository.save(entidade);
        } catch (Exception e) {
            log.error("Erro ao salvar métrica de monitoramento IA: {}", e.getMessage(), e);
        }
    }

    public MonitoramentoAgregadoDTO buscarMetricasAgregadas(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime start = dataInicio.atStartOfDay();
        LocalDateTime end = dataFim.atTime(LocalTime.MAX);

        Long totalRequisicoes = repository.countByTimestampBetween(start, end);
        Double tempoMedioGeralMs = repository.averageTempoRespostaBetween(start, end);
        
        if(tempoMedioGeralMs == null) {
            tempoMedioGeralMs = 0.0;
        }

        List<EstatisticaDiariaDTO> estatisticasDiarias = repository.findEstatisticasDiariasBetween(start, end);
        List<MonitoramentoIADTO> logsRecentes = buscarLogsDetalhados(dataInicio, dataFim);

        return new MonitoramentoAgregadoDTO(totalRequisicoes, tempoMedioGeralMs, estatisticasDiarias, logsRecentes);
    }

    public List<MonitoramentoIADTO> buscarLogsDetalhados(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime start = dataInicio.atStartOfDay();
        LocalDateTime end = dataFim.atTime(LocalTime.MAX);

        return repository.findByTimestampBetweenOrderByTimestampDesc(start, end).stream().map(m -> 
            new MonitoramentoIADTO(
                m.getTimestamp(),
                m.getPerguntaUsuario(),
                m.getRespostaIA(),
                m.getTempoRespostaMs(),
                m.getFerramentaUtilizada(),
                m.getSucessoFerramenta(),
                m.getUsuarioLogado()
            )
        ).collect(Collectors.toList());
    }
}
