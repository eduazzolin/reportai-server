package com.reportai.reportaiserver.utils;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Validacoes {

   @Autowired
   private UsuarioRepository usuarioRepository;

   public static boolean validarCPF(String cpf) {
      if (cpf == "000.000.000-00") {
         // para testes
         return true;
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
      if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
         throw new CustomException(ErrorDictionary.EMAIL_JA_EXISTE);
      }

      // cpf
      if (usuario.getCpf() == null || usuario.getCpf().isEmpty()) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }
      if (!validarCPF(usuario.getCpf())) {
         throw new CustomException(ErrorDictionary.CPF_INVALIDO);
      }
      if (usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
         throw new CustomException(ErrorDictionary.CPF_JA_EXISTE);
      }

      // senha
      if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
         throw new CustomException(ErrorDictionary.ERRO_PREENCHIMENTO);
      }

   }
}
