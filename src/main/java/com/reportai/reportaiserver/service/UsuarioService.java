package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.UsuarioMapper;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import com.reportai.reportaiserver.utils.UsuarioUtils;
import com.reportai.reportaiserver.utils.Validacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

   @Autowired
   private UsuarioRepository repository;

   @Autowired
   private Validacoes validacoes;

   public Usuario save(Usuario usuario) {
      validacoes.validarUsuario(usuario);
      usuario.setRole("USER");
      if (usuario.getId() != null) {
         usuario.setCpf(repository.findById(usuario.getId()).get().getCpf());
      }
      return repository.save(usuario);
   }

   public Usuario autenticar(String email, String senha) {

      Optional<Usuario> usuario = repository.findByEmail(email);

      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }

      if (!UsuarioUtils.senhasBatem(senha, usuario.get().getSenha())) {
         throw new CustomException(ErrorDictionary.SENHA_INVALIDA);
      }

      if (usuario.get().getIsDeleted()) {
         throw new CustomException(ErrorDictionary.USUARIO_DELETADO);
      }

      return usuario.get();
   }

   public UsuarioDTO findDTOById(Long id) {
      Optional<Usuario> usuario = repository.findById(id);
      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }
      return UsuarioMapper.toDTO(usuario.get());
   }

   public List<Usuario> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      Optional<Usuario> usuario = repository.findById(id);
      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }

      usuario.get().setIsDeleted(true);
      repository.save(usuario.get());
   }
}
