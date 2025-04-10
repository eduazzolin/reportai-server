package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.dto.RegistroListagemAdminProjection;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

   @Query(value = "CALL SP_REGISTROS_POR_DISTANCIA(:latitude, :longitude, :distancia, :limite, :filtro, :ordenacao)", nativeQuery = true)
   List<Registro> findByDistance(
           @Param("latitude") double latitude,
           @Param("longitude") double longitude,
           @Param("distancia") double distancia,
           @Param("limite") int limite,
           @Param("filtro") String filtro,
           @Param("ordenacao") String ordenacao
   );

   @Query(value = "CALL SP_ADMIN_LISTAR_REGISTROS(:p_id_nome, :id_usuario, :id_categoria, :bairro, :status, :offset, :limite, :ordenacao)", nativeQuery = true)
   List<RegistroListagemAdminProjection> searchAdminRegistros(
           @Param("p_id_nome") String pIdNome,
           @Param("id_usuario") Long idUsuario,
           @Param("id_categoria") Long idCategoria,
           @Param("bairro") String bairro,
           @Param("status") String status,
           @Param("offset") int offset,
           @Param("limite") int limite,
           @Param("ordenacao") String ordenacao
   );

   @Query(value = "CALL SP_ADMIN_LISTAR_REGISTROS_COUNT(:p_id_nome, :id_usuario, :id_categoria, :bairro, :status)", nativeQuery = true)
   int countAdminRegistros(
           @Param("p_id_nome") String pIdNome,
           @Param("id_usuario") Long idUsuario,
           @Param("id_categoria") Long idCategoria,
           @Param("bairro") String bairro,
           @Param("status") String status
   );


   Page<Registro> findByUsuarioAndIsDeleted(Usuario usuario, boolean isDeleted, Pageable pageable);
}
