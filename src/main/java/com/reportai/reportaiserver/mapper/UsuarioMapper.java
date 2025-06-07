package com.reportai.reportaiserver.mapper;

import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.model.Usuario;

public class UsuarioMapper {


   public static UsuarioDTO toDTO(Usuario entity) {

      UsuarioDTO dto = new UsuarioDTO();
      dto.setId(entity.getId());
      dto.setNome(entity.getNome());
      dto.setEmail(entity.getEmail());
      dto.setCpf(entity.getCpf());
      dto.setRole(entity.getRole());
      dto.setDtCriacao(entity.getDtCriacao());
      dto.setDtModificacao(entity.getDtModificacao());
      dto.setSegundoFator(entity.getSegundoFator());
      return dto;

   }

}
