package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.repository.CategoriaRepository;
import com.reportai.reportaiserver.utils.Validacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

   @Autowired
   private CategoriaRepository repository;

   @Autowired
   private Validacoes validacoes;

   public Categoria salvar(Categoria categoria) {
      validacoes.validarCategoria(categoria);
      return repository.save(categoria);
   }

   public Categoria buscarPorId(Long id) {
     Optional<Categoria> categoria = repository.findById(id);
      if (categoria.isEmpty()) {
         throw new CustomException(ErrorDictionary.CATEGORIA_NAO_ENCONTRADA);
      }
      return categoria.get();
   }

   public List<Categoria> buscarTodos() {
      return repository.findAll();
   }

   public void removerPorId(Long id) {
      Categoria categoria = buscarPorId(id);
      repository.deleteById(categoria.getId());
   }


}
