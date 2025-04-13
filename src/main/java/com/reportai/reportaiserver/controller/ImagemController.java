package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.ImagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/imagens")
@RequiredArgsConstructor
public class ImagemController {

   @Autowired
   private ImagemService service;


   @GetMapping
   public ResponseEntity<?> buscarTodos() {
      try {
         return ResponseEntity.ok(service.buscarTodos());
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @GetMapping("/{id}")
   public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
      try {
         return ResponseEntity.ok(service.buscarPorId(id));
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> removerPorId(@PathVariable Long id) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity

      service.removerPorId(id, usuario);
      return ResponseEntity.ok().build();
   }

   @PostMapping
   public ResponseEntity<Imagem> salvar(@RequestParam("file") MultipartFile file, @RequestParam("idRegistro") Long idRegistro) throws IOException {

      Imagem imagemSalva = service.salvar(file, idRegistro);
      return ResponseEntity.ok(imagemSalva);

   }

}
