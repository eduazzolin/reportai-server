package com.reportai.reportaiserver.utils;

//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.reportai.reportaiserver.model.Usuario;

import java.util.Objects;

public class UsuarioUtils {

   public static Usuario criptografarSenha(Usuario usuario) {
      // #ToDo #SpringSecurity
      // String senhaCriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
      String senhaCriptografada = usuario.getSenha();
      usuario.setSenha(senhaCriptografada);
      return usuario;
   }

   public static boolean senhasBatem(String senha, String senhaEncoded) {
      // #ToDo #SpringSecurity
      // return new BCryptPasswordEncoder().matches(senha, senhaEncoded);
      return Objects.equals(senha, senhaEncoded);
   }

}