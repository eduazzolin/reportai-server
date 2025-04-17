package com.reportai.reportaiserver.mapper;


import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.dto.UsuarioResumidoDTO;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;

public class RegistroMapper {

   public static RegistroDTO toDTO(Registro entity) {

      RegistroDTO dto = new RegistroDTO();
      dto.setId(entity.getId());
      dto.setTitulo(entity.getTitulo());
      dto.setDescricao(entity.getDescricao());
      dto.setBairro(entity.getBairro());
      dto.setLocalizacao(entity.getLocalizacao());
      dto.setLatitude(entity.getLatitude());
      dto.setLongitude(entity.getLongitude());
      dto.setDtCriacao(entity.getDtCriacao());
      dto.setDtModificacao(entity.getDtModificacao());
      dto.setDtConclusao(entity.getDtConclusao());
      dto.setDtExclusao(entity.getDtExclusao());
      dto.setIsConcluido(entity.getIsConcluido());
      dto.setIsDeleted(entity.getIsDeleted());

      dto.setCategoria(entity.getCategoria());

      dto.setImagens(entity.getImagens());

      UsuarioResumidoDTO usuarioResumidoDTO = new UsuarioResumidoDTO();
      usuarioResumidoDTO.setId(entity.getUsuario().getId());
      usuarioResumidoDTO.setNome(entity.getUsuario().getNome());
      dto.setUsuario(usuarioResumidoDTO);

      Integer interacoesRelevante = 0;
      Integer interacoesIrrelevante = 0;
      Integer interacoesConcluido = 0;

      if (entity.getInteracoes() != null) {
         for (Interacao interacao : entity.getInteracoes()) {
            if (interacao.getTipo().equals(Interacao.TipoInteracao.RELEVANTE) && !interacao.getIsDeleted()) {
               interacoesRelevante++;
            } else if (interacao.getTipo().equals(Interacao.TipoInteracao.IRRELEVANTE) && !interacao.getIsDeleted()) {
               interacoesIrrelevante++;
            } else if (interacao.getTipo().equals(Interacao.TipoInteracao.CONCLUIDO) && !interacao.getIsDeleted()) {
               interacoesConcluido++;
            }
         }
      }
      dto.setInteracoesRelevante(interacoesRelevante);
      dto.setInteracoesIrrelevante(interacoesIrrelevante);
      dto.setInteracoesConcluido(interacoesConcluido);

      return dto;
   }


}
