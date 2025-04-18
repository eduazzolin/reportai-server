package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.MeusRegistrosDTO;
import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.mapper.RegistroMapper;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.InteracaoService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

   @PutMapping("/{id}/concluir")
   public ResponseEntity<?> concluir(@PathVariable Long id) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity

      service.ConcluirById(id, usuario);
      return ResponseEntity.ok().build();
   }


   @GetMapping("/distancia")
   public ResponseEntity<?> listarPorDistancia(
           @RequestParam double latitude,
           @RequestParam double longitude,
           @RequestParam double distancia,
           @RequestParam String filtro,
           @RequestParam String ordenacao) {
      int limite = 100;

      List<Registro> registros = service.findByDistancia(latitude, longitude, distancia, limite, filtro, ordenacao);
      ArrayList<RegistroDTO> registrosDTO = new ArrayList<>();

      for (Registro registro : registros) {
         RegistroDTO registroDTO = RegistroMapper.toDTO(registro);
         registrosDTO.add(registroDTO);
      }

      return ResponseEntity.ok(registrosDTO);
   }

   @GetMapping("/meus-registros")
   public ResponseEntity<?> listarMeusRegistros(@RequestParam int pagina, @RequestParam int limite) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity

      MeusRegistrosDTO meusRegistrosDTO = service.listarMeusRegistros(usuario, pagina, limite);

      return ResponseEntity.ok(meusRegistrosDTO);
   }

   @GetMapping("/{id}")
   public RegistroDTO buscarDTOPorId(@PathVariable Long id) {
      Registro registro = service.findById(id);
      return RegistroMapper.toDTO(registro);
   }

   @GetMapping("/dev/{id}")
   public ResponseEntity<Registro> buscarPorId(@PathVariable Long id) {
      Registro registro = service.findById(id);
      return ResponseEntity.ok(registro);
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> excluir(@PathVariable Long id) {
      Usuario usuario = new Usuario();
      usuario.setId(2L); // #ToDo #SpringSecurity

      service.deleteById(id, usuario);
      return ResponseEntity.ok().build();
   }


}
