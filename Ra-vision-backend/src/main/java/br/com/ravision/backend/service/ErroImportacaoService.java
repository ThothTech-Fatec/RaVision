package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.ErroImportacao;
import br.com.ravision.backend.domain.StatusErro;
import br.com.ravision.backend.dto.ErroImportacaoDTO;
import br.com.ravision.backend.repository.ErroImportacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErroImportacaoService {

    private final ErroImportacaoRepository repository;

    @Transactional
    public ErroImportacaoDTO salvarErro(ErroImportacaoDTO dto) {
        log.warn("Registrando erro de importação: arquivo={}, linha={}, erro={}", dto.getNomeArquivo(), dto.getLinha(), dto.getMensagemErro());
        ErroImportacao erro = new ErroImportacao();
        erro.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        erro.setNomeArquivo(dto.getNomeArquivo());
        erro.setLinha(dto.getLinha());
        erro.setColuna(dto.getColuna());
        erro.setMensagemErro(dto.getMensagemErro());
        erro.setTipoErro(dto.getTipoErro());
        erro.setStatus(StatusErro.PENDENTE);
        erro.setUsuarioUpload(dto.getUsuarioUpload());

        erro = repository.save(erro);
        return convertToDto(erro);
    }

    public Page<ErroImportacaoDTO> listarErros(StatusErro status, String nomeArquivo, LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        return repository.findByFiltros(status, nomeArquivo, dataInicio, dataFim, pageable).map(this::convertToDto);
    }

    @Transactional
    public ErroImportacaoDTO marcarComoResolvido(Long id) {
        ErroImportacao erro = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Erro de importação não encontrado com ID: " + id));
        erro.setStatus(StatusErro.RESOLVIDO);
        erro = repository.save(erro);
        return convertToDto(erro);
    }

    public long contarErrosPendentes() {
        return repository.countByStatus(StatusErro.PENDENTE);
    }

    private ErroImportacaoDTO convertToDto(ErroImportacao erro) {
        return ErroImportacaoDTO.builder()
                .id(erro.getId())
                .timestamp(erro.getTimestamp())
                .nomeArquivo(erro.getNomeArquivo())
                .linha(erro.getLinha())
                .coluna(erro.getColuna())
                .mensagemErro(erro.getMensagemErro())
                .tipoErro(erro.getTipoErro())
                .status(erro.getStatus())
                .usuarioUpload(erro.getUsuarioUpload())
                .build();
    }
}
