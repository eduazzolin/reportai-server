package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

   @Autowired
   private UsuarioRepository usuarioRepository;


   @Override
   public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      Usuario usuarioEncontrado = usuarioRepository.findByEmail(email)
              .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

      return User.builder()
              .username(usuarioEncontrado.getEmail())
              .password(usuarioEncontrado.getSenha())
              .roles(usuarioEncontrado.getRole().toString())
              .build();
   }
}
