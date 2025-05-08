package com.reportai.reportaiserver.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CriptografiaUtils {

   public static String criptografar(String texto) {
      return new BCryptPasswordEncoder().encode(texto);
   }

   public static boolean verificarCorrespondencia(String textoOriginal, String textoCriptografado) {
      return new BCryptPasswordEncoder().matches(textoOriginal, textoCriptografado);
   }

}
