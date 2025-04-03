package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

   @Query(value = """
               CALL SP_REGISTROS_POR_DISTANCIA(:latitude, :longitude, :distancia, :limite, :filtro, :ordenacao)
           """, nativeQuery = true)
   List<Registro> findByDistance(
           @Param("latitude") double latitude,
           @Param("longitude") double longitude,
           @Param("distancia") double distancia,
           @Param("limite") int limite,
           @Param("filtro") String filtro,
           @Param("ordenacao") String ordenacao
   );

   Page<Registro> findByUsuario(Usuario usuario, Pageable pageable);

}
