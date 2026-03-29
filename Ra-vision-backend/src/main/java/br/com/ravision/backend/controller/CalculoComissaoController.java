package br.com.ravision.backend.controller;

import br.com.ravision.backend.service.CalculoComissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalculoComissaoController {

    private final CalculoComissaoService service;

    @GetMapping("/base")
    public ResponseEntity<java.util.List<br.com.ravision.backend.domain.ComissaoCalculadaBase>> buscarCalculos(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return ResponseEntity.ok(service.buscarPorCompetencia(dateRef));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/base")
    public ResponseEntity<String> calcularBase(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            service.calcularCompetencia(dateRef);
            return ResponseEntity.ok("Calculos processados com sucesso para o mes " + dateRef);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao realizar calculo: " + e.getMessage());
        }
    }
}
