package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
   Optional<Usuario> findByEmail(String email);

   Optional<Usuario> findByCpf(String cpf);

   Optional<Usuario> findByIdAndIsDeleted(Long id, boolean b);

   Page<Usuario> findByIsDeleted(boolean b, Pageable pageable);
}
