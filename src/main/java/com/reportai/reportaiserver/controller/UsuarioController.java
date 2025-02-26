package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

   @Autowired
   private UsuarioService service;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Usuario usuario) {
      try {
         Usuario usuarioSalvo = service.save(usuario);
         return ResponseEntity.ok(usuarioSalvo);
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }


}
