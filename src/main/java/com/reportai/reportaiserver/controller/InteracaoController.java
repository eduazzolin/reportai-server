package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.InteracaoRegistroSimplesDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.InteracaoService;
import com.reportai.reportaiserver.service.JwtService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.reportai.reportaiserver.model.Interacao.TipoInteracao.RELEVANTE;

@RestController
@RequestMapping("/interacoes")
@RequiredArgsConstructor
public class InteracaoController {

   @Autowired
   private InteracaoService service;

   @Autowired
   private RegistroService registroService;

   @Autowired
   private JwtService jwtService;

   /**
    * Busca todas as interações do tipo relevante por registro.
    * Este endpoint é ABERTO.
    *
    * @param idRegistro ID do registro
    * @return lista de interações relevantes em formato DTO
    */
   @GetMapping("/relevantes/{idRegistro}")
   public ResponseEntity<?> buscarInteracoesRelevante(@PathVariable Long idRegistro) {
      Registro registro = registroService.buscarPorId(idRegistro);
      return ResponseEntity.ok(service.BuscarDTOsPorRegistroETipo(registro, RELEVANTE));
   }

   /**
    * Busca um InteracaoRegistroSimplesDTO por registro e usuário.
    * Este DTO contém a quantidade de interações relevantes, irrelevantes e concluídas do registro e traz
    * também uma flag que indica se o usuário marcou o registro como relevante, irrelevante ou concluído.
    *
    * @param idRegistro ID do registro
    * @return InteracaoRegistroSimplesDTO com as informações do registro e do usuário
    */
   @GetMapping("/{idRegistro}")
   public ResponseEntity<?> buscarInteracaoRegistroSimplesDTOPorIdRegistro(@PathVariable Long idRegistro, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
      Registro registro = registroService.buscarPorId(idRegistro);
      Usuario usuarioRequisitante = null;
      if (authorizationHeader != null) {
         usuarioRequisitante =  jwtService.obterUsuarioRequisitante(authorizationHeader);

      }
      InteracaoRegistroSimplesDTO interacaoRegistroSimplesDTO = service.buscarDTORegistroSimplesPorRegistroEUsuario(registro, usuarioRequisitante);
      return ResponseEntity.ok(interacaoRegistroSimplesDTO);
   }

   /**
    * Salva uma interação no banco de dados.
    *
    * @param interacao
    * @return interacao salva
    */
   @PostMapping()
   public ResponseEntity<?> salvar(@RequestBody Interacao interacao, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      interacao.setUsuario(usuarioRequisitante);
      return ResponseEntity.ok(service.salvar(interacao));
   }

   /**
    * Marca uma interação como removida.
    *
    * @param id ID da interação a ser removida
    * @return Resposta HTTP 200 OK
    */
   @DeleteMapping("/{id}")
   public ResponseEntity<?> remover(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      Interacao interacao = service.buscarPorId(id);

      if (!interacao.getUsuario().getId().equals(usuarioRequisitante.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      service.remover(interacao);
      return ResponseEntity.ok().build();
   }

}
