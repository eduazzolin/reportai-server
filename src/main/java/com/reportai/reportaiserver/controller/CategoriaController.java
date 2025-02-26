package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.service.CategoriaService;
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

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Categoria categoria) {
      try {
         Categoria categoriaSalvo = service.save(categoria);
         return ResponseEntity.ok(categoriaSalvo);
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
