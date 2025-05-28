package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.TokenDTO;
import com.reportai.reportaiserver.dto.TokenSenhaDTO;
import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.dto.UsuariosAdminPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.UsuarioMapper;
import com.reportai.reportaiserver.model.CodigoRecuperacaoSenha;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.CodigoRecuperacaoSenhaService;
import com.reportai.reportaiserver.service.EmailService;
import com.reportai.reportaiserver.service.JwtService;
import com.reportai.reportaiserver.service.UsuarioService;
import com.reportai.reportaiserver.utils.CriptografiaUtils;
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
   private EmailService emailService;

   @Autowired
   private CodigoRecuperacaoSenhaService codigoRecuperacaoSenhaService;

   @Autowired
   private JwtService jwtService;


   /**
    * Salva um usuário no banco de dados. Este endpoint é ABERTO para inserções.
    *
    * @param usuario
    * @param authorizationHeader
    * @return UsuarioDTO
    */
   @PostMapping
   public ResponseEntity<UsuarioDTO> salvar(@RequestBody Usuario usuario, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

      Usuario usuarioSalvo = null;

      if (usuario.getId() != null) {
         /* se o usuário já existe, só pode editar o nome ou email */
         Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);

         if (!usuarioRequisitante.getId().equals(usuario.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
            throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
         }

         Usuario usuarioExistente = service.buscarPorId(usuario.getId());

         usuarioExistente.setNome(usuario.getNome());
         usuarioExistente.setEmail(usuario.getEmail());

         usuario = usuarioExistente;
         usuarioSalvo = service.editar(usuario);

      } else {
         /* se o usuário não existe, cria um novo */
         usuario.setRole(Usuario.Roles.USUARIO);
         usuarioSalvo = service.salvar(usuario);
      }
      return ResponseEntity.ok(UsuarioMapper.toDTO(usuarioSalvo));
   }


   /**
    * Altera a senha de um usuário. Somente o próprio usuário ou um ADMIN pode alterar a senha.
    * Os outros campos do usuário não são alterados.
    *
    * @param usuario             Objeto Usuario com a nova senha
    * @param authorizationHeader
    * @return UsuarioDTO
    */
   @PostMapping("/alterar-senha")
   public ResponseEntity<UsuarioDTO> alterarSenha(@RequestBody Usuario usuario, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      if (!usuarioRequisitante.getId().equals(usuario.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }
      Usuario usuarioSalvo = service.alterarSenha(usuario);
      return ResponseEntity.ok(UsuarioMapper.toDTO(usuarioSalvo));
   }

   /**
    * Altera a senha de um usuário utilizando o token de recuperação de senha. Este endpoint é ABERTO.
    *
    * @param tokenSenhaDTO
    * @return UsuarioDTO
    */
   @PostMapping("/alterar-senha-token")
   public ResponseEntity<UsuarioDTO> alterarSenhaToken(@RequestBody TokenSenhaDTO tokenSenhaDTO) {
      Usuario usuario = service.buscarPorEmail(tokenSenhaDTO.getEmail());
      String token = tokenSenhaDTO.getToken();
      CodigoRecuperacaoSenha codigoRecuperacaoSenha = codigoRecuperacaoSenhaService.buscarUltimaPorUsuario(usuario);
      String tokenCriptografadoBanco = codigoRecuperacaoSenha.getCodigo();

      if (!CriptografiaUtils.verificarCorrespondencia(token, tokenCriptografadoBanco)) {
         throw new CustomException(ErrorDictionary.TOKEN_INVALIDO);
      }

      usuario.setSenha(tokenSenhaDTO.getSenha());
      usuario = service.alterarSenha(usuario);
      codigoRecuperacaoSenhaService.utilizarCodigo(codigoRecuperacaoSenha);
      return ResponseEntity.ok(UsuarioMapper.toDTO(usuario));
   }

   /**
    * Gera um token de recuperação de senha e envia por email. Este endpoint é ABERTO.
    *
    * @param usuario
    * @return 200
    */
   @PostMapping("/recuperar-senha")
   public ResponseEntity<?> recuperarSenha(@RequestBody Usuario usuario) {

      /* lança exceção se o usuário não existe */
      usuario = service.buscarPorEmail(usuario.getEmail());

      /* gerando o código */
      String codigo = CodigoRecuperacaoSenhaService.gerarCodigo();
      codigoRecuperacaoSenhaService.salvar(usuario, codigo);
      boolean resultadoEmail = emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), codigo);
      if (!resultadoEmail) {
         throw new CustomException(ErrorDictionary.ERRO_EMAIL);
      }
      return ResponseEntity.ok().build();
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

      TokenDTO tokenDTO = TokenDTO.builder().id(usuarioAutenticado.getId()).nomeUsuario(usuarioAutenticado.getNome()).token(jwtService.gerarToken(usuarioAutenticado)).horaExpiracao(dataExpiracao.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build();

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
   public ResponseEntity<?> buscarPorTermo(@RequestParam int pagina, @RequestParam int limite, @RequestParam String termo, @RequestParam String ordenacao, @RequestHeader("Authorization") String authorizationHeader) {

      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);

      UsuariosAdminPaginadoDTO usuariosAdminPaginadoDTO = service.buscarUsuariosAdminPaginadoDTOPorTermos(pagina, limite, termo, ordenacao);
      return ResponseEntity.ok(usuariosAdminPaginadoDTO);
   }


}
