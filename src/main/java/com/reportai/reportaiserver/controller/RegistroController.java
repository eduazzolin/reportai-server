package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.mapper.RegistroMapper;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.InteracaoService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registros")
@RequiredArgsConstructor
public class RegistroController {

   @Autowired
   private RegistroService service;

   @Autowired
   private InteracaoService interacaoService;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Registro registro) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity
      registro.setUsuario(usuario);
      Registro registroSalvo = service.save(registro);
      RegistroDTO registroDTO = RegistroMapper.toDTO(registroSalvo);
      return ResponseEntity.ok(registroDTO);
   }

   @GetMapping
   public ResponseEntity<?> listar() {
      try {
         return ResponseEntity.ok(service.findAll());
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @GetMapping("/distancia")
   public ResponseEntity<?> listarPorDistancia(@RequestParam double latitude, @RequestParam double longitude, @RequestParam double distancia, @RequestParam int pagina) {

      try {
         return ResponseEntity.ok(service.findByDistancia(latitude, longitude, distancia, 99999, pagina));
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @GetMapping("/{id}")
   public RegistroDTO buscarDTOPorId(@PathVariable Long id) {
      Registro registro = service.findById(id);
      return RegistroMapper.toDTO(registro);
   }

   @GetMapping("/d/{id}")
   public Registro buscarPorId(@PathVariable Long id) {
      return service.findById(id);
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

   @PostMapping("/interagir")
   public ResponseEntity<?> interagir(@PathVariable Long id, @RequestBody Interacao interacao) {
      try {
         Registro registro = service.findById(id);
         interacao.setRegistro(registro);
         interacaoService.save(interacao);
         return ResponseEntity.ok().build();
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }


}
