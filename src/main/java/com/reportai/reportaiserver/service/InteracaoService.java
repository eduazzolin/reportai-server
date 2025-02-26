package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.repository.InteracaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteracaoService {

   @Autowired
   private InteracaoRepository repository;
   
    public Interacao save(Interacao interacao) {
      return repository.save(interacao);
   }

   public Interacao findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public List<Interacao> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      repository.deleteById(id);
   }
}
