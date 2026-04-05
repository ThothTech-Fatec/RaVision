package br.com.ravision.backend.controller;

import br.com.ravision.backend.domain.IntercorrenciaRH;
import br.com.ravision.backend.repository.IntercorrenciaRHRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intercorrencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IntercorrenciaController {

    private final IntercorrenciaRHRepository repository;

    @PostMapping
    public ResponseEntity<IntercorrenciaRH> salvar(@RequestBody IntercorrenciaRH intercorrencia) {
        return ResponseEntity.ok(repository.save(intercorrencia));
    }

    @GetMapping
    public ResponseEntity<java.util.List<IntercorrenciaRH>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }
}
