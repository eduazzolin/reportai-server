package com.reportai.reportaiserver.utils;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.CategoriaRepository;
import com.reportai.reportaiserver.repository.InteracaoRepository;
import com.reportai.reportaiserver.repository.RegistroRepository;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static com.reportai.reportaiserver.model.Interacao.TipoInteracao.*;
import static com.reportai.reportaiserver.utils.RegistroUtils.calcularDistanciaDoCentro;

@Component
public class Validacoes {

   @Autowired
   private UsuarioRepository usuarioRepository;

   @Autowired
   private CategoriaRepository categoriaRepository;

   @Autowired
   private RegistroRepository registroRepository;

   @Autowired
   private InteracaoRepository interacaoRepository;

   public static boolean validarCPF(String cpf) {
      if (0 == 0) {
         return true; //TODO dev
      }

      if (cpf == null) {
         return false;
      }

      // Remove caracteres não numéricos
      cpf = cpf.replaceAll("\\D", "");

      // Verifica se o tamanho é 11
      if (cpf.length() != 11) {
         return false;
      }

      // Elimina CPFs formados por sequências de mesmo dígito (ex: 11111111111)
      if (cpf.matches("(\\d)\\1{10}")) {
         return false;
      }

      // Cálculo do primeiro dígito verificador
      int soma = 0;
      for (int i = 0; i < 9; i++) {
         // multiplica cada dígito por (10 - i)
         soma += (cpf.charAt(i) - '0') * (10 - i);
      }
      int resto = 11 - (soma % 11);
      int digito1 = (resto >= 10) ? 0 : resto;

      // Cálculo do segundo dígito verificador
      soma = 0;
      for (int i = 0; i < 10; i++) {
         // multiplica cada dígito por (11 - i)
         soma += (cpf.charAt(i) - '0') * (11 - i);
      }
      resto = 11 - (soma % 11);
      int digito2 = (resto >= 10) ? 0 : resto;

      // Verifica se os dígitos calculados conferem com os do CPF
      return (digito1 == (cpf.charAt(9) - '0')) && (digito2 == (cpf.charAt(10) - '0'));
   }


   public void validarUsuario(Usuario usuario) {

      // nome
      if (usuario.getNome() == null || usuario.getNome().isEmpty() || usuario.getNome().length() > 255) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

      // email
      if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (!usuario.getEmail().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
         throw new CustomException(ErrorDictionary.EMAIL_INVALIDO);
      }
      if (usuario.getId() == null && usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
         throw new CustomException(ErrorDictionary.EMAIL_JA_EXISTE);
      }

      // cpf
      if (usuario.getCpf() == null || usuario.getCpf().isEmpty()) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (!validarCPF(usuario.getCpf())) {
         throw new CustomException(ErrorDictionary.CPF_INVALIDO);
      }
      if (usuario.getId() == null && usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
         throw new CustomException(ErrorDictionary.CPF_JA_EXISTE);
      }

      // senha
      if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

   }

   public void validarRegistro(Registro registro) {

      // localizacao
      if (registro.getLocalizacao() == null || registro.getLocalizacao().isEmpty() || registro.getLocalizacao().length() > 512) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

      // titulo
      if (registro.getTitulo() == null || registro.getTitulo().isEmpty() || registro.getTitulo().length() > 255) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

      // descricao
      if (registro.getDescricao() == null || registro.getDescricao().isEmpty() || registro.getDescricao().length() > 3500) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

      // categoria
      if (registro.getCategoria() == null) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (registro.getCategoria().getId() == null || !categoriaRepository.findById(registro.getCategoria().getId()).isPresent()) {
         throw new CustomException(ErrorDictionary.CATEGORIA_NAO_ENCONTRADA);
      }

      // latitude longitude
      if (registro.getLatitude() == null || registro.getLongitude() == null) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (calcularDistanciaDoCentro(registro.getLatitude(), registro.getLongitude()) > 30) {
         throw new CustomException(ErrorDictionary.DISTANCIA_INVALIDA);
      }

   }

   public void validarImagem(MultipartFile file) {


      // formato
      if (!file.getContentType().equals("image/jpeg") && !file.getContentType().equals("image/png")) {
         throw new CustomException(ErrorDictionary.FORMATO_INCORRETO);
      }


      // tamanho
      if (file.getSize() > 1024 * 1024 * 5) {
         throw new CustomException(ErrorDictionary.TAMANHO_MAXIMO);
      }

   }

   public void validarInteracao(Interacao interacao) {
      // usuario
      if (interacao.getUsuario() == null || interacao.getUsuario().getId() == null) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (!usuarioRepository.findById(interacao.getUsuario().getId()).isPresent()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }

      // registro
      if (interacao.getRegistro() == null || interacao.getRegistro().getId() == null) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (!registroRepository.findById(interacao.getRegistro().getId()).isPresent()) {
         throw new CustomException(ErrorDictionary.REGISTRO_NAO_ENCONTRADO);
      }

      // tipo
      if (interacao.getTipo() == null) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (interacao.getTipo() != CONCLUIDO && interacao.getTipo() != RELEVANTE && interacao.getTipo() != IRRELEVANTE) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

      // verificar se já existe interação
      Interacao interacaoExistente = interacaoRepository.findByUsuarioAndRegistroAndTipoAndIsDeleted(interacao.getUsuario(), interacao.getRegistro(), interacao.getTipo(), false);
      if (interacaoExistente != null) {
         throw new CustomException(ErrorDictionary.INTERACAO_DUPLICADA);
      }


   }
}
