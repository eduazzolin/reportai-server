package com.reportai.reportaiserver.dto;

import com.reportai.reportaiserver.model.Interacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoDTO {

   private Long id;
   private Interacao.TipoInteracao tipo;
   private String usuario;
   private Long idRegistro;
   private LocalDateTime dtCriacao;

}
