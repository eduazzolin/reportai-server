package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.ConclusaoProgramada;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.repository.ConclusaoProgramadaRepository;
import com.reportai.reportaiserver.repository.RegistroRepository;
import com.reportai.reportaiserver.service.CategoriaService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevController {

   @Autowired
   private CategoriaService service;

   @Autowired
   private RegistroService registroService;

   @Autowired
   private ConclusaoProgramadaRepository conclusaoProgramadaRepository;

   @Autowired
   private RegistroRepository registroRepository;


   @GetMapping("/conclusaoprogramada/{id}")
   public ResponseEntity<?> buscarConclusaoProgramada(@PathVariable Long id) {
      Registro registro = registroService.buscarPorId(id);
      ConclusaoProgramada conclusaoProgramada = conclusaoProgramadaRepository.findByRegistroAndRemovidaEm(registro, null);
      return ResponseEntity.ok(conclusaoProgramada);
   }

   @GetMapping("/relatorio-bairro")
   public ResponseEntity<?> relatorioBairro(
           @RequestParam(defaultValue = "1999-01-01") String dataInicio
   ) {
      return ResponseEntity.ok(registroRepository.generateRelatorioBairro(dataInicio));
   }


}
