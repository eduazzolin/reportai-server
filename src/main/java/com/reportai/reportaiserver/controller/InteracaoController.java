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
   public ResponseEntity<?> buscarRelevantes(@PathVariable Long idRegistro) {
      Registro registro = registroService.findById(idRegistro);
      return ResponseEntity.ok(service.findByRegistroAndTipo(registro, RELEVANTE));
   }

   @GetMapping("/{idRegistro}")
   public ResponseEntity<?> buscarPorRegistroSimples(@PathVariable Long idRegistro) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      Registro registro = registroService.findById(idRegistro);
      InteracaoRegistroSimplesDTO interacaoRegistroSimplesDTO = service.findByRegistroSimples(registro, usuario);
      return ResponseEntity.ok(interacaoRegistroSimplesDTO);
   }

   @PostMapping()
   public ResponseEntity<?> salvar(@RequestBody Interacao interacao) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      interacao.setUsuario(usuario);
      return ResponseEntity.ok(service.save(interacao));
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> excluir(@PathVariable Long id) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      Interacao interacao = service.findById(id);
      if (interacao.getUsuario().getId() != usuario.getId()) {
         throw new CustomException(SEM_PERMISSAO);
      }

      service.delete(interacao);
      return ResponseEntity.ok().build();
   }

}
