package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.InteracaoDTO;
import com.reportai.reportaiserver.dto.InteracaoRegistroSimplesDTO;
import com.reportai.reportaiserver.mapper.InteracaoMapper;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.InteracaoRepository;
import com.reportai.reportaiserver.utils.Validacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InteracaoService {

   @Autowired
   private InteracaoRepository repository;

   @Autowired
   private Validacoes validacoes;

   /**
    * Salva uma interação no banco de dados.
    *
    * @param interacao
    * @return interacao salva
    */
   public Interacao salvar(Interacao interacao) {
      validacoes.validarInteracao(interacao);
      return repository.save(interacao);
   }

   /**
    * Busca uma interação por ID.
    *
    * @param id ID da interação a ser buscada
    * @return interacao encontrada
    */
   public Interacao buscarPorId(Long id) {
      return repository.findById(id).orElse(null);
   }

   /**
    * Busca todas as interações do banco de dados.
    *
    * @return lista de interações
    */
   public List<Interacao> buscarTodos() {
      return repository.findAll();
   }

   /**
    * Marca uma interação como removida.
    *
    * @param interacao interacao a ser removida
    */
   public void remover(Interacao interacao) {
      interacao.setIsDeleted(true);
      repository.save(interacao);
   }

   /**
    * Busca interações por registro e tipo de interação.
    *
    * @param registro      registro a ser buscado
    * @param tipoInteracao tipo de interação a ser buscada
    * @return lista de interações em formato DTO
    */
   public List<InteracaoDTO> BuscarDTOsPorRegistroETipo(Registro registro, Interacao.TipoInteracao tipoInteracao) {
      List<Interacao> interacoes = repository.findByRegistroAndTipoAndIsDeleted(registro, tipoInteracao, false);
      List<InteracaoDTO> dtos = new ArrayList<>();

      for (Interacao interacoe : interacoes) {
         dtos.add(InteracaoMapper.toInteracaoDTO(interacoe));
      }

      return dtos;
   }

   /**
    * Busca um InteracaoRegistroSimplesDTO por registro e usuário.
    * Este DTO contém a quantidade de interações relevantes, irrelevantes e concluídas do registro e
    * traz também uma flag que indica se o usuário marcou o registro como relevante, irrelevante ou concluído.
    *
    * @param registro
    * @param usuario
    * @return InteracaoRegistroSimplesDTO
    */
   public InteracaoRegistroSimplesDTO buscarDTORegistroSimplesPorRegistroEUsuario(Registro registro, Usuario usuario) {
      InteracaoRegistroSimplesDTO dto = new InteracaoRegistroSimplesDTO();
      dto.setIdRegistro(registro.getId());
      dto.setIdUsuario(usuario.getId());
      dto.setQtRelevante(repository.countByRegistroAndTipoAndIsDeleted(registro, Interacao.TipoInteracao.RELEVANTE, false));
      dto.setQtIrrelevante(repository.countByRegistroAndTipoAndIsDeleted(registro, Interacao.TipoInteracao.IRRELEVANTE, false));
      dto.setQtConcluido(repository.countByRegistroAndTipoAndIsDeleted(registro, Interacao.TipoInteracao.CONCLUIDO, false));

      Interacao interacaoRelevante = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(registro, Interacao.TipoInteracao.RELEVANTE, usuario, false);
      Interacao interacaoIrrelevante = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(registro, Interacao.TipoInteracao.IRRELEVANTE, usuario, false);
      Interacao interacaoConcluido = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(registro, Interacao.TipoInteracao.CONCLUIDO, usuario, false);

      dto.setUsuarioInteracaoIdRelevante(interacaoRelevante != null ? interacaoRelevante.getId() : null);
      dto.setUsuarioInteracaoIdIrrelevante(interacaoIrrelevante != null ? interacaoIrrelevante.getId() : null);
      dto.setUsuarioInteracaoIdConcluido(interacaoConcluido != null ? interacaoConcluido.getId() : null);
      return dto;
   }
}
