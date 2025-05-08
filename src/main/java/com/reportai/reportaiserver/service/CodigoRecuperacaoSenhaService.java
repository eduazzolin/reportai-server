package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.CodigoRecuperacaoSenha;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.CodigoRecuperacaoSenhaRepository;
import com.reportai.reportaiserver.utils.CriptografiaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CodigoRecuperacaoSenhaService {

   @Autowired
   private CodigoRecuperacaoSenhaRepository repository;

   public static String gerarCodigo() {
      return UUID.randomUUID().toString();
   }

   public CodigoRecuperacaoSenha salvar(Usuario usuario, String codigo) {
      codigo = CriptografiaUtils.criptografar(codigo);
      CodigoRecuperacaoSenha codigoRecuperacaoSenha = CodigoRecuperacaoSenha
              .builder()
              .codigo(codigo)
              .usuario(usuario)
              .utilizado(false)
              .build();
      return repository.save(codigoRecuperacaoSenha);
   }


   public CodigoRecuperacaoSenha buscarUltimaPorUsuario(Usuario usuario) {
      Optional<CodigoRecuperacaoSenha> codigoRecuperacaoSenha = repository.findTop1ByUsuarioIdAndUtilizadoOrderByDtCriacaoDesc(usuario.getId(), false);
      if (codigoRecuperacaoSenha.isEmpty()) {
         throw new CustomException(ErrorDictionary.TOKEN_INVALIDO);
      }
      return codigoRecuperacaoSenha.get();
   }

   public void utilizarCodigo(CodigoRecuperacaoSenha codigoRecuperacaoSenha) {
      codigoRecuperacaoSenha.setUtilizado(true);
      repository.save(codigoRecuperacaoSenha);
   }


}
