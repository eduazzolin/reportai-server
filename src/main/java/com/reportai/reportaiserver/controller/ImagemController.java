package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.service.ImagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/imagens")
@RequiredArgsConstructor
public class ImagemController {

   @Autowired
   private ImagemService service;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Imagem imagem) {
      try {
         Imagem imagemSalvo = service.save(imagem);
         return ResponseEntity.ok(imagemSalvo);
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

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
      try {
         service.deleteById(id);
         return ResponseEntity.ok().build();
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }


}
