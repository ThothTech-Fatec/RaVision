package br.com.ravision.backend.controller;

import br.com.ravision.backend.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<String> uploadArquivosDaCompetencia(
            @RequestParam("rh") MultipartFile rh,
            @RequestParam("vendas") MultipartFile vendas,
            @RequestParam("comissao") MultipartFile comissao) {
        
        log.info("Recebido requisição de upload. RH: {}, Vendas: {}, Comissao: {}", 
                rh.getOriginalFilename(), vendas.getOriginalFilename(), comissao.getOriginalFilename());

        try {
            uploadService.processarLote(rh, vendas, comissao);
            return ResponseEntity.ok("Arquivos importados e cruzados com sucesso.");
        } catch (IllegalArgumentException e) {
            log.error("Erro de validação: " + e.getMessage());
            return ResponseEntity.badRequest().body("Falha de validação: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro interno ao processar as planilhas.", e);
            return ResponseEntity.internalServerError().body("Erro ao processar as planilhas: " + e.getMessage());
        }
    }
}
