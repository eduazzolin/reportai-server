package com.reportai.reportaiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeusRegistrosDTO {


   private int pagina;
   private int limite;
   private int totalPaginas;
   private Long totalRegistros;
   private List<RegistroDTO> registros;

}
