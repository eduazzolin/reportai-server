package com.reportai.reportaiserver.mapper;

import com.reportai.reportaiserver.dto.InteracaoRelevanteDTO;
import com.reportai.reportaiserver.model.Interacao;

public class InteracaoMapper {

   public static InteracaoRelevanteDTO toInteracaoRelevanteDTO (Interacao interacao){
      InteracaoRelevanteDTO dto = new InteracaoRelevanteDTO();
      dto.setId(interacao.getId());
      dto.setTipo(interacao.getTipo());
      dto.setUsuario(interacao.getUsuario().getNome());
      dto.setIdRegistro(interacao.getRegistro().getId());
      dto.setDtCriacao(interacao.getDtCriacao());
      return dto;
   }

}
