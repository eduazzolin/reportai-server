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
      return dto;

   }

   public static Usuario toEntity(UsuarioDTO dto) {

      Usuario entity = new Usuario();
      entity.setId(dto.getId());
      entity.setNome(dto.getNome());
      entity.setEmail(dto.getEmail());
      entity.setCpf(dto.getCpf());
      return entity;

   }

}
