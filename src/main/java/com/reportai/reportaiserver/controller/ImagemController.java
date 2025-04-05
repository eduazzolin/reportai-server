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
   public ResponseEntity<?> listar() {
      try {
         return ResponseEntity.ok(service.findAll());
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @GetMapping("/{id}")
   public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
      try {
         return ResponseEntity.ok(service.findById(id));
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> excluir(@PathVariable Long id) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity

      service.deleteById(id, usuario);
      return ResponseEntity.ok().build();
   }

   @PostMapping
   public ResponseEntity<Imagem> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("idRegistro") Long idRegistro) throws IOException {

      Imagem imagemSalva = service.save(file, idRegistro);
      return ResponseEntity.ok(imagemSalva);

   }

}
