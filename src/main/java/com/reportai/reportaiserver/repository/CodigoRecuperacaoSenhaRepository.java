package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.CodigoRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoRecuperacaoSenhaRepository extends JpaRepository<CodigoRecuperacaoSenha, Long> {

  CodigoRecuperacaoSenha findTop1ByUsuarioIdOrderByDtCriacaoDesc(Long usuarioId);


}
