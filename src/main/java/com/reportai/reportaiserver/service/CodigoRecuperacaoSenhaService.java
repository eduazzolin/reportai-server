package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.CodigoRecuperacaoSenha;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.CodigoRecuperacaoSenhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CodigoRecuperacaoSenhaService {

   @Autowired
   private CodigoRecuperacaoSenhaRepository repository;

   public static String gerarCodigo() {
      return UUID.randomUUID().toString();
   }
   public CodigoRecuperacaoSenha salvar(Usuario usuario, String codigo) {
      CodigoRecuperacaoSenha codigoRecuperacaoSenha = CodigoRecuperacaoSenha
              .builder()
              .codigo(codigo)
              .usuario(usuario)
              .build();
      return repository.save(codigoRecuperacaoSenha);
   }

   public CodigoRecuperacaoSenha buscarUltimaPorUsuario(Usuario usuario) {
      return repository.findTop1ByUsuarioIdOrderByDtCriacaoDesc(usuario.getId());
   }


}
