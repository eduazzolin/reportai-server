package com.reportai.reportaiserver.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CriptografiaUtils {

   public static String criptografar(String texto) {
      return new BCryptPasswordEncoder().encode(texto);
   }

   public static boolean verificarCorrespondencia(String texto1, String texto2) {
      return new BCryptPasswordEncoder().matches(texto1, texto2);
   }

}
