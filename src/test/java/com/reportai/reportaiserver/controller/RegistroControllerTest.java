package com.reportai.reportaiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.CategoriaService;
import com.reportai.reportaiserver.service.JwtService;
import com.reportai.reportaiserver.service.RegistroService;
import com.reportai.reportaiserver.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc()
@Transactional
class RegistroControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private RegistroService registroService;

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
   private Registro registroValido;
   private Registro registroInvalido;

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

      Categoria categoriaValida = categoriaService.salvar(Categoria.builder()
              .nome("Teste")
              .build());

      registroValido = Registro.builder()
              .titulo("Registro de Teste")
              .descricao("Descrição do registro de teste")
              .localizacao("Rua Teste, 123")
              .bairro("Bairro Teste")
              .categoria(categoriaValida)
              .usuario(usuarioAdmin)
              .latitude(-27.590744088315592)
              .longitude(-48.55018902283705)
              .isDeleted(false)
              .isConcluido(false)
              .build();

      registroInvalido = Registro.builder()
              .descricao("Descrição do registro de teste")
              .localizacao("Rua Teste, 123")
              .bairro("Bairro Teste")
              .categoria(categoriaValida)
              .usuario(usuarioAdmin)
              .isDeleted(false)
              .isConcluido(false)
              .build();

   }

   @Test
   void testDeveSalvarRegistro() throws Exception {
      mockMvc.perform(post("/registros")
                      .header("Authorization", "Bearer " + tokenValido)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(registroValido)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.titulo").value(registroValido.getTitulo()))
              .andExpect(jsonPath("$.descricao").value(registroValido.getDescricao()));
   }

   @Test
   void testDeveConcluirRegistroSeUsuarioProprietarioOuAdmin() throws Exception {
      Registro registro = registroService.salvar(registroValido);
      System.out.print("\n\n\n\n\n" + registro.getId() + "\n\n\n\n\n");
      mockMvc.perform(put("/registros/" + registro.getId() + "/concluir")
                      .header("Authorization", "Bearer " + tokenValido))
              .andExpect(status().isOk());
   }


   @Test
   void testDeveBuscarMeusRegistros() throws Exception {
      mockMvc.perform(get("/registros/meus-registros")
                      .header("Authorization", "Bearer " + tokenValido)
                      .param("pagina", "0")
                      .param("limite", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.registros").exists());
   }

   @Test
   void testDeveBuscarRegistroDTOporId() throws Exception {
      Registro registro = registroService.salvar(registroValido);
      mockMvc.perform(get("/registros/" + registro.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.titulo").value("Registro de Teste"));
   }


   @Test
   void testDeveRemoverRegistroSeProprietarioOuAdmin() throws Exception {
      Registro registro = registroService.salvar(registroValido);
      mockMvc.perform(delete("/registros/" + registro.getId())
                      .header("Authorization", "Bearer " + tokenValido))
              .andExpect(status().isOk());
   }

   @Test
   void testDeveBuscarRegistrosAdminPaginado() throws Exception {
      mockMvc.perform(get("/registros/admin")
                      .header("Authorization", "Bearer " + tokenValido)
                      .param("pagina", "0")
                      .param("limite", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.registros").exists());
   }

   @Test
   void testDeveRetornarErroAoSalvarRegistroInvalido() throws Exception {
      mockMvc.perform(post("/registros")
                      .header("Authorization", "Bearer " + tokenValido)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(registroInvalido)))
              .andExpect(status().isBadRequest());
   }

}
