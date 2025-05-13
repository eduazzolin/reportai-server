package com.reportai.reportaiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.CategoriaService;
import com.reportai.reportaiserver.service.JwtService;
import com.reportai.reportaiserver.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc()
@Transactional
class CategoriaControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private CategoriaService categoriaService;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private UsuarioService usuarioService;

   @Autowired
   private JwtService jwtService;

   private Usuario usuarioAdmin;
   private String tokenValido;

   @BeforeEach
   void setUp() {
      usuarioAdmin = new Usuario();
      usuarioAdmin.setNome("Admin Test");
      usuarioAdmin.setEmail("admin@test.com");
      usuarioAdmin.setSenha("admin123");
      usuarioAdmin.setCpf("825.918.190-84");
      usuarioAdmin.setRole(Usuario.Roles.ADMIN);
      usuarioAdmin = usuarioService.salvar(usuarioAdmin);
      tokenValido = jwtService.gerarToken(usuarioAdmin);
   }

   @Test
   void testSalvarCategoria() throws Exception {
      Categoria categoria = new Categoria();
      categoria.setNome("Categoria Teste");

      mockMvc.perform(post("/categorias")
                      .header("Authorization", "Bearer " + tokenValido)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(categoria)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").exists())
              .andExpect(jsonPath("$.nome").value("Categoria Teste"));
   }

   @Test
   void testBuscarTodos() throws Exception {
      Categoria categoria = new Categoria();
      categoria.setNome("Categoria Existente");
      categoriaService.salvar(categoria);

      mockMvc.perform(get("/categorias"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
              .andExpect(jsonPath("$[*].nome", hasItem("Categoria Existente")));
   }

   @Test
   void testBuscarPorId() throws Exception {
      Categoria categoria = new Categoria();
      categoria.setNome("Categoria Busca");
      Categoria savedCategoria = categoriaService.salvar(categoria);

      mockMvc.perform(get("/categorias/{id}", savedCategoria.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(savedCategoria.getId()))
              .andExpect(jsonPath("$.nome").value("Categoria Busca"));
   }

   @Test
   void testRemoverPorId() throws Exception {
      Categoria categoria = new Categoria();
      categoria.setNome("Categoria Remover");
      Categoria savedCategoria = categoriaService.salvar(categoria);

      mockMvc.perform(delete("/categorias/{id}", savedCategoria.getId())
                      .header("Authorization", "Bearer " + tokenValido))
              .andExpect(status().isOk());

      mockMvc.perform(get("/categorias/{id}", savedCategoria.getId()))
              .andExpect(status().isBadRequest());
   }

   @Test
   void testDeveRetornarErroAoRemoverCategoriaInexistente() throws Exception {
      Long idInexistente = 999L;

      mockMvc.perform(delete("/categorias/{id}", idInexistente)
                      .header("Authorization", "Bearer " + tokenValido))
              .andExpect(status().isBadRequest());
   }

   @Test
   void testDeveRetornarErroAoBuscarCategoriaInexistente() throws Exception {
      Long idInexistente = 999L;

      mockMvc.perform(get("/categorias/{id}", idInexistente))
              .andExpect(status().isBadRequest());
   }

   @Test
   void testDeveRetornarErroAoSalvarCategoriaSemNome() throws Exception {
      Categoria categoria = new Categoria();
      categoria.setNome("");

      mockMvc.perform(post("/categorias")
                      .header("Authorization", "Bearer " + tokenValido)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(categoria)))
              .andExpect(status().isBadRequest());
   }

}
