package com.reportai.reportaiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc()
@Transactional
@ActiveProfiles("test")
class InteracaoControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private RegistroService registroService;

   @Autowired
   private InteracaoService interacaoService;

   @Autowired
   private CategoriaService categoriaService;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private UsuarioService usuarioService;

   @Autowired
   private JwtService jwtService;

   private Usuario usuario;
   private Usuario usuarioAux;
   private String token;
   private Registro registro;
   private Interacao interacaoValida;
   private Interacao interacaoValidaAux;

   @BeforeEach
   void setUp() {
      usuario = Usuario.builder()
              .nome("Usuário Teste")
              .email("teste@teste.com")
              .senha("123456")
              .cpf("825.918.190-84")
              .isDeleted(false)
              .role(Usuario.Roles.USUARIO)
              .build();
      usuario = usuarioService.salvar(usuario);

      usuarioAux = Usuario.builder()
              .nome("Usuário Teste2")
              .email("teste2@teste.com")
              .senha("123456")
              .cpf("331.084.350-51")
              .isDeleted(false)
              .role(Usuario.Roles.USUARIO)
              .build();
      usuarioAux = usuarioService.salvar(usuarioAux);

      token = jwtService.gerarToken(usuario);


      Categoria categoriaValida = categoriaService.salvar(Categoria.builder()
              .nome("Teste")
              .build());

      registro = Registro.builder()
              .titulo("Registro de Teste")
              .descricao("Descrição do registro de teste")
              .localizacao("Rua Teste, 123")
              .bairro("Bairro Teste")
              .categoria(categoriaValida)
              .usuario(usuario)
              .latitude(-27.590744088315592)
              .longitude(-48.55018902283705)
              .isDeleted(false)
              .isConcluido(false)
              .build();
      registro = registroService.salvar(registro);

      interacaoValida = Interacao.builder()
              .registro(registro)
              .usuario(usuario)
              .tipo(Interacao.TipoInteracao.CONCLUIDO)
              .isDeleted(false)
              .build();

      interacaoValidaAux = Interacao.builder()
              .registro(registro)
              .usuario(usuarioAux)
              .tipo(Interacao.TipoInteracao.CONCLUIDO)
              .isDeleted(false)
              .build();

   }

   @Test
   void testSalvarInteracao() throws Exception {

      String interacaoJson = """
              {
                "registro": {
                  "id": %d
                },
                "usuario": {
                  "id": %d
                },
                "tipo": "%s",
                "isDeleted": false
              }
              """.formatted(registro.getId(), usuario.getId(), Interacao.TipoInteracao.CONCLUIDO);

      mockMvc.perform(post("/interacoes")
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(interacaoJson))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.tipo").value("CONCLUIDO"));
   }

   @Test
   void testDeveGerarConclusaoProgramada() throws Exception {
      interacaoValida.setRegistro(Registro.builder().id(8L).build()); // o registro 8 tem 4 interações
      interacaoValida.setId(null);
      interacaoService.salvar(interacaoValida);
      Thread.sleep(1000);
      LocalDateTime dtConclusaoProgramada = registroService.buscarDtConclusaoProgramadaPorId(8L);
      assert dtConclusaoProgramada != null;
   }

   @Test
   void testNaoDeveGerarConclusaoProgramada() throws Exception {
      interacaoValida.setRegistro(Registro.builder().id(3L).build()); // o registro 8 tem 1 interação
      interacaoValida.setId(null);
      interacaoService.salvar(interacaoValida);
      Thread.sleep(1000);
      LocalDateTime dtConclusaoProgramada = registroService.buscarDtConclusaoProgramadaPorId(8L);
      assert dtConclusaoProgramada == null;
   }

   @Test
   void testNaoDeveGerarConclusaoProgramadaSeJaExiste() throws Exception {
      interacaoValida.setRegistro(Registro.builder().id(8L).build());
      interacaoValida.setId(null);
      interacaoService.salvar(interacaoValida);
      Thread.sleep(1000);
      LocalDateTime dtConclusaoProgramada = registroService.buscarDtConclusaoProgramadaPorId(8L);
      Thread.sleep(50);

      interacaoValidaAux.setRegistro(Registro.builder().id(8L).build());
      interacaoValidaAux.setId(null);
      interacaoService.salvar(interacaoValidaAux);
      LocalDateTime dtConclusaoProgramadaNova = registroService.buscarDtConclusaoProgramadaPorId(8L);

      assert dtConclusaoProgramada.equals(dtConclusaoProgramadaNova);

   }

   @Test
   void testRemoverInteracao() throws Exception {
      Interacao interacao = Interacao.builder()
              .registro(registro)
              .usuario(usuario)
              .tipo(Interacao.TipoInteracao.RELEVANTE)
              .isDeleted(false)
              .build();
      interacao = interacaoService.salvar(interacao);

      mockMvc.perform(delete("/interacoes/" + interacao.getId())
                      .header("Authorization", "Bearer " + token))
              .andExpect(status().isOk());
   }
}
