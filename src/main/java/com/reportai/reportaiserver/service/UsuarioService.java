package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

   @Autowired
   private UsuarioRepository repository;
   
    public Usuario save(Usuario usuario) {
      return repository.save(usuario);
   }

   public Usuario findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public List<Usuario> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      repository.deleteById(id);
   }
}
