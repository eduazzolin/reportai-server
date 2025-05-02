package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.ImagemService;
import com.reportai.reportaiserver.service.JwtService;
import com.reportai.reportaiserver.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/imagens")
@RequiredArgsConstructor
public class ImagemController {

   @Autowired
   private ImagemService service;


   @Autowired
   private RegistroService registroService;

   @Autowired
   private JwtService jwtService;


   /**
    * Busca uma imagem por ID.
    *
    * @param id ID da imagem a ser buscada
    * @return Imagem do banco de dados
    */
   @GetMapping("/{id}")
   public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
      return ResponseEntity.ok(service.buscarPorId(id));
   }

   /**
    * Remove uma imagem do banco de dados fisicamente.
    *
    * @param id ID da imagem a ser removida
    * @return Resposta HTTP 200 OK
    */
   @DeleteMapping("/{id}")
   public ResponseEntity<?> removerPorId(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      Imagem imagem = service.buscarPorId(id);

      if (!imagem.getRegistro().getUsuario().getId().equals(usuarioRequisitante.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      service.remover(imagem);
      return ResponseEntity.ok().build();
   }

   /**
    * Salva uma imagem no banco de dados e no Google Cloud Storage.
    *
    * @param file
    * @param idRegistro
    * @return imagem salva
    * @throws IOException
    */
   @PostMapping
   public ResponseEntity<Imagem> salvar(@RequestParam("file") MultipartFile file, @RequestParam("idRegistro") Long idRegistro, @RequestHeader("Authorization") String authorizationHeader) throws IOException {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      Registro registro = registroService.buscarPorId(idRegistro);

      if (!registro.getUsuario().getId().equals(usuarioRequisitante.getId()) && !usuarioRequisitante.getRole().equals(Usuario.Roles.ADMIN)) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      Imagem imagemSalva = service.salvar(file, idRegistro);
      return ResponseEntity.ok(imagemSalva);

   }

}
