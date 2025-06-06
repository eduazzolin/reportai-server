package com.reportai.reportaiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoRegistroSimplesDTO {

   private Long idRegistro;
   private Long idUsuario;
   private Long qtRelevante;
   private Long qtIrrelevante;
   private Long qtConcluido;
   private Long usuarioInteracaoIdRelevante;
   private Long usuarioInteracaoIdIrrelevante;
   private Long usuarioInteracaoIdConcluido;

}
