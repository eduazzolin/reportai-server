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
public class UsuariosAdminPaginadoDTO {
   private int pagina;
   private int limite;
   private int totalPaginas;
   private int totalUsuarios;
   private List<UsuarioListagemAdminProjection> usuarios;
}
