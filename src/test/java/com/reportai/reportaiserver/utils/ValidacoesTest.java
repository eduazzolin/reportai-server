package com.reportai.reportaiserver.utils;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.CategoriaRepository;
import com.reportai.reportaiserver.repository.InteracaoRepository;
import com.reportai.reportaiserver.repository.RegistroRepository;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ValidacoesTest {

   @InjectMocks
   private Validacoes validacoes;

   @Mock
   private UsuarioRepository usuarioRepository;

   @Mock
   private CategoriaRepository categoriaRepository;

   @Mock
   private RegistroRepository registroRepository;

   @Mock
   private InteracaoRepository interacaoRepository;

   @Test
   void deveValidarCpfValido() {
      String cpfValido = "52998224725";
      assertTrue(Validacoes.validarCPF(cpfValido));
   }

   @Test
   void deveRejeitarCpfInvalido() {
      String cpfInvalido = "12345678900";
      assertFalse(Validacoes.validarCPF(cpfInvalido));
   }

   @Test
   void deveLancarExcecaoQuandoNomeUsuarioInvalido() {
      Usuario usuario = new Usuario();
      usuario.setNome("");
      usuario.setEmail("email@teste.com");
      usuario.setCpf("52998224725");
      usuario.setSenha("123456");

      assertThrows(CustomException.class, () -> validacoes.validarUsuario(usuario));
   }

   @Test
   void deveLancarErroParaEmailInvalido() {
      Usuario usuario = new Usuario();
      usuario.setNome("Nome");
      usuario.setEmail("email-invalido");
      usuario.setCpf("52998224725");
      usuario.setSenha("123456");

      assertThrows(CustomException.class, () -> validacoes.validarUsuario(usuario));
   }

   @Test
   void deveLancarErroParaEmailDuplicado() {
      Usuario usuario = new Usuario();
      usuario.setNome("Nome");
      usuario.setEmail("email@teste.com");
      usuario.setCpf("52998224725");
      usuario.setSenha("123456");

      when(usuarioRepository.findByEmail("email@teste.com")).thenReturn(Optional.of(new Usuario()));

      assertThrows(CustomException.class, () -> validacoes.validarUsuario(usuario));
   }

   @Test
   void deveLancarErroDistanciaCentro() {
      Registro registro = new Registro();
      registro.setLocalizacao("Av. Teste");
      registro.setTitulo("Título");
      registro.setDescricao("Descrição válida");
      registro.setBairro("Bairro");
      registro.setLatitude(-19.9);
      registro.setLongitude(-43.9);

      assertThrows(CustomException.class, () -> validacoes.validarRegistro(registro));
   }

   @Test
   void deveValidarRegistroComSucesso() {
      Categoria categoria = new Categoria();
      categoria.setId(1L);

      Registro registro = new Registro();
      registro.setLocalizacao("Av. Teste");
      registro.setTitulo("Título");
      registro.setDescricao("Descrição válida");
      registro.setBairro("Bairro");
      registro.setCategoria(categoria);
      registro.setLatitude(-27.590744088315592);
      registro.setLongitude(-48.55018902283705);

      when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

      assertDoesNotThrow(() -> validacoes.validarRegistro(registro));
   }

   @Test
   void deveLancarErroSeInteracaoDuplicada() {
      Interacao interacao = new Interacao();
      Usuario usuario = new Usuario();
      usuario.setId(1L);
      Registro registro = new Registro();
      registro.setId(1L);

      interacao.setUsuario(usuario);
      interacao.setRegistro(registro);
      interacao.setTipo(Interacao.TipoInteracao.RELEVANTE);

      when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
      when(registroRepository.findById(1L)).thenReturn(Optional.of(registro));
      when(interacaoRepository.findByUsuarioAndRegistroAndTipoAndIsDeleted(usuario, registro, interacao.getTipo(), false))
              .thenReturn(new Interacao());

      assertThrows(CustomException.class, () -> validacoes.validarInteracao(interacao));
   }

   @Test
   void deveLancarErroParaFormatoDeImagemInvalido() {
      MultipartFile arquivo = mock(MultipartFile.class);
      when(arquivo.getContentType()).thenReturn("application/pdf");

      assertThrows(CustomException.class, () -> validacoes.validarImagem(arquivo));
   }

   @Test
   void deveLancarErroParaTamanhoDeImagemExcedido() {
      MultipartFile arquivo = mock(MultipartFile.class);
      when(arquivo.getContentType()).thenReturn("image/jpeg");
      when(arquivo.getSize()).thenReturn(6L * 1024 * 1024); // 6MB

      assertThrows(CustomException.class, () -> validacoes.validarImagem(arquivo));
   }
}
