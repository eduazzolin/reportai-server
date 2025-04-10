package com.reportai.reportaiserver.dto;

import java.time.LocalDateTime;

public interface RegistroListagemAdminProjection {

   Long getId();

   String getTitulo();

   Long getUsuarioId();

   LocalDateTime getDtCriacao();

   LocalDateTime getDtModificacao();

   LocalDateTime getDtConclusao();

   String getCategoria();

   String getBairro();

   LocalDateTime getDtAteConclusao();

   Integer getQtConcluido();

   Integer getQtRelevante();

   Integer getQtIrrelevante();
}
