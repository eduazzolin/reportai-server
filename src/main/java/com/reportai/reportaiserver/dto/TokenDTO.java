package com.reportai.reportaiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {

   private Long id;
   private String horaExpiracao;
   private String nomeUsuario;
   private String token;
   private Status status;

   public enum Status {
      AGUARDANDO_SEGUNDO_FATOR, OK
   }


}
