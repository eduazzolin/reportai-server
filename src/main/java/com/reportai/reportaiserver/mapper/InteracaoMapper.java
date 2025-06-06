package com.reportai.reportaiserver.mapper;

import com.reportai.reportaiserver.dto.InteracaoDTO;
import com.reportai.reportaiserver.model.Interacao;

public class InteracaoMapper {

   public static InteracaoDTO toInteracaoDTO(Interacao interacao){
      InteracaoDTO dto = new InteracaoDTO();
      dto.setId(interacao.getId());
      dto.setTipo(interacao.getTipo());
      dto.setUsuario(interacao.getUsuario().getNome());
      dto.setIdRegistro(interacao.getRegistro().getId());
      dto.setDtCriacao(interacao.getDtCriacao());
      return dto;
   }

}
