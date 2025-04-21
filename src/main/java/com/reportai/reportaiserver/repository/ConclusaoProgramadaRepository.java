package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.ConclusaoProgramada;
import com.reportai.reportaiserver.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ConclusaoProgramadaRepository extends JpaRepository<ConclusaoProgramada, Long> {

   ConclusaoProgramada findByRegistroAndRemovidaEm(Registro registro, LocalDateTime removidaEm);
}
