package br.com.ravision.backend.controller;

import br.com.ravision.backend.domain.ComissaoCalculadaProporcional;
import br.com.ravision.backend.service.CalculoProporcionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.ravision.backend.repository.ComissaoCalculadaProporcionalRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/calculos/proporcional")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalculoProporcionalController {

    private final CalculoProporcionalService service;
    private final ComissaoCalculadaProporcionalRepository repository;

    @PostMapping
    public ResponseEntity<String> processarProporcional(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            service.calcularProporcional(dateRef);
            return ResponseEntity.ok("Calculo Proporcional finalizado para " + dateRef);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Falha ao rodar calculo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ComissaoCalculadaProporcional>> verProporcional(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return ResponseEntity.ok(repository.findByDateRef(dateRef));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
