package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.ConclusaoProgramada;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.repository.ConclusaoProgramadaRepository;
import com.reportai.reportaiserver.service.CategoriaService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


   @GetMapping("/conclusaoprogramada/{id}")
   public ResponseEntity<?> buscarConclusaoProgramada(@PathVariable Long id) {
      Registro registro = registroService.buscarPorId(id);
      ConclusaoProgramada conclusaoProgramada = conclusaoProgramadaRepository.findByRegistroAndRemovidaEm(registro, null);
      return ResponseEntity.ok(conclusaoProgramada);
   }


}
