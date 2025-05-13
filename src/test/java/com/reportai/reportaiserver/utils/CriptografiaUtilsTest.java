package com.reportai.reportaiserver.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CriptografiaUtilsTest {

   @Test
   void deveCriptografarSenhaDiferenteDoTextoOriginal() {
      String senha = "senha";
      String senhaCriptografada = CriptografiaUtils.criptografar(senha);

      assertNotEquals(senha, senhaCriptografada);
      assertTrue(senhaCriptografada.startsWith("$2a$")); // prefixo do BCrypt
   }

   @Test
   void deveValidarCorrespondenciaEntreSenhaEHash() {
      String senha = "senha123";
      String senhaCriptografada = CriptografiaUtils.criptografar(senha);

      assertTrue(CriptografiaUtils.verificarCorrespondencia(senha, senhaCriptografada));
   }

   @Test
   void deveRetornarFalsoQuandoSenhasNaoCorrespondem() {
      String senhaOriginal = "senha123";
      String senhaErrada = "outraSenha";
      String senhaCriptografada = CriptografiaUtils.criptografar(senhaOriginal);

      assertFalse(CriptografiaUtils.verificarCorrespondencia(senhaErrada, senhaCriptografada));
   }
}