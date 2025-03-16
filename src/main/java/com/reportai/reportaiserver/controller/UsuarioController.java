package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.TokenDTO;
import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

   @Autowired
   private UsuarioService service;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Usuario usuario) {
      Usuario usuarioSalvo = service.save(usuario);
      return ResponseEntity.ok(usuarioSalvo);
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> deletar(@PathVariable Long id) {
      service.deleteById(id);
      return ResponseEntity.ok().build();
   }


   @PostMapping("/autenticar")
   public ResponseEntity<?> autenticar(@RequestBody Usuario usuario) {
      Usuario usuarioAutenticado = service.autenticar(usuario.getEmail(), usuario.getSenha());
      // TokenDTO tokenDTO = new TokenDTO(usurioAutenticado.getNome(), jwtService.generateToken(usurioAutenticado)); #ToDo #SpringSecurity
      LocalDateTime dataExp = LocalDateTime.now().plusMinutes(999);
      TokenDTO tokenDTO = new TokenDTO(
              usuarioAutenticado.getId(),
              dataExp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
              usuarioAutenticado.getNome(),
              "token_placeholder");
      return ResponseEntity.ok(tokenDTO);
   }

   @GetMapping("/{id}")
   public ResponseEntity<?> buscarDTOPorId(@PathVariable Long id) {
      UsuarioDTO usuario = service.findDTOById(id);
      return ResponseEntity.ok(usuario);
   }


}
