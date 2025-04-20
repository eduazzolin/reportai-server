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

   @GetMapping("/status")
   public ResponseEntity<List<RelatorioStatusProjection>> relatorioStatus(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio
   ) {
       return ResponseEntity.ok(registroRepository.generateRelatorioStatus(dataInicio));
   }

   @GetMapping("/bairro")
   public ResponseEntity<?> relatorioBairro(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio
   ) {
       return ResponseEntity.ok(registroRepository.generateRelatorioBairro(dataInicio));
   }

   @GetMapping("/categoria")
   public ResponseEntity<?> relatorioCategoria(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio
   ) {
       return ResponseEntity.ok(registroRepository.generateRelatorioCategoria(dataInicio));
   }
}
