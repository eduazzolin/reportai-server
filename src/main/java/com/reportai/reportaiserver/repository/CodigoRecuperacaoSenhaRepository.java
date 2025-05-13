package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.CodigoRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRecuperacaoSenhaRepository extends JpaRepository<CodigoRecuperacaoSenha, Long> {

   Optional<CodigoRecuperacaoSenha> findTop1ByUsuarioIdOrderByDtCriacaoDesc(Long usuarioId);


}
