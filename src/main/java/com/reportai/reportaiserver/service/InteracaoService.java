package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.InteracaoRelevanteDTO;
import com.reportai.reportaiserver.mapper.InteracaoMapper;
import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
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

   public Interacao save(Interacao interacao) {
      validacoes.validarInteracao(interacao);
      return repository.save(interacao);
   }

   public Interacao findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public Interacao findByIdAndUsuarioId(Long id, Long usuarioId) {
      return repository.findByIdAndUsuarioId(id, usuarioId);
   }

   public List<Interacao> findAll() {
      return repository.findAll();
   }


   public void delete(Interacao interacao) {
      interacao.setIsDeleted(true);
      repository.save(interacao);
   }

   public List<InteracaoRelevanteDTO> findByRegistroAndTipo(Registro registro, Interacao.TipoInteracao tipoInteracao) {
      List<Interacao> interacoes = repository.findByRegistroAndTipo(registro, tipoInteracao);
      List<InteracaoRelevanteDTO> dtos = new ArrayList<>();

      for (Interacao interacoe : interacoes) {
         dtos.add(InteracaoMapper.toInteracaoRelevanteDTO(interacoe));
      }

      return dtos;
   }
}
