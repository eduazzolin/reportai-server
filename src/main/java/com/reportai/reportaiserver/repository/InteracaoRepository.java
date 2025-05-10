package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {

   List<Interacao> findByRegistroAndTipoAndIsDeleted(Registro registro, Interacao.TipoInteracao tipoInteracao, boolean isDeleted);

   Long countByRegistroAndTipoAndIsDeleted(Registro registro, Interacao.TipoInteracao tipoInteracao, boolean isDeleted);

   Interacao findByRegistroAndTipoAndUsuarioAndIsDeleted(Registro registro, Interacao.TipoInteracao tipoInteracao, Usuario usuario, boolean isDeleted);

   Interacao findByUsuarioAndRegistroAndTipoAndIsDeleted(Usuario usuario, Registro registro, Interacao.TipoInteracao tipo, boolean isDeleted);

}
