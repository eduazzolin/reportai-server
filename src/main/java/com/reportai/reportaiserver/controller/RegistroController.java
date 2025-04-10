package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.MeusRegistrosDTO;
import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.dto.RegistrosAdminPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.RegistroMapper;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.InteracaoService;
import com.reportai.reportaiserver.service.RegistroService;
import com.reportai.reportaiserver.service.UsuarioService;
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
   @Autowired
   private UsuarioService usuarioService;

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

   @GetMapping("/admin")
   public ResponseEntity<?> buscarPorTermo(
           @RequestParam(defaultValue = "") String idNome,
           @RequestParam(defaultValue = "0") Long idUsuario,
           @RequestParam(defaultValue = "0") Long idCategoria,
           @RequestParam(defaultValue = "") String bairro,
           @RequestParam(defaultValue = "") String status,
           @RequestParam int pagina,
           @RequestParam int limite,
           @RequestParam(defaultValue = "dtCriacao") String ordenacao) {


      Usuario usuario = usuarioService.findAtivosById(2L); // #ToDo #SpringSecurity

      if (!(usuario.getRole().equals(Usuario.Roles.ADMIN))) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      RegistrosAdminPaginadoDTO registrosAdminPaginadoDTO = service.adminSearchByTerms(idNome, idUsuario, idCategoria, bairro, status, pagina, limite, ordenacao);


      return ResponseEntity.ok(registrosAdminPaginadoDTO);
   }


}
