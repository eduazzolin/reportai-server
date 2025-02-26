package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
