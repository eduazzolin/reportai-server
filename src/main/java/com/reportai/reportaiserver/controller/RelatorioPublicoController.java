package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.RelatorioStatusProjection;
import com.reportai.reportaiserver.repository.RegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorio-publico")
@RequiredArgsConstructor
public class RelatorioPublicoController {


   @Autowired
   private RegistroRepository registroRepository;

   /**
    * Gera um relatório de registros por status. Este endpoint é ABERTO.
    * @param dataInicio
    * @param dataFim
    * @return RelatorioStatusProjection
    */
   @GetMapping("/status")
   public ResponseEntity<List<RelatorioStatusProjection>> relatorioStatus(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio,
           @RequestParam(defaultValue = "2999-01-01") String dataFim
   ) {
      return ResponseEntity.ok(registroRepository.generateRelatorioStatus(dataInicio, dataFim));
   }

   /**
    * Gera um relatório de registros por bairro. Este endpoint é ABERTO.
    * @param dataInicio
    * @param dataFim
    * @return RelatorioBairroProjection
    */
   @GetMapping("/bairro")
   public ResponseEntity<?> relatorioBairro(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio,
           @RequestParam(defaultValue = "2999-01-01") String dataFim
   ) {
      return ResponseEntity.ok(registroRepository.generateRelatorioBairro(dataInicio, dataFim));
   }

   /**
    * Gera um relatório de registros por categoria. Este endpoint é ABERTO.
    * @param dataInicio
    * @param dataFim
    * @return RelatorioCategoriaProjection
    */
   @GetMapping("/categoria")
   public ResponseEntity<?> relatorioCategoria(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio,
           @RequestParam(defaultValue = "2999-01-01") String dataFim
   ) {
      return ResponseEntity.ok(registroRepository.generateRelatorioCategoria(dataInicio, dataFim));
   }


}
