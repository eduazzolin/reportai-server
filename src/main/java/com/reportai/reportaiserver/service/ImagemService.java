package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.repository.ImagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImagemService {

   @Autowired
   private ImagemRepository repository;
   
   
    public Imagem save(Imagem imagem) {
      return repository.save(imagem);
   }

   public Imagem findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public List<Imagem> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      repository.deleteById(id);
   }
}
