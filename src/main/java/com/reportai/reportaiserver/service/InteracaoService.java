package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.InteracaoDTO;
import com.reportai.reportaiserver.dto.InteracaoRegistroSimplesDTO;
import com.reportai.reportaiserver.mapper.InteracaoMapper;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.InteracaoRepository;
import com.reportai.reportaiserver.utils.Validacoes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
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

   @Autowired
   private EntityManager entityManager;

   /**
    * Salva uma interação no banco de dados.
    * Se a interação for do tipo CONCLUIDO, chama a proc SP_INCLUIR_RESOLUCAO_AUTOMATICA.
    * Se a interação for do tipo RELEVANTE ou IRRELEVANTE, verifica se já existe uma interação do outro tipo
    * para o mesmo registro e usuário, e se existir, marca essa interação como deletada.
    *
    * @param interacao
    * @return interacao salva
    */
   public Interacao salvar(Interacao interacao) {
      validacoes.validarInteracao(interacao);
      interacao = repository.save(interacao);

      if (interacao.getTipo().equals(Interacao.TipoInteracao.RELEVANTE)) {
         Interacao interacaoIrrelevante = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(interacao.getRegistro(), Interacao.TipoInteracao.IRRELEVANTE, interacao.getUsuario(), false);
         if (interacaoIrrelevante != null) {
            interacaoIrrelevante.setIsDeleted(true);
            repository.save(interacaoIrrelevante);
         }
      }

      if (interacao.getTipo().equals(Interacao.TipoInteracao.IRRELEVANTE)) {
         Interacao interacaoRelevante = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(interacao.getRegistro(), Interacao.TipoInteracao.RELEVANTE, interacao.getUsuario(), false);
         if (interacaoRelevante != null) {
            interacaoRelevante.setIsDeleted(true);
            repository.save(interacaoRelevante);
         }
      }

      if (interacao.getTipo().equals(Interacao.TipoInteracao.CONCLUIDO)) {
         chamarProcedureIncluirResolucaoAutomatica(interacao.getRegistro().getId());
      }
      return interacao;
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
      dto.setQtRelevante(repository.countByRegistroAndTipoAndIsDeleted(registro, Interacao.TipoInteracao.RELEVANTE, false));
      dto.setQtIrrelevante(repository.countByRegistroAndTipoAndIsDeleted(registro, Interacao.TipoInteracao.IRRELEVANTE, false));
      dto.setQtConcluido(repository.countByRegistroAndTipoAndIsDeleted(registro, Interacao.TipoInteracao.CONCLUIDO, false));

      if (usuario != null) {
         dto.setIdUsuario(usuario.getId());
         Interacao interacaoRelevante = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(registro, Interacao.TipoInteracao.RELEVANTE, usuario, false);
         Interacao interacaoIrrelevante = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(registro, Interacao.TipoInteracao.IRRELEVANTE, usuario, false);
         Interacao interacaoConcluido = repository.findByRegistroAndTipoAndUsuarioAndIsDeleted(registro, Interacao.TipoInteracao.CONCLUIDO, usuario, false);

         dto.setUsuarioInteracaoIdRelevante(interacaoRelevante != null ? interacaoRelevante.getId() : null);
         dto.setUsuarioInteracaoIdIrrelevante(interacaoIrrelevante != null ? interacaoIrrelevante.getId() : null);
         dto.setUsuarioInteracaoIdConcluido(interacaoConcluido != null ? interacaoConcluido.getId() : null);
      }

      return dto;
   }


   public void chamarProcedureIncluirResolucaoAutomatica(Long idRegistro) {
      StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SP_INCLUIR_RESOLUCAO_AUTOMATICA");
      query.registerStoredProcedureParameter("p_id_registro", Long.class, ParameterMode.IN);
      query.setParameter("p_id_registro", idRegistro);
      query.execute();
   }
}
