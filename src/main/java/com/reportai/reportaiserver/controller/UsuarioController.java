package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.MeusRegistrosDTO;
import com.reportai.reportaiserver.dto.TokenDTO;
import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.dto.UsuariosPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.UsuarioMapper;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

   @Autowired
   private UsuarioService service;
   @Autowired
   private UsuarioService usuarioService;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Usuario usuario) {
      if (usuario.getId() != null) {
         Usuario usuarioExistente = service.findById(usuario.getId());
         usuarioExistente.setNome(usuario.getNome());
         usuarioExistente.setEmail(usuario.getEmail());
         if (usuario.getSenha() != null) {
            usuarioExistente.setSenha(usuario.getSenha());
         }
         usuario = usuarioExistente;
      }
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


   @GetMapping("/admin")
   public ResponseEntity<?> buscarPorTermo(
           @RequestParam int pagina,
           @RequestParam int limite,
           @RequestParam String termo,
           @RequestParam String ordenacao) {
      Usuario usuario = usuarioService.findAtivosById(2L); // #ToDo #SpringSecurity

      if (!(usuario.getRole().equals(Usuario.Roles.ADMIN))) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      UsuariosPaginadoDTO usuariosPaginadoDTO = service.findAtivosByTermo(pagina, limite, termo, ordenacao);
      return ResponseEntity.ok(usuariosPaginadoDTO);
   }


}
