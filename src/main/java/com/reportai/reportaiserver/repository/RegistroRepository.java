package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

   @Query(value = """
               CALL SP_REGISTROS_POR_DISTANCIA(:latitude, :longitude, :distancia, :paginacao, :pagina)
           """, nativeQuery = true)
   List<Registro> findByDistance(
           @Param("latitude") double latitude,
           @Param("longitude") double longitude,
           @Param("distancia") double distancia,
           @Param("paginacao") int paginacao,
           @Param("pagina") int pagina
   );
}
