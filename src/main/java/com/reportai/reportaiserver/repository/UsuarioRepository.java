package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
   Optional<Object> findByEmail(String email);

   Optional<Object> findByCpf(String cpf);
}
