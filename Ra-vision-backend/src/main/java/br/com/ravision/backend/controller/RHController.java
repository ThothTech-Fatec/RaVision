package br.com.ravision.backend.controller;

import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.repository.BaseRHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rh")
@CrossOrigin(origins = "*")
public class RHController {

    @Autowired
    private BaseRHRepository baseRHRepository;

    @GetMapping("/funcionarios")
    public List<BaseRH> getFuncionariosByMonth(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return baseRHRepository.findByDateRef(dateRef);
    }

    @GetMapping("/meses-disponiveis")
    public List<LocalDate> getMesesDisponiveis() {
        return baseRHRepository.findDistinctDateRefs();
    }
}
