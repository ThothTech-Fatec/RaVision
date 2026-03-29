package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseComissionamento;
import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.BaseVendas;
import br.com.ravision.backend.repository.BaseComissionamentoRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.BaseVendasRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final BaseRHRepository rhRepository;
    private final BaseVendasRepository vendasRepository;
    private final BaseComissionamentoRepository comissaoRepository;

    @Transactional
    public void processarLote(MultipartFile fileRH, MultipartFile fileVendas, MultipartFile fileComissoes) throws Exception {
        log.info("Iniciando processamento das planilhas");

        List<BaseRH> listaRH = parseRH(fileRH);
        List<BaseVendas> listaVendas = parseVendas(fileVendas);
        List<BaseComissionamento> listaComissoes = parseComissoes(fileComissoes);

        if (listaRH.isEmpty() || listaVendas.isEmpty()) {
            throw new IllegalArgumentException("Planilhas RH e Vendas não podem estar vazias.");
        }

        LocalDate dataRefRH = listaRH.get(0).getDateRef();
        LocalDate dataRefVendas = listaVendas.get(0).getDateRef();

        // O dia 1 normalizado deve coincidir no ano e mês para ambas as bases
        if (!dataRefRH.equals(dataRefVendas)) {
            throw new IllegalArgumentException("As datas de referência (Competência) da base de RH e Vendas divergem. RH: " 
                + dataRefRH + " Vendas: " + dataRefVendas);
        }

        rhRepository.deleteByDateRef(dataRefRH);
        vendasRepository.deleteByDateRef(dataRefRH);
        comissaoRepository.deleteAll();

        rhRepository.saveAll(listaRH);
        vendasRepository.saveAll(listaVendas);
        comissaoRepository.saveAll(listaComissoes);

        log.info("Processamento concluído. Mês de competência salvo: " + dataRefRH);
    }

    private LocalDate parseDateStr(String d) {
        if (d == null || d.isBlank()) return null;
        String val = d.trim().toLowerCase();
        
        // Padrão "dez.-25" ou "jan.-25" presente na Base de RH
        if (val.matches("^[a-z_ç]{3}\\.-[0-9]{2}$")) {
            String monthStr = val.substring(0, 3);
            int year = 2000 + Integer.parseInt(val.substring(5));
            int month = switch (monthStr) {
                case "jan" -> 1;
                case "fev" -> 2;
                case "mar", "març" -> 3;
                case "abr" -> 4;
                case "mai" -> 5;
                case "jun" -> 6;
                case "jul" -> 7;
                case "ago" -> 8;
                case "set" -> 9;
                case "out" -> 10;
                case "nov" -> 11;
                case "dez" -> 12;
                default -> 1;
            };
            return LocalDate.of(year, month, 1);
        }
        
        // Formatos Date tradicionais
        try {
            return LocalDate.parse(val, DateTimeFormatter.ofPattern("M/d/yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(val, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                return LocalDate.now();
            }
        }
    }

    private List<BaseRH> parseRH(MultipartFile file) throws Exception {
        List<BaseRH> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> lines = reader.readAll();
            for (int i = 1; i < lines.size(); i++) {
                String[] data = lines.get(i);
                
                // Tratar se abrir usando vírgula ou ponto-e-vírgula
                if (data.length == 1 && data[0].contains(";")) {
                    data = data[0].split(";");
                }
                
                if (data.length < 10) continue;
                
                BaseRH rh = new BaseRH();
                rh.setDateRef(parseDateStr(data[0]));
                rh.setCodMarca(Integer.parseInt(data[1].trim()));
                rh.setDescrMarca(data[2].trim());
                rh.setCodLoja(Integer.parseInt(data[3].trim()));
                rh.setDescrLoja(data[4].trim());
                rh.setMatricula(data[5].trim());
                rh.setDataAdmissao(parseDateStr(data[6]));
                rh.setDataDemissao(parseDateStr(data[7]));
                rh.setCodCargo(Integer.parseInt(data[8].trim()));
                rh.setDescrCargo(data[9].trim());
                result.add(rh);
            }
        }
        return result;
    }

    private List<BaseVendas> parseVendas(MultipartFile file) throws Exception {
        List<BaseVendas> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> lines = reader.readAll();
            for (int i = 1; i < lines.size(); i++) {
                String[] data = lines.get(i);
                
                if (data.length == 1 && data[0].contains(";")) {
                    data = data[0].split(";");
                }
                
                if (data.length < 7) continue;

                BaseVendas v = new BaseVendas();
                v.setDateRef(parseDateStr(data[0]));
                v.setCodMarca(Integer.parseInt(data[1].trim()));
                v.setDescrMarca(data[2].trim());
                v.setCodLoja(Integer.parseInt(data[3].trim()));
                v.setDescrLoja(data[4].trim());
                v.setMatricula(data[5].trim());
                String valorNormalizado = data[6].replace("\"", "").replace(",", ".");
                v.setVlrVenda(new BigDecimal(valorNormalizado));
                result.add(v);
            }
        }
        return result;
    }

    private List<BaseComissionamento> parseComissoes(MultipartFile file) throws Exception {
        List<BaseComissionamento> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> lines = reader.readAll();
            for (int i = 1; i < lines.size(); i++) {
                String[] data = lines.get(i);
                
                if (data.length == 1 && data[0].contains(";")) {
                    data = data[0].split(";");
                }
                
                if (data.length < 5) continue;

                BaseComissionamento c = new BaseComissionamento();
                c.setCodMarca(Integer.parseInt(data[0].trim()));
                c.setDescrMarca(data[1].trim());
                c.setCodCargo(Integer.parseInt(data[2].trim()));
                c.setDescriCargo(data[3].trim());
                String percentualNormal = data[4].replace("\"", "").replace("%", "").replace(",", ".");
                c.setPercentualComissao(new BigDecimal(percentualNormal));
                result.add(c);
            }
        }
        return result;
    }
}
