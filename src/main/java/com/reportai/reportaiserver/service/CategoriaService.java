package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

   @Autowired
   private CategoriaRepository repository;

   public Categoria save(Categoria categoria) {
      return repository.save(categoria);
   }

   public Categoria findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public List<Categoria> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      repository.deleteById(id);
   }


}
