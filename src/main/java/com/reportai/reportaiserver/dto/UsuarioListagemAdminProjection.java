package com.reportai.reportaiserver.dto;

import java.time.LocalDateTime;

public interface UsuarioListagemAdminProjection {

   Long getId();

   String getNome();

   String getCpf();

   LocalDateTime getDtCriacao();

   LocalDateTime getDtModificacao();

   String getEmail();

   boolean getIsDeleted();

   String getRole();

   int getTotalRegistros();
}
