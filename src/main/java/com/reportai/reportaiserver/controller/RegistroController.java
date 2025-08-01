package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.MeusRegistrosDTO;
import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.dto.RegistrosAdminPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.RegistroMapper;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.JwtService;
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
   private UsuarioService usuarioService;

   @Autowired
   private JwtService jwtService;


   /**
    * Salva um registro no banco de dados.
    *
    * @param registro
    * @return DTO do registro salvo
    */
   @PostMapping
   public ResponseEntity<?> salvar(
           @RequestBody Registro registro,
           @RequestHeader("Authorization") String authorizationHeader
   ) {

      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);

      /*
       * se não for admin, coloca o usuario requisitante como dono do registro.
       * se for admin, mas for um registro novo, coloca o usuario requisitante como dono.
       */
      if (usuarioRequisitante.getRole() != Usuario.Roles.ADMIN) {
         registro.setUsuario(usuarioRequisitante);
      } else {
         if (registro.getId() == null) {
            registro.setUsuario(usuarioRequisitante);
         }
      }


      Registro registroSalvo = service.salvar(registro);
      RegistroDTO registroDTO = RegistroMapper.toDTO(registroSalvo);
      return ResponseEntity.ok(registroDTO);
   }


   /**
    * Marca um registro como concluído.
    *
    * @param id ID do registro a ser concluído
    * @return Resposta HTTP 200 OK
    */
   @PutMapping("/{id}/concluir")
   public ResponseEntity<?> concluir(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      Registro registro = service.buscarPorId(id);

      if (!registro.getUsuario().getId().equals(usuarioRequisitante.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }
      service.concluirPorId(registro);
      return ResponseEntity.ok().build();
   }

   @PutMapping("/{id}/ignorar-conclusao")
   public ResponseEntity<?> removerConclusaoProgramada(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      Registro registro = service.buscarPorId(id);

      if (!registro.getUsuario().getId().equals(usuarioRequisitante.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }
      service.removerConclusaoProgramada(registro);
      return ResponseEntity.ok().build();
   }


   /**
    * Busca registros por distância a partir de uma localização (latitude e longitude).
    * Para mais informações, consulte a procedure SP_REGISTROS_POR_DISTANCIA.
    * Este endpoint é ABERTO.
    *
    * @return lista de registros encontrados em formato DTO
    */
   @GetMapping("/distancia")
   public ResponseEntity<?> buscarPorDistancia(
           @RequestParam double latitude,
           @RequestParam double longitude,
           @RequestParam double distancia,
           @RequestParam String filtro,
           @RequestParam String ordenacao) {
      int limite = 100;

      List<Registro> registros = service.buscarPorDistancia(latitude, longitude, distancia, limite, filtro, ordenacao);
      ArrayList<RegistroDTO> registrosDTO = new ArrayList<>();

      for (Registro registro : registros) {
         RegistroDTO registroDTO = RegistroMapper.toDTO(registro);
         registrosDTO.add(registroDTO);
      }

      return ResponseEntity.ok(registrosDTO);
   }

   /**
    * Busca todos os registros do usuário logado.
    *
    * @param pagina
    * @param limite
    * @return meusRegistrosDTO
    */
   @GetMapping("/meus-registros")
   public ResponseEntity<?> buscarMeusRegistrosDTO(@RequestParam int pagina, @RequestParam int limite, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      MeusRegistrosDTO meusRegistrosDTO = service.buscarMeusRegistrosDTOPorUsuario(usuarioRequisitante, pagina, limite);
      return ResponseEntity.ok(meusRegistrosDTO);
   }

   /**
    * Busca o DTO do registro por ID. Desconsidera removidos.
    * Este endpoint é ABERTO.
    *
    * @param id ID do registro a ser buscado
    * @return
    */
   @GetMapping("/{id}")
   public RegistroDTO buscarDTOPorId(@PathVariable Long id) {
      Registro registro = service.buscarPorId(id);
      return RegistroMapper.toDTO(registro);
   }

   /**
    * Busca o registro por ID e devolve o objeto completo, para uso de desenvlvimento.
    *
    * @param id ID do registro a ser buscado
    * @return Registro
    */
   @GetMapping("/dev/{id}")
   public ResponseEntity<Registro> buscarPorId(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);
      Registro registro = service.buscarPorId(id);
      return ResponseEntity.ok(registro);
   }


   /**
    * Marca um registro como excluído.
    *
    * @param id ID do registro a ser excluído
    * @return
    */
   @DeleteMapping("/{id}")
   public ResponseEntity<?> remover(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      Registro registro = service.buscarPorId(id);

      if (!registro.getUsuario().getId().equals(usuarioRequisitante.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      service.remover(registro);
      return ResponseEntity.ok().build();
   }


   /**
    * Busca os registros para a página de administração.
    * Para mais informações, consulte a procedure SP_ADMIN_LISTAR_REGISTROS.
    *
    * @param idNome
    * @param idUsuario
    * @param idCategoria
    * @param bairro
    * @param status
    * @param pagina
    * @param limite
    * @param ordenacao
    * @return
    */
   @GetMapping("/admin")
   public ResponseEntity<?> buscarRegistrosAdminPaginadoDTOPorTermo(@RequestParam(defaultValue = "") String idNome, @RequestParam(defaultValue = "0") Long idUsuario, @RequestParam(defaultValue = "0") Long idCategoria, @RequestParam(defaultValue = "") String bairro, @RequestParam(defaultValue = "") String status, @RequestParam int pagina, @RequestParam int limite, @RequestParam(defaultValue = "dtCriacao") String ordenacao, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);
      RegistrosAdminPaginadoDTO registrosAdminPaginadoDTO = service.buscarRegistrosAdminpaginadoDTOPorTermos(idNome, idUsuario, idCategoria, bairro, status, pagina, limite, ordenacao);
      return ResponseEntity.ok(registrosAdminPaginadoDTO);
   }


}
