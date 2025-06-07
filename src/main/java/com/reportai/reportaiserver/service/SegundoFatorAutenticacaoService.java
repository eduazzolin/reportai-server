package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.SegundoFatorAutenticacao;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.SegundoFatorAutenticacaoRepository;
import com.reportai.reportaiserver.utils.CriptografiaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SegundoFatorAutenticacaoService {

   @Autowired
   private SegundoFatorAutenticacaoRepository repository;


   public String gerarCodigoDescriptografado() {
      String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      SecureRandom random = new SecureRandom();
      int tamanho = 6;

      StringBuilder sb = new StringBuilder(tamanho);
      for (int i = 0; i < tamanho; i++) {
         int index = random.nextInt(CARACTERES.length());
         sb.append(CARACTERES.charAt(index));
      }
      return sb.toString();
   }

   public SegundoFatorAutenticacao salvar(Usuario usuario, String codigo) {
      codigo = CriptografiaUtils.criptografar(codigo);
      SegundoFatorAutenticacao segundoFatorAutenticacao = SegundoFatorAutenticacao
              .builder()
              .codigo(codigo)
              .usuario(usuario)
              .utilizado(false)
              .build();
      return repository.save(segundoFatorAutenticacao);
   }


   public SegundoFatorAutenticacao buscarUltimoPorUsuario(Usuario usuario) {
      Optional<SegundoFatorAutenticacao> segundoFatorAutenticacao = repository.findTop1ByUsuarioIdOrderByDtCriacaoDesc(usuario.getId());
      if (segundoFatorAutenticacao.isEmpty()) {
         throw new CustomException(ErrorDictionary.TOKEN_INVALIDO);
      }
      if (segundoFatorAutenticacao.get().getUtilizado()) {
         throw new CustomException(ErrorDictionary.TOKEN_INVALIDO);
      }
      return segundoFatorAutenticacao.get();
   }

   public void utilizarCodigo(SegundoFatorAutenticacao segundoFatorAutenticacao) {
      segundoFatorAutenticacao.setUtilizado(true);
      repository.save(segundoFatorAutenticacao);
   }


   public void verificarCodigo(Usuario usuario, String codigoSegundoFator) {

      SegundoFatorAutenticacao segundoFatorAutenticacao = buscarUltimoPorUsuario(usuario);

      if (!CriptografiaUtils.verificarCorrespondencia(codigoSegundoFator, segundoFatorAutenticacao.getCodigo())) {
         throw new CustomException(ErrorDictionary.CODIGO_VERIFICACAO_INVALIDO);
      }

      if (segundoFatorAutenticacao.getDtCriacao().plusMinutes(30).isBefore(LocalDateTime.now())) {
         throw new CustomException(ErrorDictionary.CODIGO_VERIFICACAO_EXPIRADO);
      }
      utilizarCodigo(segundoFatorAutenticacao);

   }
}
