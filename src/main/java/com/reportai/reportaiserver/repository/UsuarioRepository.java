package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
   Optional<Usuario> findByEmail(String email);

   Optional<Usuario> findByCpf(String cpf);

   Optional<Usuario> findByIdAndIsDeleted(Long id, boolean b);

   @Query(value = """
           SELECT *
           FROM usuario
           WHERE NOT is_deleted
             AND (
               LOWER(nome)  LIKE LOWER(CONCAT('%', :termo, '%')) OR 
               LOWER(cpf)   LIKE LOWER(CONCAT('%', :termo, '%')) OR 
               LOWER(email) LIKE LOWER(CONCAT('%', :termo, '%')) OR
                          ID LIKE CONCAT('%', :termo, '%')
             )
           ORDER BY nome
            LIMIT :limite
            OFFSET :offset
           """,

           nativeQuery = true)
   List<Usuario> searchAtivosByTermo(@Param("termo") String termo,
                                     @Param("offset") int offiset,
                                     @Param("limite") int limite
   );

   @Query(value = """
           SELECT COUNT(*)
           FROM usuario
           WHERE NOT is_deleted
             AND (
               LOWER(nome)  LIKE LOWER(CONCAT('%', :termo, '%')) OR 
               LOWER(cpf)   LIKE LOWER(CONCAT('%', :termo, '%')) OR 
               LOWER(email) LIKE LOWER(CONCAT('%', :termo, '%')) OR
                          ID LIKE CONCAT('%', :termo, '%')
             )
           """,

           nativeQuery = true)
   int countAtivosByTermo(@Param("termo") String termo);


}
