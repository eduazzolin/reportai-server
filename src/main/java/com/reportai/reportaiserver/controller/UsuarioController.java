package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.TokenDTO;
import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.dto.UsuariosAdminPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.JwtService;
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

   @Autowired
   private UsuarioService usuarioService;

   @Autowired
   private JwtService jwtService;


   /**
    * Salva um usuário no banco de dados. Este endpoint é ABERTO.
    *
    * @param usuario
    * @param authorizationHeader
    * @return
    */
   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Usuario usuario, @RequestHeader("Authorization") String authorizationHeader) {

      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);

      if (usuario.getId() != null) {

         if (!usuarioRequisitante.getId().equals(usuario.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
            throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
         }


         Usuario usuarioExistente = service.buscarPorId(usuario.getId());
         usuarioExistente.setNome(usuario.getNome());
         usuarioExistente.setEmail(usuario.getEmail());
         if (usuario.getSenha() != null) {
            usuarioExistente.setSenha(usuario.getSenha());
         }
         usuario = usuarioExistente;
      }
      Usuario usuarioSalvo = service.salvar(usuario);
      return ResponseEntity.ok(usuarioSalvo);
   }


   /**
    * Remove um usuário do banco de dados.
    *
    * @param id                  ID do usuário a ser removido
    * @param authorizationHeader
    * @return Resposta HTTP 200 OK
    */
   @DeleteMapping("/{id}")
   public ResponseEntity<?> deletar(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      if (!usuarioRequisitante.getId().equals(id) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }
      service.removerPorId(id);
      return ResponseEntity.ok().build();
   }


   /**
    * Autentica um usuário no sistema. Este endpoint é ABERTO.
    *
    * @param usuario
    * @return TokenDTO com o token JWT gerado
    */
   @PostMapping("/autenticar")
   public ResponseEntity<?> autenticar(@RequestBody Usuario usuario) {
      Usuario usuarioAutenticado = service.autenticar(usuario.getEmail(), usuario.getSenha());
      LocalDateTime dataExpiracao = jwtService.gerarDataExpiracao();

      TokenDTO tokenDTO = TokenDTO
              .builder()
              .id(usuarioAutenticado.getId())
              .nomeUsuario(usuarioAutenticado.getNome())
              .token(jwtService.gerarToken(usuarioAutenticado))
              .horaExpiracao(dataExpiracao.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
              .build();

      return ResponseEntity.ok(tokenDTO);
   }


   /**
    * Busca um usuário por ID. Só o próprio usuário ou um ADMIN pode buscar o usuário.
    *
    * @param id                  ID do usuário a ser buscado
    * @param authorizationHeader
    * @return UsuárioDTO
    */
   @GetMapping("/{id}")
   public ResponseEntity<?> buscarDTOPorId(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      if (!usuarioRequisitante.getId().equals(id) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }
      UsuarioDTO usuario = service.buscarDTOPorId(id);
      return ResponseEntity.ok(usuario);
   }


   /**
    * Busca a listagem de usuários paginada. Somente um ADMIN pode acessar este endpoint.
    *
    * @param pagina
    * @param limite
    * @param termo
    * @param ordenacao
    * @param authorizationHeader
    * @return UsuariosAdminPaginadoDTO
    */
   @GetMapping("/admin")
   public ResponseEntity<?> buscarPorTermo(
           @RequestParam int pagina,
           @RequestParam int limite,
           @RequestParam String termo,
           @RequestParam String ordenacao,
           @RequestHeader("Authorization") String authorizationHeader) {

      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);

      UsuariosAdminPaginadoDTO usuariosAdminPaginadoDTO = service.buscarUsuariosAdminPaginadoDTOPorTermos(pagina, limite, termo, ordenacao);
      return ResponseEntity.ok(usuariosAdminPaginadoDTO);
   }


}
