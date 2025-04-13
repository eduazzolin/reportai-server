package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.InteracaoRegistroSimplesDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.InteracaoService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.reportai.reportaiserver.exception.ErrorDictionary.SEM_PERMISSAO;
import static com.reportai.reportaiserver.model.Interacao.TipoInteracao.RELEVANTE;

@RestController
@RequestMapping("/interacoes")
@RequiredArgsConstructor
public class InteracaoController {

   @Autowired
   private InteracaoService service;

   @Autowired
   private RegistroService registroService;

   @GetMapping("/relevantes/{idRegistro}")
   public ResponseEntity<?> buscarInteracoesRelevante(@PathVariable Long idRegistro) {
      Registro registro = registroService.buscarPorId(idRegistro);
      return ResponseEntity.ok(service.BuscarDTOsPorRegistroETipo(registro, RELEVANTE));
   }

   @GetMapping("/{idRegistro}")
   public ResponseEntity<?> buscarInteracaoRegistroSimplesDTOPorIdRegistro(@PathVariable Long idRegistro) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      Registro registro = registroService.buscarPorId(idRegistro);
      InteracaoRegistroSimplesDTO interacaoRegistroSimplesDTO = service.buscarDTORegistroSimplesPorRegistroEUsuario(registro, usuario);
      return ResponseEntity.ok(interacaoRegistroSimplesDTO);
   }

   @PostMapping()
   public ResponseEntity<?> salvar(@RequestBody Interacao interacao) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      interacao.setUsuario(usuario);
      return ResponseEntity.ok(service.salvar(interacao));
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> remover(@PathVariable Long id) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      Interacao interacao = service.buscarPorId(id);
      if (interacao.getUsuario().getId() != usuario.getId()) {
         throw new CustomException(SEM_PERMISSAO);
      }
      service.remover(interacao);
      return ResponseEntity.ok().build();
   }

}
