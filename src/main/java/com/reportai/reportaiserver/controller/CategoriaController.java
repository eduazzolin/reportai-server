package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.CategoriaService;
import com.reportai.reportaiserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

   @Autowired
   private CategoriaService service;

   @Autowired
   private JwtService jwtService;

   /**
    * Salva uma categoria no banco de dados.
    * Este endpoint é restrito a usuários ADMIN.
    *
    * @param categoria
    * @param authorizationHeader
    * @return Categoria
    */
   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Categoria categoria, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);

      Categoria categoriaSalvo = service.salvar(categoria);
      return ResponseEntity.ok(categoriaSalvo);
   }

   /**
    * Busca todas as categorias do banco de dados.
    * Este endpoint é ABERTO.
    *
    * @return List<Categoria>
    */
   @GetMapping
   public ResponseEntity<?> buscarTodos() {
      return ResponseEntity.ok(service.buscarTodos());
   }

   /**
    * Busca uma categoria por ID.
    *
    * @param id ID da categoria a ser buscada
    * @return Categoria
    */
   @GetMapping("/{id}")
   public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
      return ResponseEntity.ok(service.buscarPorId(id));
   }

   /**
    * Remove uma categoria do banco de dados.
    * Este endpoint é restrito a usuários ADMIN.
    *
    * @param id ID da categoria a ser removida
    * @param authorizationHeader
    * @return Resposta HTTP 200 OK
    */
   @DeleteMapping("/{id}")
   public ResponseEntity<?> removerPorId(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
      Usuario usuarioRequisitante = jwtService.obterUsuarioRequisitante(authorizationHeader);
      jwtService.verificarSeUsuarioADMIN(usuarioRequisitante);

      service.removerPorId(id);
      return ResponseEntity.ok().build();
   }


}
