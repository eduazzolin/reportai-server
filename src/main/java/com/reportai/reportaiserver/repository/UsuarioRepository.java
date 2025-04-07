package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.dto.UsuarioListagemAdminProjection;
import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
   Optional<Usuario> findByEmail(String email);

   Optional<Usuario> findByCpf(String cpf);

   Optional<Usuario> findByIdAndIsDeleted(Long id, boolean b);

   @Query(value = "CALL SP_ADMIN_LISTAR_USUARIOS(:termo, :offset, :limite, :ordenacao)", nativeQuery = true)
   List<UsuarioListagemAdminProjection> searchAtivosByTermo(@Param("termo") String termo,
                                                            @Param("offset") int offset,
                                                            @Param("limite") int limite,
                                                            @Param("ordenacao") String ordenacao
   );

   @Query(value = "CALL SP_ADMIN_LISTAR_USUARIOS_COUNT(:termo)", nativeQuery = true)
   int countAtivosByTermo(@Param("termo") String termo);


}
