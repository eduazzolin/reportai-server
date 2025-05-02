package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.CategoriaService;
import com.reportai.reportaiserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

   @Autowired
   private CategoriaService service;

   @Autowired
   private JwtService jwtService;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Categoria categoria, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);

      Categoria categoriaSalvo = service.salvar(categoria);
      return ResponseEntity.ok(categoriaSalvo);
   }

   @GetMapping
   public ResponseEntity<?> buscarTodos() {
      return ResponseEntity.ok(service.buscarTodos());
   }

   @GetMapping("/{id}")
   public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
      return ResponseEntity.ok(service.buscarPorId(id));
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> removerPorId(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);

      service.removerPorId(id);
      return ResponseEntity.ok().build();
   }


}
