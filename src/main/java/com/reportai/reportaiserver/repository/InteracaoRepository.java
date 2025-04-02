package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Interacao;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {

   Interacao findByUsuarioAndRegistro(Usuario usuario, Registro registro);

   Interacao findByIdAndUsuarioId(Long id, Long usuarioId);

   List<Interacao> findByRegistroAndTipo(Registro registro, Interacao.TipoInteracao tipoInteracao);

   Long countByRegistroAndTipo(Registro registro, Interacao.TipoInteracao tipoInteracao);

   Boolean existsByRegistroAndTipoAndUsuario(Registro registro, Interacao.TipoInteracao tipoInteracao, Usuario usuario);

   Interacao findByRegistroAndTipoAndUsuarioAndIsDeleted(Registro registro, Interacao.TipoInteracao tipoInteracao, Usuario usuario, boolean isDeleted);


   Interacao findByUsuarioAndRegistroAndTipoAndIsDeleted(Usuario usuario, Registro registro, Interacao.TipoInteracao tipo, boolean isDeleted);
}
