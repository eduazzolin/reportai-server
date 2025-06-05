package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.SegundoFatorAutenticacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SegundoFatorAutenticacaoRepository extends JpaRepository<SegundoFatorAutenticacao, Long> {

   Optional<SegundoFatorAutenticacao> findTop1ByUsuarioIdOrderByDtCriacaoDesc(Long usuarioId);


}
